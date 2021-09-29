#!/bin/bash

for hostname in `cat ../config/chunkservers`; do
  echo -e "> Removing all artifacts for Chunk Server on $hostname..."
  ssh $hostname "rm -rf /tmp/s"
done
