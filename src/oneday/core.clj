(ns oneday.core
  (:require oneday.http)
  (:require oneday.db)
  (:require [clojure.edn :as edn])
  (:gen-class))

(defonce default-config
  {:http
   {:port 3000}
   :db
   {:spec "postgresql://onedayuser:onedaypw@localhost/oneday"}})

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
  (let [cfg (edn/read-string (slurp config-file))]
    (println cfg)
    (reset! system (run cfg))
    (println "Hello, World!")))

