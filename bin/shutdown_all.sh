#!/bin/bash

PROJECT_DIR=`readlink -f ../`
CONTROLLER_HOST=`cat ../config/controller`
ssh $CONTROLLER_HOST "kill \$(ps aux | jps | grep Main | awk '{print \$1}')" && echo "Successfully killed Controller" || echo "Unable to kill Controller"
for CHUNKSERVER_HOST in `cat ../config/chunkservers`; do
  echo -e "> Killing Chunk Server on $CHUNKSERVER_HOST..."
  ssh $CHUNKSERVER_HOST "kill \$(ps aux | jps | grep Main | awk '{print \$1}')" && echo "Successfully killed Chunk Server on $CHUNKSERVER_HOST" || echo "Unable to kill Chunk Server on $CHUNKSERVER_HOST"
done
