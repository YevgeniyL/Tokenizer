@ECHO OFF
chcp 65001
java -Dfile.encoding=UTF-8 -Dconfig.file=.\configs\proof.properties -jar proof/target/proof-1.0.0.jar
pause