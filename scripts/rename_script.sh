#!/bin/bash

FOLDER=$1
for f in ${FOLDER}/graph*; do mv $f ${f/graph\./graph-transposed.}; done 
