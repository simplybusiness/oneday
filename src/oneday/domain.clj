(ns oneday.domain
  (:require [jdbc.core :as jdbc]))

(defn post-proposal [db p]
  (let [res (jdbc/fetch db ["insert into proposal (title,description,complexity) values (?,?,?) returning id"
                            (:title p)
                            (:description p) 
                            (:complexity p)])]
    (:id res)))
