(ns wilson.resources.items
  (:require [clojure.spec :as s]
            [rethinkdb.query :as r]
            [wilson.helpers :refer :all]
            [wilson.score :refer :all]
            [wilson.spec :as ws]))

(defn- with-time [x]
  (let [now (date)]
    (merge x {::ws/created now
              ::ws/updated now})))

(def item-default-values {::ws/ups 0
                          ::ws/n 0})

(defn- prep-item [parsed]
  (with-time
    (merge item-default-values parsed)))

(defn- recalc-scores [item]
  (let [{:keys [::ws/ups ::ws/n]} item]
    (assoc item ::ws/score   (- n ups)
                ::ws/wilson  (not-average ups n))))

(defn update-item
  "Takes an item and a vote and updates the scored accordingly."
  [item vote]
  (let [ups   (+ (::ws/ups item)
                 (tf->10 (::ws/up vote)))
        n     (+ (::ws/n item)
                 (tf->10 (some? (::ws/up vote))))
        item' (assoc item ::ws/ups ups
                          ::ws/n n
                          ::ws/updated (date))]
    (recalc-scores item')))

(defn post-items! [{:keys [body]}]
  (let [parsed (s/conform ::ws/item body)]
    (if (= parsed ::s/invalid)
       {:status 400 :body (s/explain-data ::ws/item body)}
       (let [item (-> parsed (prep-item) (recalc-scores))]
         (try
           (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
             (let [{[iid] :generated_keys}
                   (-> (r/db "wilson")
                     (r/table "items")
                     (r/insert item)
                     (r/run conn))]
               {:status 201 :body (assoc item :id iid)}))
           (catch Exception e
             (.printStackTrace e)
             {:status 500 :body (format "IOException: %s" (.getMessage e))}))))))

(defn get-items [_]
  (try
    (let [items
          (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
            (-> (r/db "wilson")
              (r/table "items")
              (r/get-field :id)
              (r/run conn)))]
      {:status 200 :body items})
    (catch Exception e
      (.printStackTrace e)
      {:status 500 :body (format "IOException: %s" (.getMessage e))})))

(defn get-item [{{:keys [iid]} :params}]
  (try
    (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
      (let [item (-> (r/db "wilson")
                   (r/table "items")
                   (r/get iid)
                   (r/run conn))]
        (if (some? item)
          {:status 200 :body item}
          {:status 404})))
    (catch Exception e
      (.printStackTrace e)
      {:status 500 :body (format "IOException: %s" (.getMessage e))})))
