(ns adapter.core-test
  (:require [clojure.test :refer :all]
            [adapter.core :refer :all]
            [clojure.spec.alpha :as s]))

(s/def ::person (s/keys :req [::first-name ::last-name ::email]))
(s/def ::text string?)
(s/def ::response (s/keys :req-un [::status ::body]))
(s/fdef person->text :args (s/cat :person ::person) :ret ::text)
(s/fdef text->response :args (s/cat :text ::text) :ret ::response)

(defn person->text [person]
  (str (::first-name person) " " (::last-name person) ", " (::email person)))

(defn text->response [text]
  {:status 200 :body text})

(register #'person->text #'text->response)

(def john {::first-name "John" ::last-name "Smith" ::email "user@sample.org"})

(deftest empty-adapter-test
  (is (= (adapt ::person john) john)))

(deftest person->text-test
  (is (= (adapt ::text (with-meta john {::hint ::person}))
         "John Smith, user@sample.org")))

(comment (deftest person->response-test
           (is (= (adapt ::response john) {:status 200 :body "John Smith, user@sample.org"}))))
