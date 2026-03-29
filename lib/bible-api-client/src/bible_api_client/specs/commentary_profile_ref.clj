(ns bible-api-client.specs.commentary-profile-ref
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.commentary-profile-ref-reference :refer :all]
            )
  (:import (java.io File)))


(def commentary-profile-ref-data
  {
   (ds/req :id) string?
   (ds/req :subject) string?
   (ds/opt :reference) commentary-profile-ref-reference-spec
   (ds/req :thisProfileLink) string?
   (ds/opt :referenceChapterLink) string?
   })

(def commentary-profile-ref-spec
  (ds/spec
    {:name ::commentary-profile-ref
     :spec commentary-profile-ref-data}))
