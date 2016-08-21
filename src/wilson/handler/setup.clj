(ns wilson.handler.setup
  (:require [ring.util.http-response :refer :all]
            [rethinkdb.query :as r]
            [wilson.spec :as ws]
            [wilson.helpers :refer :all]))

(defn setup! []
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
    (ok "Setup complete")))
