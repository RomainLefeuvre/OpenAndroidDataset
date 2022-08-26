#!/bin/bash  
set -e  
while read line  
do  
  aws s3 cp --no-sign-request s3://softwareheritage/$line .  
done <filename.txt
