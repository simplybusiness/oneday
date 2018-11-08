(ns oneday.controllers.proposal
  (require [oneday.domain :as d]
           [jdbc.core :as jdbc]
           [clojure.walk :refer [keywordize-keys]]
           [ring.util.response :as rsp]
           [oneday.views.proposal :as v]))

;; a controller ns chooses the view to render, and does any
;; lookups (local database or external services) to get the
;; data that view will need

(defn post [r _]
  (let [p (and (= (:request-method r) :post)
               (keywordize-keys (:form-params r)))]
    (if (and p (d/post-proposal (:db r) (assoc p :sponsor (:username r))))
      {:respond (rsp/redirect "/" :see-other)}
      ;; not happy about the value I'm sending into this view. It's
      ;; maybe a special case because there is yet no entity associated
      ;; with the view - just the stuff that the user keyed in but
      ;; which would not validate as a legitimate proposal
      {:view v/post :params p})))

(defn index [r _]
  (let [offset 0
        limit 10
        proposals (jdbc/fetch (:db r)
                              ["select p.*,a.handle as sponsor from 
(proposal p left join agent a on a.id=p.agent)
where p.created_at is not null 
order by created_at desc
offset ? limit ? "
                               offset limit])]
    {:view v/index
     :proposals proposals}))

(defn show [r route]
  (let [id (-> route :route-params :id Integer/parseInt)
        proposal
        (first (jdbc/fetch (:db r)
                           ["select * from proposal 
                                where id = ?" id]))]
    {:view v/show
     :proposal proposal}))
