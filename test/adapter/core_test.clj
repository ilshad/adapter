(ns adapter.core-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [adapter.core :as adapter]))

(s/def ::contact  (s/keys :req [::email ::adress]))
(s/def ::person   (s/keys :req [::first-name ::last-name]))
(s/def ::user     (s/keys :reg [::email ::address ::first-name ::last-name]))
(s/def ::response (s/keys :req-un [::status ::body]))

(s/fdef contact+person->user
  :args (s/cat :contact ::contact
               :person ::person)
  :ret ::user)

(s/fdef person->user
  :args (s/cat :person ::person)
  :ret ::user)

(s/fdef user->response
  :args (s/cat :user ::user)
  :ret ::response)

(defn contact+person->user [contact person]
  (merge contact person))

(defn person->user [person]
  (assoc person ::email "" ::address ""))

(defn user->response [user]
  {:status 200
   :body (str (::first-name user) " "
              (::last-name  user) ", "
              (::email      user))})

(def contact {::email "john@sample.org" ::address "NY"})
(def contact* (with-meta contact {::adapter/hint ::contact}))

(def person {::first-name "John" ::last-name "Smith"})
(def person* (with-meta person {::adapter/hint ::person}))
(def person** (assoc person ::adapter/hint ::person))

(def user {::first-name "John"
           ::last-name  "Smith"
           ::email      "john@sample.org"
           ::address    "NY"})

(def user* (with-meta user {::adapter/hint ::user}))

(use-fixtures :once
  (fn [tests]
    (adapter/register #'contact+person->user
                      #'person->user
                      #'user->response)
    (tests)))

(deftest empty-adapter
  (is (= (adapter/adapt ::person person*) person*)))

(deftest direct-single-adapter
  (is (= (adapter/adapt ::response user*)
         {:status 200 :body "John Smith, john@sample.org"})))

(deftest direct-multi-adapters
  (is (= (adapter/adapt ::user contact* person*)
         {::first-name "John"
          ::last-name "Smith"
          ::email "john@sample.org"
          ::address "NY"})))

(deftest transitive-adapters
  (is (= (adapter/adapt ::response person*)
         {:status 200 :body "John Smith, "}))
  (comment (is (= (adapter/adapt ::response contact* person*)
                  {:status 200 :body "John Smith, john@sample.org"}))))
