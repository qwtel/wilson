(ns wilson.handler
  (:require [clojure.spec :as s]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [rethinkdb.query :as r]
            [wilson.helpers :refer :all]
            [wilson.score :refer :all]
            [wilson.spec :as ws]
            [wilson.resources.items :refer :all]
            [wilson.resources.votes :refer :all]))

(defn- post-setup! [_]
  (try
    (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
      (r/run (r/db-create "wilson") conn)
      (-> (r/db "wilson")
          (r/table-create "items")
          (r/run conn))
      (-> (r/db "wilson")
          (r/table-create "votes")
          (r/run conn))
      (-> (r/db "wilson")
          (r/table "votes")
          (r/index-create "iid" (r/fn [row]
                                  (r/get-field row ::ws/iid)))
          (r/run conn))
      {:status 201 :body "Setup complete"})
    (catch Exception e
      (.printStackTrace e)
      {:status 500 :body (format "IOException: %s" (.getMessage e))})))

(defroutes app-routes
  (GET   ["/items"]                            []    get-items)
  (POST  ["/items"]                            []    post-items!)
  (GET   ["/items/:iid" :iid uuid-regex]       [iid] get-item)
  (GET   ["/items/:iid/votes" :iid uuid-regex] [iid] get-votes)
  (POST  ["/items/:iid/votes" :iid uuid-regex] [iid] post-votes!)
  (GET   ["/votes/:vid" :vid uuid-regex]       [vid] get-vote)

  ;; TODO: remove
  (POST  ["/setup"] [] post-setup!)

  (route/not-found "Not Found!?"))

(def app
  (-> app-routes
    (wrap-json-body {:keywords? #(keyword "wilson.spec" %)})
    (wrap-json-response {:key-fn name})))
