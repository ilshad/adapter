(ns adapter.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::hint keyword?)

(def registry (atom {}))

(defn register [& fns])

(def hint (some-fn ::hint (comp ::hint meta)))

(defn- find-spec [data]
  (hint data))

(defn- empty-adapter [spec [arg & args]]
  (when (and (empty? args) (s/valid? spec arg))
    identity))

(defn- multi-adapter [spec args]
  (get-in @registry [spec (mapv find-spec args)]))

(defn- error-adapter [spec args]
  (throw (Exception. (str "Adapter not found: " args "->[" spec "]"))))

(defn- find-adapter [spec args]
  (or (empty-adapter spec args)
      (multi-adapter spec args)
      (error-adapter spec args)))

(defn adapt [spec & args]
  (apply (find-adapter spec args) args))
