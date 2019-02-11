@ECHO OFF
chcp 65001
java -Dfile.encoding=UTF-8 -Dconfig.file=.\configs\flow.properties -jar flow/target/flow-1.0.0.jar
pause