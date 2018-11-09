(ns oneday.domain
  (:require [jdbc.core :as jdbc]))

(defn post-proposal [db p]
  (let [res (jdbc/fetch db ["insert into proposal (title,description,complexity,agent) values (?,?,?, (select id from agent where handle = ? limit 1)) returning id"
                            (:title p)
                            (:description p) 
                            (:complexity p)
                            (:sponsor p)])]
    (:id (first  res))))
