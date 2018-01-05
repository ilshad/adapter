(ns adapter.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::hint keyword?)

(defonce registry (atom {}))

(defn register [& fns]
  (doseq [func fns
          :let [[sym & _ :as spec] (-> func s/get-spec s/form)]
          :when (= sym 'clojure.spec.alpha/fspec)
          :let [[_ _ [_ & args] _ ret _ _] spec]]
    (swap! registry assoc-in [ret (->> args (partition 2) (map second) vec)]
      func)))

(defn- find-spec [data]
  (let [finder (some-fn ::hint (comp ::hint meta))]
    (finder data)))

(defn- ident [return [arg & args]]
  (when (and (empty? args) (= return arg))
    identity))

(defn- find-direct [return args]
  (get-in @registry [return args]))

(defn- not-found [return args]
  (throw (ex-info "Adapter not found" {:from (vec args) :to return})))

(defn- find-adapter [return args]
  (or (ident           return args)
      (find-direct     return args)
      (not-found       return args)))

(defn adapt [spec & args]
  (let [adapter (find-adapter spec (map find-spec args))]
    (apply adapter args)))
