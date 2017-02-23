(ns oracle-spatial-to-kml.converter)

(defn atanh
  [^double value]
	(/ (Math/log (/ (+ (/ 1 value) 1) (- (/ 1 value) 1))) 2))

	   
(defn asinh
  [^double value] 
	(Math/log (+ value (Math/sqrt(inc (* value value))))))

(defn etr35->wgs84
  [x y]
  (let [ca   6378137.0
        cf   (/ 1.0 298.257223563)
        ck0  0.9996      
        clo0 (Math/toRadians 27.0)          
        ce0  500000.0
        cn   (/ cf (- 2.0 cf))
        ca1  (* (/ ca (+ 1.0 cn)) (+ 1.0 (/ (Math/pow cn 2.0) 4.0) (/ (Math/pow cn 4.0) 64.0 )))	 
        ce   (Math/sqrt (- (* 2.0 cf) (Math/pow cf 2.0)))
        ch1  (-> (* (/ 1.0  2.0) cn) 
            (- (* (/ 2.0  3.0) (Math/pow cn 2.0))) 
            (+ (* (/ 37.0  96.0) (Math/pow cn 3.0))) 
            (- (* (/ 1.0 360.0) (Math/pow cn 4.0)))) 
        ch2  (-> (* (/ 1.0  48.0) (Math/pow cn 2.0)) 
           (+ (* (/ 1.0 15.0) (Math/pow cn 3.0)))
           (- (* (/ 437.0 1440.0) (Math/pow cn, 4.0))))
	   
        ch3  (-> (* (/ 17.0 480.0) (Math/pow cn 3.0))
            (- (* (/ 37.0 840.0) (Math/pow cn 4.0))))
        ch4  (* (/ 4397.0 161280.0) (Math/pow cn 4.0))
        e    (/ x (* ca1 ck0))

        nn   (/ (- y ce0) (* ca1  ck0))
        e1p  (* ch1 (Math/sin (* 2.0 e)) (Math/cosh (* 2.0 nn)))
        e2p  (* ch2 (Math/sin(* 4.0 e)) (Math/cosh (* 4.0 nn)))
        e3p  (* ch2 (Math/sin(* 6.0 e)) (Math/cosh (* 6.0 nn)))
        e4p  (* ch3 (Math/sin(* 8.0 e)) (Math/cosh (* 8.0 nn)))
        nn1p  (* ch1 (Math/cos(* 2.0 e)) (Math/sinh (* 2.0 nn)))
        nn2p  (* ch2 (Math/cos(* 4.0 e)) (Math/sinh (* 4.0 nn)))
        nn3p  (* ch3 (Math/cos (* 6.0 e)) (Math/sinh (* 6.0 nn)))
        nn4p  (* ch4 (Math/cos(* 8.0 e)) (Math/sinh (* 8.0 nn)))
        ep    (- e e1p e2p e3p e4p)
        
        nnp   (- nn nn1p nn2p nn3p nn4p)
        be    (Math/asin (/ (Math/sin ep) (Math/cosh nnp)))
        
        q     (asinh (Math/tan be))
        qp1 (+ q (* ce (atanh (* ce (Math/tanh q)))))
        qp2 (+ q (* ce (atanh (* ce (Math/tanh qp1)))))
        qp3 (+ q (* ce (atanh (* ce (Math/tanh qp2)))))
        qp  (+ q (* ce (atanh (* ce (Math/tanh qp3)))))]
      {:latitude  (Math/toDegrees (Math/atan(Math/sinh qp)))
       :longitude (Math/toDegrees (+ clo0 (Math/asin (/ (Math/tanh nnp) (Math/cos be)))))}))
        
 


