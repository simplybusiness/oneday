(ns oneday.helpers)

(defn credentials [request]
  (get-in request [:session "google" :credentials :id_token_decoded]))

(defn format-time [time]
  (let [now (java.util.Date.)
        diff (- (.getTime now) (.getTime time))]
    (cond (< diff 10000) "a few seconds ago"
          (< diff 60000) (str (int (/ diff 1000)) " seconds ago")
          (< diff 300000) (str (int (/ diff 60000)) " minutes ago")
          :else
          (let [sdf (java.text.SimpleDateFormat. "yyyy-MM-dd hh:mm")]
            (.format sdf time)))))

(defn merge-attrs [[tagname attrs & content] more-attrs]
  (into [tagname (merge attrs more-attrs)] content))

(defn request-subscriber-id [request]
  (-> request :session :subscriber :id))
