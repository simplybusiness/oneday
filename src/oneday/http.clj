;; the majority of this file is about routes, not http per se
;; think about renaming it

(ns oneday.http
  (:require [bidi.bidi :as bd]
            [oneday.page :refer [page]]
            [oneday.views :as v]
            oneday.controllers.proposal
            [ring.util.response :as rsp]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def routes
  ["/"
   {"" #'oneday.controllers.proposal/index
    "post" #'oneday.controllers.proposal/post
    #_#_ "about" :about}])


(defn app-handler [r]
  (let [route (bd/match-route routes (:uri r))]
    (if route
      (let [controller (:handler route)
            view-data (controller r)]
        (if-let [view (:view view-data)]
          (view (dissoc view-data :view))
          (:respond view-data)))
      (rsp/content-type (rsp/not-found "not found") "text/plain"))))

(def handle-request (-> app-handler wrap-params wrap-stacktrace))

(defn handler [r] (#'handle-request r))

(defonce server (atom nil))
(defn start [config]
  (reset! server (future (run-jetty handler (merge {:port 3000} config)))))
