(ns wilson.spec
  (:require [clojure.spec :as s]
            [wilson.helpers :refer :all]))

(s/def ::n int?)
(s/def ::ups int?)

(s/def ::item (s/keys :req []
                      :opt [::ups ::n]))

(s/def ::up (s/nilable boolean?))
(s/def ::iid string?)

(s/def ::vote (s/keys :req [::up]
                      :opt [::iid]))
