;; the majority of this file is about routes, not http per se
;; think about renaming it

(ns oneday.http
  (:require [bidi.bidi :as bd]
            [oneday.page :refer [page]]
            oneday.controllers.proposal
            [ring.util.response :as rsp]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def routes
  ["/proposals/"
   {"" #'oneday.controllers.proposal/index
    "post" #'oneday.controllers.proposal/post
    [:id] #'oneday.controllers.proposal/show
    #_#_ "about" :about}])


(defn app-handler [r]
  (let [route (bd/match-route routes (:uri r))]
    (if route
      (let [controller (:handler route)
            view-data (controller r route)]
        (if-let [view (:view view-data)]
          (view (dissoc view-data :view))
          (:respond view-data)))
      (rsp/content-type (rsp/not-found "not found") "text/plain"))))

(def handle-request (-> app-handler wrap-params wrap-stacktrace))

(defn handler [r] (#'handle-request r))

(defn start [config]
  (let [db (-> config :db :connection)
        wrap-db (fn [h] (fn [r] (h (assoc r :db db))))
        server (future (run-jetty (wrap-db handler) (:http config)))]
    (assoc-in config [:http :server] server)))

