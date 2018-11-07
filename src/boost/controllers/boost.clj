(ns boost.controllers.boost
  (require [boost.domain :as d]
           [ring.util.response :as rsp]
           [boost.views.boost :as v]))

;; a controller ns chooses the view to render, and does any
;; lookups (local database or external services) to get the
;; data that view will need

(defn post [r]
  (let [p (and (= (:request-method r) :post) (:form-params r))]
    (println p)
    (if (d/post-boost p)
      {:respond (rsp/redirect "/" :see-other)}
      ;; not happy about the value I'm sending into this view. It's
      ;; maybe a special case because there is yet no entity associated
      ;; with the view - just the stuff that the user keyed in but
      ;; which would not validate as a legitimate boost
      {:view v/post :params p})))

(defn index [r]
  {:view v/index
   :boosts [1 2 3 4 ]})
