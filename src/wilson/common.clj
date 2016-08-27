(ns wilson.common
  (:require [clojure.java.io :as io]
            [ring.util.http-response :refer :all]
            [clout.core :as clout]
            [rethinkdb.query :as r])
  (:import java.net.URL))

(def base-url "")

; (defn route-matches [route uri]
;   (let [path (.getRawPath (java.net.URI. uri))]
;     (clout/route-matches route {:uri path})))
;
; (route-matches "/items/:iid" "/items/123")
; (route-matches "/items/:iid" "http://localhost:3000/items/123")
; (route-matches "/items/:iid" "http://127.0.0.1/items/123")
; (route-matches "/items/:iid" "//127.0.0.1/items/123")
; (route-matches "/items/:iid" "//localhost/items/123")
; (route-matches "/items/:iid" "//qwtel.com/items/123")
; (route-matches "/items/:iid" "qwtel.com/items/123")

(defn uuid
  ([] (java.util.UUID/randomUUID))
  ([s] (some-> s java.util.UUID/fromString)))

(defn date
  ([] (java.util.Date.)))

(def uuid-regex #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

; (defn float= [eps x y]
;   (<= (Math/abs (- x y)) eps))
;
; (def close? (partial float= 0.01))
