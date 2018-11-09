(ns oneday.domain
  (:require [jdbc.core :as jdbc]))

(defn post-proposal [db p]
  (let [res (jdbc/fetch db ["insert into proposal (title,description,complexity,agent) values (?,?,?, (select id from agent where handle = ? limit 1)) returning id"
                            (:title p)
                            (:description p) 
                            (:complexity p)
                            (:sponsor p)])]
    (:id (first  res))))

(defn add-comment [db proposal-id comment]
  (let [res (jdbc/fetch db ["insert into comment (proposal_id, text, interested, author_id) values (?,?,?, (select id from agent where handle = ? limit 1)) returning id"
                            proposal-id
                            (:text comment)
                            (:interested comment) 
                            (:author comment)])]
    (:id (first  res))))
