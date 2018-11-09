(ns oneday.views.proposal
  (:require [oneday.page :refer [page]]
            [clojure.java.io :as io]
            [oneday.helpers :as h]
            [markdown.core :as md]
            [hiccup.form :as f]))

(def placeholder-descr (slurp (io/resource "placeholder-description.md")))

(defn post [state]
  (let [p (merge  {:description placeholder-descr} (or (:params state) {}))]
    (page "Post a proposal"
          [:header.gradient {} [:h1 "Post a proposal"]]
          [:div.proposal {}
           (f/form-to
            [:post ""]
            [:div {}
             [:label {:for :title} "Title"]
             (f/text-field :title (:title p))]
            [:div {}
             [:label {:for :description} "Description"]
             (f/text-area :description (:description p))]
            [:div {}
             [:label {:for :tags} "Tags"]
             (f/text-field :tags (:tags p))]
            [:div {}
             [:label {:for :complexity} "Complexity"]
             (f/drop-down
              :complexity
              ["Obvious"
               "Complicated"
               "Complex"
               "Chaotic"]
              (:complexity p))]
            [:button {} "Post"])])))

(defn link-to [p]
  [:a {:href (:id p)}
   (:title p)])

(defn dateline [prop]
  [:div.dateline "Proposed "
   (if-let [c(:created_at prop)] (h/format-time c) "")
   " by " (or (:sponsor prop) "a mystery guest")])

(defn description [prop]
  (md/md-to-html-string (:description prop)))
  

(defn proposal-summary [prop]
  [:div.proposal {:onclick (str "window.location=window.location.href+" (:id prop))}
   [:h2 (link-to prop)]
   (dateline prop)
   [:p {:align :right
        :style "font-weight: bold"}
    [:a {:href (:id prop)}
     (:kudosh prop) " kudosh"]
    " "
    [:a {:href (:id prop)}   
     (:comments prop) " comments"]]
   ])


(defn show [value]
  (let [prop (:proposal value)]
    (page
     (str "oneday - " (:title prop))
     [:header.gradient
      [:h1 (:title prop)]
      (dateline prop)]
     [:div.proposal {}
      [:blockquote (description prop)]
      [:button {} "Comment"]])))

(defn index [value]
  (page
   "One day ..."
   [:header.gradient {} [:h1 "One day ..."]]
   [:div.intro
    [:p "Ever said “<b>one day</b> we ought to do <i>thing</i>” ?"]
    [:p "or “I think we could do  <i>thing</i> in <b>one day</b>”?"]
    [:p {} [:a {:href "post"} "Make your proposal"]
     " for the thing we need to do"]]
   [:div.proposals (map proposal-summary (:proposals value))]))
