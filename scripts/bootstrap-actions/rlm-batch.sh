#!/bin/bash
set -e
sudo rm -f /home/hadoop/lib/jackson-core-asl-*.jar
sudo rm -f /home/hadoop/lib/jackson-mapper-asl-*.jar
sudo wget -S -T 10 -t 5 https://s3.amazonaws.com/isell.demo/libs/jackson-mapper-asl-1.9.3.jar
sudo wget -S -T 10 -t 5 https://s3.amazonaws.com/isell.demo/libs/jackson-core-asl-1.9.3.jar
sudo cp -f jackson-mapper-asl-1.9.3.jar /home/hadoop/lib
sudo cp -f jackson-core-asl-1.9.3.jar /home/hadoop/lib

