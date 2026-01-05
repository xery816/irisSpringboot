#!/bin/bash
echo "Starting Iris Recognition Service on Linux..."

export JAVA_LIBRARY_PATH=/opt/iris_sdk/lib

mvn spring-boot:run -Djava.library.path=$JAVA_LIBRARY_PATH

