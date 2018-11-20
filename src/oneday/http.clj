;; the majority of this file is about routes, not http per se
;; think about renaming it

(ns oneday.http
  (:require [bidi.bidi :as bd]
            [bidi.ring :refer [->Resources]]
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
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.adapter.jetty :refer [run-jetty]]))

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

(defn stacktrace-el->map [el]
  (let [{:keys [className fileName lineNumber methodName]} (bean el)]
    (map (memfn toString) [className fileName lineNumber methodName])))

(defn error-attributes [ex]
  (let [m (Throwable->map ex)]
    {:cause (:cause m)
     :trace (map stacktrace-el->map (:trace m))
     :via (map (fn [{ :keys [type message at]}]
                 {:type (pr-str type)
                  :message message
                  :at  (pr-str at)})
               (:via m))}))

(defn json-log [request response & [exception]]
  (let [value {:request request
               :status (:status response)
               :exception exception}]
    (println (json/generate-string (into {} (filter val value)))))
  response)

(defn wrap-catch-and-log [h]
  (fn [r]
    (try
      (json-log r (h r))
      (catch Throwable ex
        (let [x (error-attributes ex)]
          (json-log r
                    (-> (rsp/response "Internal error.  Clean up on aisle 5")
                        (rsp/status 500)
                        (rsp/content-type "text/plain"))
                    x))))))
               
    
;; (let [handler (wrap-catch-and-log (fn [r] (/ 1 0)))]
;;   (handler {:uri "/" :request-method :get}))

;; (let [handler (wrap-catch-and-log (fn [r] (rsp/response "dfgg")))]
;;   (handler {:uri "/" :request-method :get}))


(defn middlewares [config]
  (-> app-handler
      (wrap-auth)
      (wrap-oidc (-> config :oidc :google))
      wrap-params
      wrap-content-type
      (wrap-session
       {:cookie-attrs {:secure true}
        :store (cookie-store {:key (-> config :http :session-secret)})})
      wrap-catch-and-log
      ))

(defn start [config]
  (let [db (-> config :db :connection)
        pipeline (middlewares config)
        wrap-db (fn [h] (fn [r] (h (assoc r :db db))))
        server (run-jetty (wrap-db pipeline)
                          (assoc (:http config) :join? false))]
    (assoc-in config [:http :server] server)))

(defn stop [config]
  (.stop (-> config :http :server))
  config)
