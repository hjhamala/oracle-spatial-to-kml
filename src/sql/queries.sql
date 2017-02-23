-- name: select-srk
-- Palauttaa kaikki seurakunnat tyypin mukaan 
SELECT * 
    FROM seurakunnat s
    LEFT JOIN seurakunnat_tableau t
        ON s.seurakunta_tunnus = t.SRK_koodi 
        WHERE s.aluetyyppi in (:type);

-- name: select-hpk
-- Palauttaa hiippakunnan
SELECT * 
    FROM seurakunnat s
    LEFT JOIN seurakunnat_tableau t
        ON s.seurakunta_tunnus = t.SRK_koodi 
        WHERE t.HPK_koodi = :hpk_koodi and s.aluetyyppi not in ("02", "03");
        
-- name: select-rovastikunta
-- Palauttaa rovastikunnan
SELECT * 
    FROM seurakunnat s
    LEFT JOIN seurakunnat_tableau t
        ON s.seurakunta_tunnus = t.SRK_koodi 
        WHERE t.RVK_koodi = :rvk_koodi and s.aluetyyppi not in ("02", "03");
        
-- name: insert-seurakunta!
-- Lisää seurakunnan
INSERT INTO seurakunnat
    (`oracle_id`,`seurakunta_nimi`,`seurakunta_tunnus`,`seurakunta_alue`,
    `ordinate_array_info`,`ordinate_array`,`geometry_type`,`aluetyyppi`)
    VALUES
    (:oracle_id,:seurakunta_nimi,:seurakunta_tunnus,:seurakunta_alue,:ordinate_array_info,
    :ordinate_array,:geometry_type,:aluetyyppi);
    
    
-- name: select-distinct-hiippakunta
-- Palauttaa hiippakuntien koodit 
SELECT 
    DISTINCT HPK_koodi, hiippakunta 
        FROM seurakunnat_tableau ;

-- name: select-distinct-rovastikunta
-- Palauttaa rovastikuntien koodit 
SELECT 
    DISTINCT RVK_koodi, rovastikunta, hiippakunta 
        FROM seurakunnat_tableau;
         