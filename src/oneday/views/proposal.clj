(ns oneday.views.proposal
  (:require [oneday.page :refer [page]]
            [clojure.walk :refer [keywordize-keys]]
            [hiccup.form :as f]))

(defn post [state]
  (let [p (:params (keywordize-keys state))]
    (page "Post a proposal"
          (f/form-to [:post ""]
                     [:div.post {}
                      [:div {} "Title"
                       (f/text-field :title (:title p))]
                      [:div {} "Description"
                       (f/text-area :description (:description p))]
                      [:div {} "Complexity"
                       (f/drop-down :complexity
                                    ["Obvious"
                                     "Complicated"
                                     "Complex"
                                     "Chaotic"]
                                    (:complexity p))]]
                     [:button {} "Post"]
                     ))))
(defn index [value]
  (page "Oneday proposals"
        [:ul (map (fn [l] [:li {} l]) (:proposals value))]))
