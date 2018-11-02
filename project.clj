(defproject boost "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]
                 [com.layerware/hugsql "0.4.9"]
                 [ring "1.7.1"]
                 [org.postgresql/postgresql "42.2.2"]
                 [migratus "1.1.6"]]
  :plugins [[migratus-lein "0.6.7"]]
  :main ^:skip-aot boost.core
  :target-path "target/%s"
  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "org.postgressq.Driver"
                  :subprotocol "postgresql"
                  :subname "//localhost/boost"
                  :user "boostuser"
                  :password "boostpw"}}
  :profiles {:uberjar {:aot :all}})
