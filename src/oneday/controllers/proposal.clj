(ns oneday.controllers.proposal
  (require [oneday.domain :as d]
           [clojure.java.io :as io]
           [jdbc.core :as jdbc]
           [clojure.walk :refer [keywordize-keys]]
           [ring.util.response :as rsp]
           [oneday.helpers :as h]
           [oneday.views.proposal :as v]))

;; a controller ns chooses the view to render, and does any
;; lookups (local database or external services) to get the
;; data that view will need



;; THINKABOUTME there's quite a lot of sql and jdbc grunge in this
;; file which feels like it should be moved into a db interface of
;; some kind

(def proposal-sql (slurp (io/resource "sql/proposal-frag.sql")))

(defn index [r _]
  (let [offset 0
        limit 10
        proposals (jdbc/fetch
                   (:db r)
                   [(str proposal-sql
                         "order by created_at desc offset ? limit ?")
                    offset limit])]
    {:view v/index
     :proposals proposals}))

(defn show [r route]
  (let [id (-> route :route-params :id Integer/parseInt)
        proposal (d/get-proposal-by-id  (:db r) id)
        editable? (d/may-update? {:id (h/request-subscriber-id r)}
                                 proposal)
        sponsors
        (jdbc/fetch (:db r) ["select sum(points),s.* from kudosh k join subscriber s on s.id=k.sponsor_id  where proposal_id=? group by s.id" id])
        comments
        (jdbc/fetch (:db r) ["select c.*,s.handle as author from comment c join subscriber s on c.author_id=s.id where proposal_id=? and text<>'' order by created_at " id])
        ]
    {:view v/show
     :proposal proposal
     :sponsors sponsors
     :edit-url (and editable? (str (:uri r) "/edit"))
     :comments comments}))

(defn post [r _]
  (let [p (and (= (:request-method r) :post)
               (keywordize-keys (:form-params r)))
        success (and
                 p
                 (let [p (assoc p :proposer-id (h/request-subscriber-id r))]
                   (d/post-proposal (:db r) p)))]
    (if success
      {:redirect show :id success}
      ;; not happy about the value I'm sending into this view. It's
      ;; maybe a special case because there is yet no entity associated
      ;; with the view - just the stuff that the user keyed in but
      ;; which would not validate as a legitimate proposal
      {:view v/post :params p})))

(defn edit [r route]
  (let [id (-> route :route-params :id Integer/parseInt)
        p (and (= (:request-method r) :post)
               (keywordize-keys (:form-params r)))
        before (d/get-proposal-by-id (:db r) id)
        editable? (d/may-update? {:id (h/request-subscriber-id r)}
                                 before)]
    (if (d/may-update? {:id (h/request-subscriber-id r)}
                       before)
      (if-let [success (and p (d/update-proposal (:db r) id p))]
        {:redirect show :id id}
        {:view v/edit :params (d/get-proposal-by-id (:db r) id)})
      {:respond (-> "Current subscriber may not edit"
                    rsp/response
                    (rsp/status 403)
                    (rsp/content-type "text/plain"))})))

