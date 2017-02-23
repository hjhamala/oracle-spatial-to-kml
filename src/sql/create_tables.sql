CREATE TABLE `seurakunnat` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `oracle_id` int(11) DEFAULT NULL,
  `seurakunta_nimi` varchar(100) DEFAULT NULL,
  `seurakunta_tunnus` char(8) DEFAULT NULL,
  `seurakunta_alue` varchar(100) DEFAULT NULL,
  `ordinate_array_info` text,
  `ordinate_array` longtext,
  `geometry_type` int(11) DEFAULT NULL,
  `aluetyyppi` char(2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=445 DEFAULT CHARSET=latin1;
CREATE TABLE `seurakunnat_tableau` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Hiippakunta` char(25) DEFAULT NULL,
  `HPK_koodi` char(6) DEFAULT NULL,
  `Kunta` char(255) DEFAULT NULL,
  `Kunta_nro` char(255) DEFAULT NULL,
  `Rovastikunta` char(255) DEFAULT NULL,
  `RVK_koodi` char(6) DEFAULT NULL,
  `Seurakunta` char(255) DEFAULT NULL,
  `Seurakuntatalous` char(255) DEFAULT NULL,
  `SRK_koodi` char(6) DEFAULT NULL,
  `SRKT_koodi` char(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=432 DEFAULT CHARSET=latin1;



