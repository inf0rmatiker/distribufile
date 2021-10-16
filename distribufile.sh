#!/bin/bash

function print_usage() {
  echo -e "\nUSAGE\n\tdistribufile.sh [OPTIONS]\n"
  echo -e "OPTIONS\n"
  echo -e "\t--cleanup\t\t\tRemove all artifacts/chunks stored on all Chunk Servers in cluster\n"
  echo -e "\t--shutdown\t\t\tShuts down all Chunk Servers first, then the Controller\n"
  echo -e "\t--chunkservers\t\t\tStarts the Chunk Servers\n"
  echo -e "\t--controller\t\t\tStarts the Controller\n"
  echo -e "\t--client-read <file> <out>\tReads a file specified by <file> and outputs it as <out> file\n"
  echo -e "\t--client-write <file>\t\tWrites a file specified by <file> to the distributed filesystem\n"
  echo -e "\t--status\t\t\tRetrieves a diagnostic report of the system from the Controller\n"
}

function client_read {
  [ $# -ne 2 ] && echo "Provide the absolute path of the filename you wish to read, and output filename" && exit 1
  CONTROLLER_HOST=`cat ./config/controller`
  READ_FILENAME=$1
  OUT_FILENAME=$2
  java -cp $JAR_PATH Main --client-read $CONTROLLER_HOST $READ_FILENAME $OUT_FILENAME
}

function client_write {
  [ $# -ne 1 ] && echo "Provide the absolute path of the filename you wish to write" && exit 1
  CONTROLLER_HOST=`cat ./config/controller`
  WRITE_FILENAME=$1
  java -cp $JAR_PATH Main --client-write $CONTROLLER_HOST $WRITE_FILENAME
}

function client_status {
  CONTROLLER_HOST=`cat ./config/controller`
  java -cp $JAR_PATH Main --client-report $CONTROLLER_HOST
}

JAR_PATH="build/libs/distribufile-uber.jar"

if [[ $# -ge 1 ]]; then
  case $1 in

    "--cleanup")
      cd bin/ && ./clean_artifacts.sh
      ;;

    "--shutdown")
      cd bin/ && ./shutdown_all.sh
      ;;

    "--controller")
      cd bin/ && ./start_controller.sh
      ;;

    "--chunkservers")
      cd bin/ && ./start_chunkservers.sh
      ;;

    "--client-write")
      shift
      client_write $@
      ;;

    "--client-read")
      shift
      client_read $@
      ;;

    "--status")
      client_status
      ;;

    "-h" | "--h" | *)
      print_usage
      ;;

  esac

else
  print_usage
  exit 1
fi
