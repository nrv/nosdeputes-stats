nosdeputes-stats
================

Quelques outils pour sortir des statistiques à partir du dump MySQL du site nosdeputes.fr

Le code SQL est ultrabasique et non optimisé

- téléchargez le dump mysql ici : http://www.regardscitoyens.org/telechargement/donnees/nosdeputes.fr/
- créez une base nosdeputes (avec un compte nosdeputes/nosdeputes)
- importez le dump
mysql nosdeputes --max_allowed_packet=64M -u nosdeputes --password=nosdeputes < data.sql
- modifiez éventuellement le fichier nosdeputes.properties
- lancez la classe TestNosDeputes sans oublier d'ajouter le fichier jar des drivers MySQL (type mysql-connector-java-5.1.18-bin.jar) au classpath


