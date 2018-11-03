(ns boost.http
  (:require [bidi.bidi :as bd]
            [boost.page :refer [page]]
            [boost.views :as v]
            [ring.util.response :as rsp]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def routes
  ["/"
   {"" :boost
    "post" :post
    "about" :about}])

(defmulti dispatch (fn [match _] (:handler match)))

(defmethod dispatch :boost [_ r]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body (str "boost\n")})

(defmethod dispatch :post [_ r] (v/post-boost r))

(defmethod dispatch :about [_ r]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body (str "about\n")})


(defn app-handler [r]
  (let [route (bd/match-route routes (:uri r))]
    (if route
      (let [response (dispatch route r)]
        (println response)
        response)
      (rsp/content-type
       (rsp/not-found "not found")
       "text/plain"))))

(def handle-request (wrap-stacktrace app-handler))

(defn handler [r] (#'handle-request r))

(defonce server (atom nil))
(defn start [config]
  (reset! server (future (run-jetty handler (merge {:port 3000} config)))))
