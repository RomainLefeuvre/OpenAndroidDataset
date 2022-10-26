#!/bin/bash
git clone https://github.com/RomainLefeuvre/swh-graph
cd swh-graph/java/
mvn clean install
cd ../../
rm -r -f ./swh-graph/
