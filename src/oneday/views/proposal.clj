(ns oneday.views.proposal
  (:require [oneday.page :refer [page]]
            [hiccup.form :as f]))

(defn post [state]
  (let [p (:params state)]
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

(defn show [value]
  (println value)
  (let [prop (:proposal value)]
    (page (:title prop)
          [:div
           [:div.timestamp {} "Created at " (:created_at prop)]
           [:p {} (:description prop)]
           ;; XXX tack the comments on here too
           ])))
         
        

(defn link-to [p]
  [:a {:href (str "show/" (:id p))}
   (:title p)])

(defn index [value]
  (page "Oneday proposals"
        [:ul (map (fn [l] [:li {} (link-to l)])
                  (:proposals value))]))
