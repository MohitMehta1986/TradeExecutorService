#! /bin/sh

case "$RUN_ENV" in
   dev) echo "running on dev";;
   uat) echo "running on uat";;
   prod) echo "running on prod";;
   *) echo "valid env {dev/uat}";;
     exit 1;;

esac

MODULE_NAME=$2
MODULE_INSTANCE=$4

if[ -z $MODULE_NAME ]
then
  echo "Missing module name"
  exit 1
fi

if[-z $MODULE_INSTANCE]
then
  echo "Module instance is not specified, default to 1"
  MODULE_INSTANCE=1
fi

PROCESS_TOKEN=P_${RUN_ENV}_${MODULE_NAME}_${MODULE_INSTANCE}

if[-z $JAVA_HOME]
then
  echo "ERROR JAVA_HOME is missing, exit .."
  exit 1
fi


if[-z ${MAIN_CLASS}]
then
  MAIN_CLASS="awesome.code.launcher.GRPCServiceLauncher"
  echo "MAIN_CLASS is not set, default to $MAIN_CLASS"
else
  echo "Java main class is explicitly defined: $MAIN_CLASS"
fi

if[ -z $RUN_DIR]
then
  RUN_DIR="apps/option-trade/logs"
fi

cd $SCRIPT_BASE

cd ../..
INSTALL_DIR=`pwd`

if[ -z "$CONFIG_DIR"]
  then
    CONFIG_DIR = $INSTALL_DIR/config
    echo "CONFIG_DIR defaults to ${CONFIG_DIR}"
fi

echo "information env: $RUN_ENV, module: $MODULE_NAME, module instance: $MODULE_INSTANCE, run dir: $RUN_DIR, install dir: INSTALL_DIR"

LIB_DIR=$INSTALL_DIR/lib

if[ -f "$INSTALL_DIR/bin/common/$cp_file"]
then
  source "$INSTALL_DIR/bin/common/$cp_file"
fi

if[ -z $MODULE_CP ]
then
  CLASS_PATH="$INSTALL_DIR/lib/*:$CONFIG_DIR/env/$RUN_ENV:$CONFIG_DIR/module/$MODULE_NAME:$CONFIG_DIR/env/default"
else
  CLASS_PATH="$MODULE_CP:$CONFIG_DIR/env/$RUN_ENV:$CONFIG_DIR/module/$MODULE_NAME:$CONFIG_DIR/env/default"
fi

if[ -z "$MODULE_CLASS" ]
then
  CORE_COMMANDLINE=" -e $RUN_ENV -c $CONFIG_DIR -l $RUN_DIR -svc $MODULE_NAME -svi $MODULE_INSTANCE"
else
  CORE_COMMANDLINE="-e $RUN_ENV -m $MODULE_CLASS -c $CONFIG_DIR -l $RUN_DIR -svc $MODULE_NAME -svi $MODULE_INSTANCE"
fi

if[ -z "$LOG_CONFIG" ]
then
  LOG_CONFIG=${CONFIG_DIR}/env/${RUN_ENV}/log4j2.K8s.${RUN_ENV}.xml
  echo "LOG_CONFIG defaults to ${LOG_CONFIG}"
fi

CMD="$JAVA_HOME/bin/java -Dzookeper.sasl.client=false -DprocessId=$PROCESS_TOKEN -Dservice.name=$MODULE_NAME -Dlog4j.configurationFile=$LOG_CONFIG -Dcore.service.heartbeat.frequency.millis=30000 $VM_ARGS -classpath $CLASS_PATH $MAIN_CLASS $CORE_COMMANDLINE $PROGRAM_ARGS"

echo "${CMD}"
cd $RUN_DIR
TIME_STAMP=`data +%Y%m%d.%H%M%S`

if[ -z $JOB_NAME ]
then
  export STD_OUT=${PROCESS_TOKEN}.${TIME_STAMP}.console
else
  export STD_OUT=${JOB_NAME}.${TIME_STAMP}.console
fi

mkdir -p console
STDOUT_LOG=console/${STD_OUT}
${CMD} | tee ${STDOUT_LOG} 2>&1

pid=`ps -ef | grep java | grep "$PROCESS_TOKEN" | grep -v grep | awk '{print $2}'`
echo $pid