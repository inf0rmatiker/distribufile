#!/bin/bash

PROJECT_DIR=`readlink -f ../`
CONTROLLER_HOST=`cat ../config/controller`
for CHUNKSERVER_HOST in `cat ../config/chunkservers`; do
  echo -e "> Starting Chunk Server on $CHUNKSERVER_HOST..."
  ssh $CHUNKSERVER_HOST  "cd $PROJECT_DIR; nohup java -cp build/libs/distribufile-uber.jar Main --chunkserver $CONTROLLER_HOST > chunkserver_$CHUNKSERVER_HOST.log &" & disown
done
