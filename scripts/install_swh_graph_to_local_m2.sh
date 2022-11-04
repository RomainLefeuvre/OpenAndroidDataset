#!/bin/bash
git clone https://github.com/RomainLefeuvre/swh-graph
cd swh-graph/java/
mvn clean install --no-transfer-progress
cd ../../
rm -r -f ./swh-graph/
mvn install:install-file -Dfile=dsiutils/dsiutils-2.7.3-SNAPSHOT.jar -DpomFile=dsiutils/pom.xml
