(ns wilson.schema
  (:require [schema.core :as s]
            [wilson.spec :as ws]))

(s/defschema NewItem
  {(s/optional-key ::ws/n) s/Int
   (s/optional-key ::ws/ups) s/Int})

(s/defschema Item
  {::ws/n s/Int
   ::ws/ups s/Int
   ::ws/score s/Num
   ::ws/wilson s/Num
   ::ws/created java.util.Date
   ::ws/updated java.util.Date
   :id s/Str})

(s/defschema NewVote
  {::ws/up (s/maybe s/Bool)})

(s/defschema Vote
  {::ws/up (s/maybe s/Bool)
   ::ws/iid s/Str
   ::ws/created java.util.Date
   ::ws/updated java.util.Date
   :id s/Str})
