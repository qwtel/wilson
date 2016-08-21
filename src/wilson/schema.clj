(ns wilson.schema
  (:require [schema.core :as schema]
            [wilson.spec :as ws]))

(schema/defschema NewItem
  {(schema/optional-key ::ws/n) schema/Int
   (schema/optional-key ::ws/ups) schema/Int})

(schema/defschema Item
  {::ws/n schema/Int
   ::ws/ups schema/Int
   ::ws/score schema/Num
   ::ws/wilson schema/Num
   ::ws/created java.util.Date
   ::ws/updated java.util.Date
   :id schema/Str})

(schema/defschema NewVote
  {::ws/up (schema/maybe schema/Bool)})

(schema/defschema Vote
  {::ws/up (schema/maybe schema/Bool)
   ::ws/iid schema/Str
   ::ws/created java.util.Date
   ::ws/updated java.util.Date
   :id schema/Str})
