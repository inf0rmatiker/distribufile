#!/bin/bash

PROJECT_DIR=`readlink -f ../`
CONTROLLER_HOST=`cat ../config/controller`
echo -e "> Starting Controller on $CONTROLLER_HOST..."
ssh $hostname "cd $PROJECT_DIR; java -cp build/libs/distribufile-uber.jar Main --controller &"
