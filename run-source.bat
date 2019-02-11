@ECHO OFF
chcp 65001
java -Dfile.encoding=UTF-8 -Dconfig.file=.\configs\source.properties -jar source/target/source-1.0.0.jar
pause