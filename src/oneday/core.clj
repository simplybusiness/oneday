(ns oneday.core
  (:require oneday.http)
  (:require oneday.db)
  (:require [oneday.config :as config])
  (:require [clojure.edn :as edn])
  (:gen-class))

(defonce system (atom nil))

(defn run [config]
  (->> config
       oneday.db/start
       oneday.http/start))

(defn stop [sys]
  (->> sys
       oneday.http/stop
       oneday.db/stop))

(defn -main
  [config-file & args]
  (org.apache.log4j.BasicConfigurator/configure)
  (. (. org.apache.log4j.Logger getRootLogger)
     setLevel org.apache.log4j.Level/INFO)
  (let [cfg (config/read-config config-file :oneday)]
    (println (config/redact-config cfg))
    (reset! system (run cfg))
    (println "Hello, World!")))
