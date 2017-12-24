(ns adapter.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::hint keyword?)

(def registry (atom {}))

(defn register [& fns]
  (doseq [func fns
          :let [[sym & _ :as spec] (-> func s/get-spec s/form)]
          :when (= sym 'clojure.spec.alpha/fspec)
          :let [[_ _ [_ & args] _ ret _ _] spec]]
    (swap! registry assoc-in [ret (vec (map second (partition 2 args)))]
      func)))

(def hint (some-fn ::hint (comp ::hint meta)))

(defn- find-spec [data]
  (hint data))

(defn- empty-adapter [spec [arg & args]]
  (when (and (empty? args) (s/valid? spec arg))
    identity))

(defn- multi-adapter [spec args]
  (get-in @registry [spec (mapv find-spec args)]))

(defn- not-found [spec args]
  (throw (Exception. (str "Adapter not found: " args "->[" spec "]"))))

(defn- find-adapter [spec args]
  (or (empty-adapter spec args)
      (multi-adapter spec args)
      (not-found     spec args)))

(defn adapt [spec & args]
  (apply (find-adapter spec args) args))
