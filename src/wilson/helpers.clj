(ns wilson.helpers
  (:require [clojure.java.io :as io])
  (:import java.net.URL))

(defn uuid
  ([] (java.util.UUID/randomUUID))
  ([s] (some-> s java.util.UUID/fromString)))

(defn date
  ([] (java.util.Date.)))

(def uuid-regex #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

(defn tf->10
  "Maps true/false to 1/0"
  [e]
  (if (true? e) 1 0))
