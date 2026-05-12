#!/bin/bash

echo "Ce script utilise le Jar généré par le dernier \"mvn install\"".

java -jar target/Client.jar "$@"

exit 0

