#! /bin/sh

SCRIPT=$(readlink -f "$0")
SCRIPT_BASE=`dirname "$SCRIPT"`
echo  SCRIPT
echo  SCRIPT_BASE

export MODULE_NAME=ComputeServicePod
export MODULE_INSTANCE=1
export VM_ARGS="-Xms4g -Xmx8g -XX:MaxDirectMemorySize=750M"
export MAIN_CLASS=awesome.code.launcher.GRPCServiceLauncher
export MAIN_JAR="option-trade-executor-service-1.0.0-SNAPSHOT.jar"


export cp_file=option-trade-executor-service-classpath-unix.txt

$SCRIPT_BASE/k8s-start-main.sh --servicename $MODULE_NAME --serviceinstance $MODULE_INSTANCE