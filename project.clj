(defproject wilson "0.1.0-SNAPSHOT"
  :description "How not to sort by average rating."
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-logger "0.7.6"]
                 [compojure "1.5.1"]
                 [metosin/compojure-api "1.1.6"]
                 [metosin/ring-http-response "0.8.0"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [rm-hull/infix "0.2.9"]
                 [com.apa512/rethinkdb "0.15.26"]
                 [prismatic/schema "1.1.3"]
                 [environ "1.1.0"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.1.0"]]
  :ring {:handler wilson.handler/app}
  :aliases {"setup" ["run" "-m" "wilson.database.setup"]}
  :profiles
  {:dev {:env {:db-url "//localhost:28015"
               :db-name "test"}
         :dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]
                        [proto-repl "0.3.1"]]}})
