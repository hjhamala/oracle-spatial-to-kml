(ns oracle-spatial-to-kml.core
  (:use [clojure.data.xml])
  (:require [yesql.core :as y]
            [oracle-spatial-to-kml.converter :as c]
            [oracle-spatial-to-kml.oracle-db-conversion :as db]))


(y/defqueries "../src/sql/queries.sql"  
   {:connection db/db-spec})

(defn oracle->wgs84
  [array from to]
  (let [splitted (clojure.string/split array #",")]
    (let [from_array (drop (dec from) splitted)
          to_array (if (= -1 to)
                     from_array 
                     (take (- to from ) from_array))]
      (loop [split to_array result []]
        (if 
          (empty? split) 
          (apply str result)
          (do
            (let [[y x] split 
                  wgs  (c/etr35->wgs84 (read-string x) (read-string y))]
              (recur (drop 2 split) (conj result (str (:longitude wgs) "," (:latitude wgs) ",0 "))))))))))

(defn create-linear-ring
  [coord]
  (element :LinearRing {}
    (element :coordinates {} coord)))

(defn info->array
  [x]
  (let [vals (clojure.string/split x #",")]
    (loop [lvals vals res []]
      (if (empty? lvals)
       res
       (let [[f s t] lvals]
         (recur (drop 3 lvals) (conj res {:start f :type s :interpretation t})))))))


(defn create-inner-bound-coordinates
  [elem_array ordinate_array]
  (loop [lelem_array elem_array res []]
    (let [frs (first lelem_array)]
      (if (or (= "1003" (:type frs)) (empty? lelem_array))
        res
        (recur (rest lelem_array) 
               (conj res (element :LinearRing {}
                           (element :coordinates {} 
                             (oracle->wgs84 ordinate_array (read-string (:start frs )) (if 
                                                                                         (empty? (rest lelem_array))
                                                                                         -1
                                                                                         (read-string (:start (second lelem_array)))))))))))))
(defn 
  create-polygon
  [info ordinate_array]
  (let [info_array (info->array info)]
    (loop [linfo_array info_array  res []]
      (if (empty? linfo_array)
        res
        (let [start (read-string (:start (first linfo_array))) 
              end (if (empty? (rest linfo_array))
                        -1 
                        (read-string (:start (second linfo_array))))
              out_bound_coord (oracle->wgs84 ordinate_array start  end)
              inner_bound_coord (create-inner-bound-coordinates (rest linfo_array) ordinate_array)]
          (do
            (recur (drop (+ 1 (count inner_bound_coord)) linfo_array) (conj res (element :Polygon {}
                                                                           (element :extrude {} 1)
                                                                           (element :altitudeMode {} "relativeToGround")
                                                                           (element :outerBoundaryIs {}
                                                                             (element :LinearRing {}
                                                                               (element :coordinates {} out_bound_coord)))
                                                                           (element :innerBoundaryIs {} inner_bound_coord))))))))))

(defn create-extended-data
  [data]
  (let [fields [:oracle_id :seurakunta_nimi :seurakunta_tunnus :seurakunta_alue :aluetyyppi
                :hiippakunta :hpk_koodi :kunta :kunta_nro :rovastikunta :rvk_koodi :seurakuntatalous :srk_koodi :srkt_koodi]]
    (reduce (fn[res val](conj res (element :Data {:name (name val)}
                                    (element :value {} (val data))) )) [] fields)))

(defn create-kml
  [row]
    (element :Placemark {}
      (element :name {} (:seurakunta_nimi row))
      (element :ExtendedData {} (create-extended-data row))
      (element :MultiGeometry {}
        (create-polygon (:ordinate_array_info row) (:ordinate_array row)))))
  
(defn write-kml
  [kml-data file-name path]
  (clojure.java.io/make-parents (str path "/" file-name ".kml"))
  (with-open [out-file (java.io.FileWriter. (str path "/" file-name ".kml"))]
    (emit (element :kml {} kml-data ) out-file)))


(defn kml-vect
  [rows]
  (loop [rows rows res []]
    (if (empty? rows)
      res
      (let [kml (create-kml (first rows))]
        (recur (rest rows) (conj res kml))))))
 
(defn write-kml-one-file
  "write all rows to one file"
  [rows name path]
  (let [kml (kml-vect rows)]
    (write-kml kml name path)))
     
(defn write-all-hpk
  []
  (doseq [hpk (select-distinct-hiippakunta)]
    (write-kml-one-file (select-hpk hpk) (:hiippakunta hpk) (str "resources/out/" (:hiippakunta hpk) "/"))))  
  

(defn write-all-rovastikunta
  []
  (doseq [rvk (select-distinct-rovastikunta)]
    (write-kml-one-file (select-rovastikunta rvk) (str (:rovastikunta rvk) "_rovastikunta") (str "resources/out/" (:hiippakunta rvk "/")))))  

(defn write-suomenkieliset
  []
  (let [srks (select-srk {:type 1})]
    (doseq [srk srks]
      (write-kml (create-kml srk) (:seurakunta_nimi srk) (str "resources/out/suomenkieliset/")))
    (write-kml-one-file srks "suomenkieliset" (str "resources/out/suomenkieliset/"))))

(defn write-ruotsinkieliset
  []
  (let [srks (select-srk {:type 4})]
    (doseq [srk srks]
      (write-kml (create-kml srk) (:seurakunta_nimi srk) (str "resources/out/ruotsinkieliset/")))
    (write-kml-one-file srks "ruotsinkieliset" (str "resources/out/ruotsinkieliset/"))))


(defn write-all 
  []
  (write-all-hpk)
  (write-all-rovastikunta)
  (write-ruotsinkieliset)
  (write-suomenkieliset))
  