drop table if exists `TBL_PERSONEN`;
CREATE TABLE `TBL_PERSONEN` (
        `id` uuid NOT NULL,
        `nachname` varchar(30) NOT NULL,
        `vorname` varchar(30) DEFAULT NULL,
        PRIMARY KEY (`id`)
) ;