(ns wilson.database.items
  (:require [rethinkdb.query :as r]
            [wilson.helpers :refer :all]
            [wilson.database.common :refer [created-timestamped timestamped]]))

(defn save-item! [item]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (let [item (created-timestamped item)
          {[iid] :generated_keys}
          (-> (r/db "wilson")
              (r/table "items")
              (r/insert item)
              (r/run conn))]
      (assoc item :id iid))))

(defn patch-item! [item]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (let [item (timestamped item)]
      (-> (r/db "wilson")
        (r/table "items")
        (r/insert item {:conflict :replace
                        :durability :hard})
        (r/run conn)))))

(defn fetch-items []
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (-> (r/db "wilson")
        (r/table "items")
        (r/get-field :id)
        (r/run conn))))

(defn fetch-item [iid]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (-> (r/db "wilson")
        (r/table "items")
        (r/get iid)
        (r/run conn))))

(defn remove-item! [iid]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (-> (r/db "wilson")
      (r/table "items")
      (r/get iid)
      (r/delete {:durability :hard
                 :return-changes false})
      (r/run conn))))
