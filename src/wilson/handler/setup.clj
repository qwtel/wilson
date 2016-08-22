(ns wilson.handler.setup
  (:require [ring.util.http-response :refer :all]
            [rethinkdb.query :as r]
            [wilson.common :refer :all]
            [wilson.database.setup :as db]))

(defn setup! []
  (db/setup!)
  (ok "Setup complete"))
