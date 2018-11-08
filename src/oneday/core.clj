(ns oneday.core
  (:require oneday.http)
  (:require oneday.db)
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
        
(defn -main
  [& args]
  (reset! system (run default-config))
  (println "Hello, World!"))
