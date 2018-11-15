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

;; Guessing the type of a config attribute based on its value
;; is obviously (I claim) unreliable and for that reason not
;; good style.  Perhaps a future version of this software will
;; use core.spec to define the allowable configuration robustly

(defn guess-type [v]
  (cond
    (= v "true") true
    (= v "false") false
    (re-matches #"-?[0-9]+" v) (Integer/parseInt v)
    true v))
      

(defn nest-map [h]
  "Turns a single-level map with entries L1__L2__L3=v into a nested map {:l1 => { :l2 => { :l3 => v } } }"
  (let [kwize (fn [s] (keyword (clojure.string/replace
                                (.toLowerCase s) #"_" "-")))]
    (reduce (fn [a [k v]]
              (assoc-in a (map kwize
                               (clojure.string/split k  #"__")) (guess-type v)))
            {}
            h)))

(defn deep-merge [a b] (if (map? a) (merge-with deep-merge a b) b))

(defn -main
  [config-file & args]
  (let [cfg (-> config-file
                slurp
                edn/read-string
                (deep-merge (:oneday (nest-map (System/getenv)))))]
    (println cfg)
    (reset! system (run cfg))
    (println "Hello, World!")))
