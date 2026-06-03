#!/bin/bash

rm -rf bin
mkdir -p bin

javac --class-path "lib/*" -d bin src/main/java/com/autosur/models/*.java src/main/java/com/autosur/database/*.java src/main/java/com/autosur/dao/*.java src/main/java/com/autosur/controllers/*.java src/main/java/com/autosur/Main.java

mkdir -p bin/views bin/css
cp src/main/resources/views/* bin/views/
cp src/main/resources/css/* bin/css/

java --module-path "lib" --add-modules javafx.controls,javafx.fxml,org.slf4j --class-path "bin:lib/*" com.autosur.Main