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
   ::ws/created schema/Str
   ::ws/updated schema/Str
   :id schema/Str})

(schema/defschema NewVote
  {::ws/up schema/Bool})

(schema/defschema Vote
  {::ws/up schema/Bool
   ::ws/created schema/Str
   ::ws/updated schema/Str
   ::ws/iid schema/Str
   :id schema/Str})
