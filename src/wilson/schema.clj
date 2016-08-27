(ns wilson.schema
  (:require [schema.core :as s]))

(s/defschema NewItem
  {(s/optional-key :n) s/Int
   (s/optional-key :ups) s/Int})

(s/defschema Item
  {:n s/Int
   :ups s/Int
   :score s/Num
   :wilson s/Num
   :created java.util.Date
   :updated java.util.Date
   :id s/Str})

(s/defschema NewVote
  {:up (s/maybe s/Bool)})

(s/defschema Vote
  {:up (s/maybe s/Bool)
   :iid s/Str
   :created java.util.Date
   :updated java.util.Date
   :id s/Str})
