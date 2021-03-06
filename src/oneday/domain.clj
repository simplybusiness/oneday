(ns oneday.domain
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [jdbc.core :as jdbc]))

(def proposal-sql (slurp (io/resource "sql/proposal-frag.sql")))

(defn get-proposal-by-id [db id]
  (first (jdbc/fetch
          db
          [(str "select * from ("
                proposal-sql
                " ) proposal where id = ?") id])))
  

(defn may-update? [subscriber proposal]
  (or (= (:id subscriber) (:proposer_id proposal))
      #_ (= (:id subscriber) 1)))

(defn post-proposal [db p]
  (let [res (jdbc/fetch db ["insert into proposal (title,description,complexity,status,proposer_id) values (?,?,?, ?::proposal_status,?) returning id"
                            (:title p)
                            (:description p) 
                            (:complexity p)
                            (:status p)
                            (:proposer-id p)])]
    (:id (first  res))))

(defn update-proposal [db id p]
  (let [res (jdbc/fetch db ["update proposal set title=?,description=?,complexity=?,status=?::proposal_status,updated=now() where id=? returning id"
                            (:title p)
                            (:description p) 
                            (:complexity p)
                            (:status p)
                            id])]
    (:id (first  res))))

(defn add-kudosh [db proposal-id points sponsor-id]
  (:created_at (first (jdbc/fetch db ["insert into kudosh (proposal_id, points, sponsor_id) values (?,?, ?) returning created_at"
                                      proposal-id points sponsor-id]))))

(defn add-comment [db proposal-id comment]
  (let [res (jdbc/fetch db ["insert into comment (proposal_id, text, interested, demo, author_id) values (?,?,?,?, ?) returning id"
                            proposal-id
                            (:text comment)
                            (:interested comment)
                            (:demo comment)
                            (:author-id comment)])]
    (when-let [id (:id (first  res))]
      (and (or (not (:sponsor comment))
               (add-kudosh db proposal-id (:kudosh comment) (:author-id comment)))
           id))))
          
      
(defn get-subscriber-from-id-token [db id-token]
  ;; if wrap-auth works correctly it will set subscriber in the session,
  ;; so ths function is only going to be called once per session
  (let [iss (:iss id-token)
        sub (:sub id-token)
        display-name (:name id-token)
        handle (str/replace (.toLowerCase display-name)
                            #"[^a-zA-Z0-9-]" "")
        payload (json/generate-string id-token)
        subscriber (or (first (jdbc/fetch db ["select s.* from subscriber s join authentication a on s.id=a.subscriber_id where a.iss=? and a.sub=?"
                                           iss sub]))
                       ;; this is possibly racy, if new subscriber
                       ;; is logging in from two browsers at once
                       (first (jdbc/fetch db ["insert into subscriber (handle, display_name) values (?,?) returning *",
                                           handle
                                           display-name])))
        subscriber-id (:id subscriber)]
    (jdbc/execute db ["insert into authentication (subscriber_id,iss,sub,display_name,payload) values (?,?,?,?,?::jsonb) on conflict on constraint authentication_pkey do update set subscriber_id=?, iss=?, sub=?, display_name=?, payload=?::jsonb"
                      subscriber-id iss sub display-name payload
                      subscriber-id iss sub display-name payload])
    subscriber))
