(ns oneday.db
  (:require [migratus.core :as migratus]
            [jdbc.core :as jdbc]))

(defn migration-config [config]
  {:store :database
   :migration-dir "migrations"
   :db (str "jdbc:" (-> config :db :spec))})

(defn migrate [config]
  (migratus/migrate (migration-config config)))

(defn start [config]
  (migrate config)
  (let [conn (jdbc/connection (-> config :db :spec))]
    (assoc-in config [:db :connection] conn)))

(defn stop [config]
  (if-let [c (-> config :db :connection)]
    (.close c))
  config)

