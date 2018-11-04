;; the majority of this file is about routes, not http per se
;; think about renaming it

(ns boost.http
  (:require [bidi.bidi :as bd]
            [boost.page :refer [page]]
            [boost.views :as v]
            boost.controllers.boost
            [ring.util.response :as rsp]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def routes
  ["/"
   {"" boost.controllers.boost/index
    "post" boost.controllers.boost/post
    #_#_ "about" :about}])


(defn app-handler [r]
  (let [route (bd/match-route routes (:uri r))]
    (if route
      (let [controller (:handler route)
            view-data (controller r)]
        ((:view view-data) (dissoc view-data :view)))
      (rsp/content-type (rsp/not-found "not found") "text/plain"))))

(def handle-request (wrap-stacktrace app-handler))

(defn handler [r] (#'handle-request r))

(defonce server (atom nil))
(defn start [config]
  (reset! server (future (run-jetty handler (merge {:port 3000} config)))))
