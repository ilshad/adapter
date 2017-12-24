(ns adapter.core-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [adapter.core :as adapter]))

(s/def ::contact    (s/keys :req [::email]))
(s/def ::person     (s/keys :req [::first-name ::last-name ::email]))
(s/def ::specialist (s/keys :req [::occupation]))
(s/def ::response   (s/keys :req-un [::status ::body]))

(s/fdef contact->person
  :args (s/cat :contact ::contact)
  :ret ::person)

(s/fdef person->response
  :args (s/cat :person ::person)
  :ret ::response)

(s/fdef person+specialist
  :args (s/cat :person ::person :specialist ::specialist)
  :ret ::person)

(defn contact->person [contact]
  (assoc contact
    ::first-name "First Name"
    ::last-name "Last Name"))

(defn person->response [person]
  {:status 200
   :body (str (::first-name person) " "
              (::last-name  person) ", "
              (::email      person))})

(defn person+specialist [person specialist]
  (merge person specialist))

(def anonym {::email "anonym@sample.org"})
(def anonym* (with-meta anonym {::adapter/hint ::contact}))

(def john {::first-name "John" ::last-name "Smith" ::email "john@sample.org"})
(def john* (with-meta john {::adapter/hint ::person}))
(def john** (assoc john ::adapter/hint ::person))

(def engineer {::occupation :engineer})
(def engineer* (with-meta engineer {::adapter/hint ::specialist}))

(use-fixtures :once
  (fn [tests]
    (adapter/register #'contact->person
                      #'person->response
                      #'person+specialist)
    (tests)))

(deftest empty-adapters
  (is (= (adapter/adapt ::person john) john)))

(deftest simple-adapters
  (is (= (adapter/adapt ::person anonym*)
         {::email "anonym@sample.org"
          ::first-name "First Name"
          ::last-name "Last Name"}))
  (is (= (adapter/adapt ::response john*)
         {:status 200 :body "John Smith, john@sample.org"})))

(deftest multi-adapters
  (is (= (adapter/adapt ::person john* engineer*)
         {::first-name "John"
          ::last-name "Smith"
          ::email "john@sample.org"
          ::occupation :engineer})))

(comment
  (deftest complex-path-adapters
    (is (= (adapter/adapt ::response john*)
           {:status 200 :body "John Smith, john@sample.org"}))))
