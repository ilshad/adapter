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

(defn- direct [return args]
  (get-in @registry [return args]))

(defn- arity= [arity]
  (fn [[args _]]
    (= (count args) arity)))

(defn- find-path [from to]
  [#'adapter.core-test/person->user]) ;; dummy

(defn- apply-chain [chain x]
  (reduce (fn [result f] (apply f result)) x chain))

(defn- convergent-chain [chains final]
  (fn [args]
    (prn "applied args" args)
    (apply final (map apply-chain chains args))))

(defn- transitive-for-each-arg [args-from]
  (fn [[args-to final]]
    (let [chains (filter identity (map find-path args-from args-to))]
      {:count (inc (count chains))
       :fn (convergent-chain chains final)})))

(defn transitive [return args]
  (->> (get @registry return)
       (filter (arity= (count args)))
       (map (transitive-for-each-arg args))
       concat
       (sort-by :count)
       first
       :fn))

(defn- not-found [return args]
  (throw (ex-info "Adapter not found" {:from (vec args) :to return})))

(defn- adapter [return args]
  (or (ident      return args)
      (direct     return args)
      (transitive return args)
      (not-found  return args)))

(defn adapt [spec & args]
  (apply (adapter spec (map find-spec args)) args))
