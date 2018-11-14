(ns authomatic.oidc
  (:require
   [clj-http.client :as http]
   [clojure.string :as str]
   [ring.util.codec]
   [clojure.set]
   [clojure.walk :refer [keywordize-keys]]
   [ring.util.response :as resp]
   [cheshire.core :as json]
   [clojure.test :as test :refer [is deftest with-test]]
   )
  (:import [org.apache.commons.codec.binary Base64 Hex]))

(defn read-json-response [s]
  (keywordize-keys (json/decode (:body s))))

(defn discover-configuration [options]
  (if-let [uri (:discovery-uri options)]
    (merge (-> uri http/get read-json-response) options)
    options))

(defn validate-configuration [options]
  (let [actual (set (keys options))
        expected #{:userinfo_endpoint :authorization_endpoint
                   :client-id :redirect-uri :provider-name
                   :issuer :token_endpoint}]
    (or (and (clojure.set/subset? expected actual) options)
        (throw (ex-info "missing keys in configuration"
                        {:missing
                         (clojure.set/difference expected actual)})))))


(defn gen-oauth-state-token []
  (let [bytes (byte-array 32)]
    (with-open [r (java.io.FileInputStream. "/dev/urandom")]
      (.read r bytes 0 32))
    (Base64/encodeBase64String bytes)))

(def scopes-desired
  #{"openid" ;openid
    #_"sdps-r" ;yahoo
    "email" ;openid
    "public_profile" ;facebook
    "profile" ;openid
    })

(defn authorization-request-uri [options state]
  (let [{:keys [client-id 
                scopes_supported
                redirect-uri
                authorization_endpoint]} options
        scopes (clojure.set/intersection
                (set scopes_supported) scopes-desired)]
    (str authorization_endpoint
         "?"
         (ring.util.codec/form-encode
          {"client_id" client-id
           "response_type" "code"
           "scope" (str/join " " scopes)
           "redirect_uri" redirect-uri
           "state" state}))))


(defn redirect-to-idp [options request]
  (let [random (gen-oauth-state-token)
        state (str/join ":" [random (:uri request)])]
    (assoc-in
     (resp/redirect (authorization-request-uri options state))
     [:session (:provider-name options) :state]
     state)))


(defn decode-jwt [token]
  {:pre [(string? token)
         (.contains token ".")]}
  (let [[header payload sig] (str/split token #"\.")]
    ;; UNSURE if decodeBase64 returns string
    (json/parse-string (String. (Base64/decodeBase64 payload)) true)))

(defn verify-token-json [json]
  (println [:json json])
  (if-let [decoded
           (and (:id_token json)
                (decode-jwt (:id_token json)))]
    (assoc json :id_token_decoded decoded)
    (throw (ex-info "Malformed OAuth id-token" json))))

(defn request-for-token-endpoint [options code]
  (let [body (ring.util.codec/form-encode
              {"code" code
               "grant_type" "authorization_code"
               "client_id" (:client-id options)
               "client_secret" (:client-secret options)
               "redirect_uri" (:redirect-uri options)
               })]
    {:method :post
     :url (:token_endpoint options)
     :as :json
     :headers {"Content-Type" "application/x-www-form-urlencoded"
               "Accept" "application/json"}
     :body body}))

(defn request-token-for-code [options code]
  (->> code
       (request-for-token-endpoint options)
       http/request
       :body
       verify-token-json))

(defn handle-idp-callback [provider-name options request]
  (let [{state "state" code "code"}  (:params request)
        required-state (get-in request [:session provider-name :state])]
    (print [:state state  required-state])
    (if (= state required-state)
      (if-let [token (request-token-for-code options code)]
        (assoc-in (resp/redirect (second (str/split state #":")))
                  [:session provider-name :credentials] token)
        (throw (ex-info "can't get oauth token!" {})))
      (throw (ex-info "received idp callback with incorrect state"
                      {:expected required-state
                       :actual state})))))


;; wraps a ring handler with something that will check for
;; some credentials, and perform OpenID Connect authentication dance
;; if there are none present
(defn wrap-oidc [handler options]
  (let [provider-name (:provider-name options)
        redirect-uri (:redirect-uri options)
        discovered (discover-configuration options)
        options (validate-configuration discovered)]
    (fn [request]
      (let [creds (get-in request [:session provider-name :credentials])]
        (cond
          (= (:uri request) "favicon.ico")
          (handler request)             ; don't care
          
          ;; already authed, pass through
          creds
          (handler request)
                                               
          ;; handle the post-auth redirect from IdP
          (and (= (.getPath (java.net.URI. redirect-uri)) (:uri request))
               (get-in request [:session provider-name :state]))
          (handle-idp-callback provider-name options request)

          ;; not authed, no auth in progress - begin the oauth dance
          true
          (redirect-to-idp options request))))))

(def test-options
  {
   :discovery-uri "https://accounts.google.com/.well-known/openid-configuration"
   :client-id "12345"
   :provider-name "example"
   :redirect-uri "/oidc/redirect"
   :authorization_endpoint "https://accounts.example.com/o/oauth2/v2/auth"
   })

(deftest redirect-to-idp-test
  (let [h (fn [_] (resp/response "hello world"))
        req {:uri "/proposals" :scheme :https :request-method :get}
        app (-> h (wrap-oidc test-options))
        rsp (app req)]
    (is (= 302 (:status rsp)))
    (is (= "/proposals"
           (second (str/split (-> rsp :session (get "example") :state) #":"))))
    ;; would like to test also that the cookie is httponly/secure
    (is (re-find #"accounts.example.com/o/oauth2/v2/auth"
                 (-> rsp :headers (get "Location"))))))

(deftest auth-in-progress-test
  (let [h (fn [_] (resp/response "hello world"))
        session {"example" {:state "2345678908765432:/proposals/"}}
        req {:uri "/oidc/redirect" :scheme :https
             :request-method :get
             :session session}
        app (-> h (wrap-oidc test-options))
        rsp (app req)]
    (is (nil? rsp))))
