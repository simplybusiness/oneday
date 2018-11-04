(ns boost.controllers.boost
  (require [boost.views.boost :as v]))

;; a controller ns chooses the view to render, and does any
;; lookups (local database or external services) to get the
;; data that view will need

(defn post [r]
  {:view v/post
   :params {:title "hey" :description "you"}})

(defn index [r]
  {:view v/index
   :boosts [1 2 3 4 ]})
