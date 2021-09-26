#!/bin/bash

PROJECT_DIR=`readlink -f ../`
CONTROLLER_HOST=`cat ../config/controller`
for hostname in `cat ../config/chunkservers`; do
  echo -e "> Starting Chunk Server on $hostname..."
  ssh $hostname "cd $PROJECT_DIR; java -cp build/libs/distribufile-uber.jar Main --chunkserver --controller=$CONTROLLER_HOST &"
done
