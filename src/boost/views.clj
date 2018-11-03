(ns boost.views
  (:require [boost.page :refer [page]]
            [hiccup.form :as f]))

(defn post-boost [r]
  (let [p {:complexity "Complex"} ] ; (params r)
    (page "Post a boost"
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
                                    (:complexity p))]]))))
