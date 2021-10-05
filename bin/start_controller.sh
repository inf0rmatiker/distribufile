#!/bin/bash

PROJECT_DIR=`readlink -f ../`
CONTROLLER_HOST=`cat ../config/controller`
echo -e "> Starting Controller on $CONTROLLER_HOST..."
ssh $CONTROLLER_HOST "cd $PROJECT_DIR; nohup java -cp build/libs/distribufile-uber.jar Main --controller > controller_$CONTROLLER_HOST.log &" & disown
