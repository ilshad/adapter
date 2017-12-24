(ns adapter.core-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [adapter.core :as adapter]))

(s/def ::person (s/keys :req [::first-name ::last-name ::email]))
(s/def ::text string?)
(s/def ::response (s/keys :req-un [::status ::body]))
(s/fdef person->text :args (s/cat :person ::person) :ret ::text)
(s/fdef text->response :args (s/cat :text ::text) :ret ::response)

(defn person->text [person]
  (str (::first-name person) " "
       (::last-name  person) ", "
       (::email      person)))

(defn text->response [text]
  {:status  200
   :body    text})

(def john
  {::first-name  "John"
   ::last-name   "Smith"
   ::email       "user@sample.org"})

(use-fixtures :once
  (fn [tests]
    (adapter/register #'person->text #'text->response)
    (tests)))

(deftest empty-adapter-test
  (is (= (adapter/adapt ::person john) john)))

(deftest person->text-test
  (is (= (adapter/adapt ::text (with-meta john {::adapter/hint ::person}))
         "John Smith, user@sample.org")))

(comment
  (deftest person->response-test
    (is (= (adapter/adapt ::response john)
           {:status 200 :body "John Smith, user@sample.org"}))))
