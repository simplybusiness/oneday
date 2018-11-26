;; the majority of this file is about routes, not http per se
;; think about renaming it

(ns oneday.http
  (:require [bidi.bidi :as bd]
            [bidi.ring :refer [->Resources]]
            [clojure.java.io :as io]
            [oneday.page :refer [page]]
            oneday.controllers.proposal
            oneday.controllers.static
            oneday.controllers.comment
            [oneday.domain :as domain]
            [ring.util.response :as rsp]
            [cheshire.core :as json]
            [authomatic.oidc :refer [wrap-oidc]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.content-type :refer (wrap-content-type)]
            [oneday.ring-log :as ring-log]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.test :as test :refer [is deftest with-test]]))

(def routes
  ["/" {["static/" [ #"[a-zA-Z0-9/_\.-]+" :path]]
        #'oneday.controllers.static/send-resource
        "proposals/"
        {"" #'oneday.controllers.proposal/index
         "post" #'oneday.controllers.proposal/post
         [:id] #'oneday.controllers.proposal/show
         [:id "/comments/new"] #'oneday.controllers.comment/new
         }}])


(defn app-handler [r]
  (let [route (bd/match-route routes (:uri r))]
    (if route
      (let [controller (:handler route)
            view-data (controller r route)]
        (if-let [view (:view view-data)]
          (rsp/charset (view (dissoc view-data :view)) "UTF-8")
          (:respond view-data)))
      (rsp/content-type (rsp/not-found "not found") "text/plain"))))

(defn wrap-auth [h]
  (fn [r]
    ;; find local user from foreign credentials, and put in session to
    ;; avoid database lookup on every single request
    (if-let [subscriber (get-in r [:session :subscriber])]
      (h r)
      (let [token (get-in r [:session "google" :credentials :id_token_decoded])
            subscriber (domain/get-subscriber-from-id-token (:db r) token)
            response (h (assoc-in r [:session :subscriber] subscriber))
            session (or (:session response) (:session r))]
        (assoc response :session (assoc session :subscriber subscriber))))))


(defn middlewares [config]
  (-> app-handler
      (wrap-auth)
      (wrap-oidc (-> config :oidc :google))
      wrap-params
      wrap-content-type
      (wrap-session
       {:cookie-attrs {:secure (-> config :http :secure)}
        :store (cookie-store {:key (-> config :http :session-secret)})})
      (ring-log/wrap-catch-and-log {:log-stream *err* :log-format :json})
      ))

(defn start [config]
  (let [db (-> config :db :connection)
        log-stream (if-let [f (-> config :http :log-file-name)]
                     (io/writer (io/file f) :append true)
                     *err*)
        pipeline (binding [*err* log-stream] (middlewares config))
        wrap-db (fn [h] (fn [r] (h (assoc r :db db))))
        server (run-jetty (wrap-db pipeline)
                          (assoc (:http config) :join? false))]
    (binding [*out* log-stream]
      (println (json/generate-string {:timestamp (java.util.Date.)
                                      :message "HTTP server ready"})))
    (assoc-in config [:http :server] server)))

(defn stop [config]
  (.stop (-> config :http :server))
  config)
