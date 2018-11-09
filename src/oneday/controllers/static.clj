(ns oneday.controllers.static
  (:require [clojure.java.io :as io]
            [ring.util.response :refer [resource-response]]))

(defn send-resource [req route]
  (let [res (str "public/" (-> route :route-params :path))
        r (resource-response res)]
    {:respond r}))
