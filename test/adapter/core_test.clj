(ns adapter.core-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [adapter.core :as adapter]))

(s/def ::text       string?)
(s/def ::person     (s/keys :req [::first-name ::last-name ::email]))
(s/def ::specialist (s/keys :req [::occupation]))
(s/def ::response   (s/keys :req [::status ::body]))

(s/fdef person->text
  :args (s/cat :person ::person)
  :ret ::text)

(s/fdef person+specialist
  :args (s/cat :person ::person :specialist ::specialist)
  :ret ::person)

(s/fdef text->response
  :args (s/cat :text ::text)
  :ret ::response)

(def john {::first-name "John" ::last-name "Smith" ::email "user@sample.org"})
(def john* (with-meta john {::adapter/hint ::person}))
(def john** (assoc john ::adapter/hint ::person))

(def engineer {::occupation :engineer})
(def engineer* (with-meta engineer {::adapter/hint ::specialist}))

(defn person->text [person]
  (str (::first-name person) " "
       (::last-name  person) ", "
       (::email      person)))

(defn person+specialist [person specialist]
  (merge person specialist))

(defn text->response [text]
  {:status  200
   :body    text})

(use-fixtures :once
  (fn [tests]
    (adapter/register #'person->text #'person+specialist #'text->response)
    (tests)))

(deftest empty-adapters
  (is (= (adapter/adapt ::person john) john)))

(deftest simple-adapters
  (is (= (adapter/adapt ::text john*)
         "John Smith, user@sample.org")))

(deftest multi-adapters
  (is (= (adapter/adapt ::person john* engineer*)
         {::first-name "John"
          ::last-name "Smith"
          ::email "user@sample.org"
          ::occupation :engineer})))

(comment
  (deftest person->response-test
    (is (= (adapter/adapt ::response john)
           {:status 200 :body "John Smith, user@sample.org"}))))
