#!/bin/bash  
set -e  
while read line  
do  
  aws s3 cp --no-sign-request s3://softwareheritage/graph/2022-04-25/compressed/$line .
done <filename.txt
