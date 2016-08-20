(defproject wilson "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha10"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [rm-hull/infix "0.2.9"]
                 [com.apa512/rethinkdb "0.15.26"]
                 [metosin/compojure-api "1.1.6"]
                 [metosin/ring-http-response "0.8.0"]
                 [prismatic/schema "1.1.3"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler wilson.handler/app
         :nrepl   {:start? true}}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]
                        [proto-repl "0.3.1"]]}})
