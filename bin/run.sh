#!/bin/bash

function print_usage() {
  echo -e "\nUsage: ./run.sh [--cleanup] [--system] [--client [OPTIONS]]\n"
}

case $1 in

  "--cleanup")
    ./clean_artifacts.sh
    ;;

  "--system")
    ./start_controller.sh
    ./start_chunkservers.sh
    ;;

  "--client")
    shift
    echo -e "Launching client $@"
    ;;

  "-h" | "--h" | *)
    print_usage
    ;;

esac
