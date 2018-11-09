(ns oneday.controllers.comment
  (require [oneday.domain :as d]
           [clojure.java.io :as io]
           [jdbc.core :as jdbc]
           [clojure.walk :refer [keywordize-keys]]
           [ring.util.response :as rsp]
           #_ [oneday.views.comment :as v]))

(defn new [req route]
  (let [proposal-id (Integer/parseInt (-> route :route-params :id))
        fields (assoc (keywordize-keys (:form-params req))
                      :author (:username req))]
    (if-let [comment (d/add-comment (:db req) proposal-id fields)]
      {:respond (rsp/redirect (str "/proposals/" proposal-id)
                              :see-other)}
      {:respond
       {:status 200
        :headers {"content-type" "text/plain"}
        :body (pr-str fields)}})))
