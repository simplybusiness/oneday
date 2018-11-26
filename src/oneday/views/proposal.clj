(ns oneday.views.proposal
  (:require [oneday.page :refer [page]]
            [clojure.java.io :as io]
            [oneday.helpers :as h]
            [markdown.core :as md]
            [hiccup.form :as f]))

(def placeholder-descr (slurp (io/resource "placeholder-description.md")))

(defn proposal-form [p]
  (f/form-to
   [:post ""]
   [:div {}
    [:label {:for :title} "Title"]
    (f/text-field :title (:title p))]
   [:div {}
    [:label {:for :description :title "(markdown ok)"}
     "Description" ]
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
   [:button {} "Post"]))
  
(defn post [state]
  (let [p (merge  {:description placeholder-descr} (or (:params state) {}))]
    (page "Post a proposal"
          [:div.proposal.post-proposal {}
           [:h2 "Post a proposal"]
           (proposal-form p)
           ])))

(defn edit [state]
  (let [p (merge  {:description placeholder-descr} (or (:params state) {}))]
    (page "Edit proposal"
          [:div.proposal.post-proposal {}
           [:h2 "Edit proposal"]
           (proposal-form p)
           ])))

(defn link-to [p]
  [:a {:href (:id p)}
   (:title p)])

(defn dateline [prop]
  [:div.dateline "Proposed "
   (if-let [c(:created_at prop)] (h/format-time c) "")
   " by " (or (:proposer prop) "a mystery guest")])

(defn description [prop]
  (md/md-to-html-string (:description prop)))
  

(defn proposal-summary [prop]
  [:div.proposal {:onclick (str "window.location=window.location.href+" (:id prop))}
   [:h2 (link-to prop)]
   (dateline prop)
   [:p {:align :right
        :style "font-weight: bold"}
    [:a {:href (:id prop)}
     (:sponsors_count prop) " sponsors"]
    ", "
    [:a {:href (:id prop)}   
     (:comments prop) " comments"]]
   ])

(defn show-comment [c]
  (let [interested? (:interested c)
        demo? (:demo c)
        sponsor? (:sponsor c)]
    [:div.comment
     {:class (cond interested? "interested" demo? "demo" :else "drive-by")}
     [:div.attribution {} 
      "At " (h/format-time (:created_at c)) ", "
      (:author c)
      (cond
        interested? " said they were interested in working on it"
        demo? " requested a review"
        :else " wrote")
       ":"]
     [:div.body (:text c)]]))

(defn show [value]
  (let [prop (:proposal value)
        kudosh (fn [n]
                 [:label {}
                  (h/merge-attrs
                   (f/radio-button :kudosh false n)
                   {:onchange "if(this.checked) document.getElementById('sponsor').checked=true;"})
                  n])]
    (page
     (str "oneday - " (:title prop))
     [:div.proposal {}
      [:h2 (:title prop)]
      (dateline prop)
      [:blockquote (description prop)]
      (when-let [sponsors (seq (:sponsors value))]
        [:div.sponsors {}
         [:h3 "Sponsored by"]
         [:ul (map
               (fn [s] [:li (:handle s) " (" (:sum s) " points)"])
               sponsors)]])
      [:div.comments {}
       [:h3 "Comments"]
       (map show-comment (:comments value))
       [:p]
       [:form {:class :comment
               :method "POST"
               :action (str (:id prop) "/comments/new")}
        [:div.field
         (h/merge-attrs (f/text-area :text "")
                        {:placeholder "What do you think?"})]
        [:div.field
         [:label {} (f/check-box :interested) " I am interested in working on this (no commitment)"]]
        [:div.field
         (h/merge-attrs
          (f/check-box :sponsor)
          {:onclick
           "this.checked||Array.prototype.map.call(document.getElementsByName('kudosh'), function(l) {l.checked=false;})"})
         " I want to sponsor this work with "
         (kudosh "10") " "
         (kudosh "20") " "         
         (kudosh "40") " kudosh"]
        [:div.field
         [:label {} (f/check-box :demo) " I have been working on this: please review my approach /solution!"]]        
        [:div.field [:button {} "Add comment"]]

        ]]])))


(defn index [value]
  (page
   "One day ..."
   [:div.intro
    [:p "Ever said “<b>one day</b> we ought to do <i>thing</i>” ?"]
    [:p "or “I think we could do  <i>thing</i> in <b>one day</b>”?"]
    [:p {} [:a {:href "post"} "Make your proposal"]
     " for the thing we need to do"]]
   [:div.proposals (map proposal-summary (:proposals value))]))
