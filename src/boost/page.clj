(ns boost.page
  (:require [hiccup.core :as h]
            [hiccup.page :as p]))

(defn page [title content]
  (let [body
        (p/html5
         {}
         [:head {} [:title {} title]]
         [:body {}
          [:h1 title]
          [:div.contents content]])]
    {:status 200 :body body :headers {"content-type" "text/html"}}))
