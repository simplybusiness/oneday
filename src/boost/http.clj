(ns boost.http
  (:require [ring.adapter.jetty :refer [run-jetty]]))

(defn handle-request [r]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "hello world 2\n"})

(defn handler [r] (#'handle-request r))

(defonce server (atom nil))
(defn start [config]
  (reset! server (future (run-jetty handler (merge {:port 3000} config)))))

