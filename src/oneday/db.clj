(ns oneday.db
  (:require [jdbc.core :as jdbc]))

(defn start [config]
  (let [conn (jdbc/connection (-> config :db :spec))]
    (assoc-in config [:db :connection] conn)))

(defn stop [config]
  (if-let [c (-> config :db :connection)]
    (.close c))
  config)

