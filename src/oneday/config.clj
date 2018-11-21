(ns oneday.config
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test :refer [is deftest with-test]]))

;; Guessing the type of a config attribute based on its value is
;; obviously (I *claim* this is obvious ...) unreliable and for that
;; reason not good style.  Perhaps a future version of this software
;; will use core.spec to define the allowable configuration robustly

(defn guess-type [v]
  (cond
    (= v "true") true
    (= v "false") false
    (re-matches #"-?[0-9]+" v) (Integer/parseInt v)
    true v))
      

(defn nest-map [h]
  "Turns a single-level map with entries L1__L2__L3=v into a nested map {:l1 => { :l2 => { :l3 => v } } }, transforming underscores in map keys into hyphens"
  (let [kwize (fn [s] (keyword (str/replace
                                (.toLowerCase s) #"_" "-")))]
    (reduce (fn [a [k v]]
              (assoc-in a (map kwize
                               (str/split k  #"__")) (guess-type v)))
            {}
            h)))

(deftest nest-map-test
  (is (= {:l1 { :l2 {:l3 "v"}}} (nest-map {"L1__L2__L3" "v"})))
  (is (= {:l1-l2 {:l3 "v"}} (nest-map {"L1_L2__L3" "v" }))))


(defn deep-merge [a b] (if (map? a) (merge-with deep-merge a b) b))

(deftest deep-merge-test
  (is (= {:a {:b 1} :c 2}
         (deep-merge {:a :foo :c 2} {:a {:b 1}}))))

(defn read-env
  ([prefix] (read-env prefix (System/getenv)))
  ([prefix env] (get (nest-map env) prefix)))

(defn read-file [config-file] (-> config-file slurp edn/read-string))

(defn read-config [config-file env-prefix]
  (deep-merge (read-file config-file) (read-env env-prefix)))


(def secret-paths [[:http :session-secret]
                   [:db :spec]
                   [:oidc :google :client-secret]])

(defn redact-config [config]
  "Given a configuration map, replace the sensitive/secret values
(as specified by `secret-paths` with the key `:redacted` so that it
can safely be e.g. printed to a log file"
  (reduce (fn [c path] (assoc-in c path :redacted))
          config
          secret-paths))

(deftest redact-config-test
  (let [config
        {:http
         {:port 3000
          :session-secret 'aaaaaaaaaaaaaaaa'
          }
         :db
         {:spec "postgresql://onedayuser:onedaypw@localhost/oneday"}
         :oidc
         {:google
          {
           :client-id "1234567890"
           :client-secret "ZZZZZZZZzzzzzz"
           :discovery-uri "https://accounts.google.com/.well-known/openid-configuration"
           :provider-name "google"
           :redirect-uri "http://localhost:3000/login/google/postauth"
           }
          }
         }
        redacted (redact-config config)]
    (is (= :redacted (get-in redacted [:http :session-secret])))
    (is (= :redacted (get-in redacted [:db :spec])))
    (is (= :redacted (get-in redacted [:oidc :google :client-secret])))
    (is (= (get-in redacted [:http :port]) (get-in config [:http :port])))))
