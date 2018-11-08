(ns oneday.page
  (:require [hiccup.core :as h]
            [hiccup.page :as p]))

(defn page [title content]
  (let [body
        (p/html5
         {}
         [:head {}          
          [:link {:rel "stylesheet" :href "/static/styles.css"}]
          [:title {} title]]
         [:body {}
          [:h1.gradient title]
          [:div.contents content]])]
        {:status 200 :body body :headers {"content-type" "text/html"}}))
  
