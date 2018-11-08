(ns oneday.controllers.static
  (:require [clojure.java.io :as io]
            [ring.util.response :refer [resource-response]]))

(defn send-resource [req route]
  (let [r (resource-response (io/resource "public/" (:path route)))]
    (println r)
    {:respond r}))
