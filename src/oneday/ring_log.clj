(ns oneday.ring-log
  (:require [ring.util.response :as rsp]
            [cheshire.core :as json]
            [clojure.test :as test :refer [is deftest with-test]]))

(defn stacktrace-el->array [el]
  (let [{:keys [className fileName lineNumber methodName]} (bean el)]
    (map (fn [v] (and v (.toString v)))
         [className fileName lineNumber methodName])))

(defn error-attributes [ex]
  (let [m (Throwable->map ex)]
    {:cause (:cause m)
     :trace (if-let [trace (:trace m)]
              (map stacktrace-el->array trace))
     :via (map (fn [{ :keys [type message at]}]
                 {:type (pr-str type)
                  :message message
                  :at  (pr-str at)})
               (:via m))}))

(defn log-entry-payload [request response exception]
  (let [value {:http-request
               (select-keys request [:uri :remote-addr :headers
                                     :server-port :server-name
                                     :query-string
                                     :scheme :request-method])
               :status (:status response)
               :timestamp (java.util.Date.)
               :exception exception}]
    (into {} (filter val value))))

(defn json-log [request response & [exception]]
  (println
   (json/generate-string (log-entry-payload request response exception))))

(defn edn-log [request response & [exception]]
  (println
   (log-entry-payload request response exception)))


(defn wrap-catch-and-log [h {:keys [log-stream log-format]}]
  (fn [request]
    (let [[response error]
          (try
            [(h request) nil ]
            (catch Throwable ex
              [(-> (rsp/response "Internal error.  Clean up on aisle 5")
                   (rsp/status 500)
                   (rsp/content-type "text/plain"))
               (error-attributes ex)]))]
      (binding [*out* log-stream]
        ((get {:json json-log :edn edn-log} log-format edn-log)
         request response error))
      response)))

(deftest wrap-log-exception
  (let [s (java.io.StringWriter.)
        handler (wrap-catch-and-log (fn [r] (/ 1 0))
                                    {:log-stream s})]
    (handler {:uri "/" :request-method :get})
    (let [line (.toString s)]
      (is (re-find #":status 500" line))
      (is (re-find #":exception" line))
      (is (re-find #":cause Divide by zero" line)))))

(deftest wrap-log-ok
  (let [s (java.io.StringWriter.)
        handler (wrap-catch-and-log (fn [r] (rsp/response "OK"))
                                    {:log-stream s})]
    (handler {:uri "/" :request-method :get})
    (let [line (.toString s)]
      (is (re-find #":status 200" line))
      (is (not (re-find #":exception" line))))))


;; (let [handler (wrap-catch-and-log (fn [r] (rsp/response "dfgg")))]
;;   (handler {:uri "/" :request-method :get}))

