(ns adapter.core-test
  (:require [clojure.test :refer :all]
            [adapter.core :refer :all]
            [clojure.spec.alpha :as s]))

(s/def ::person (s/keys :req [::first-name ::last-name ::email]))
(s/def ::response (s/keys :req-un [::status ::body]))
(s/fdef person->string :args (s/cat :person ::person) :ret string?)
(s/fdef string->response :args (s/cat :string string?) :ret ::response)

(defn person->string [person]
  (str (::first-name person) " " (::last-name person) ", " (::email person)))

(defn string->response [string]
  {:status 200 :body string})

(register person->string string->response)

(def john {::first-name "John" ::last-name "Smith" ::email "user@sample.org"})

(deftest empty-adapter-test
  (is (= (adapt ::person john) john)))

(deftest person->string-test
  (is (= (adapt string? john) "John Smith, user@sample.org")))

(deftest person-view-test
  (is (= (adapt ::response john) {:status 200 :body "John Smith, user@sample.org"})))
