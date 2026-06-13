#! /bin/sh

SCRIPT=$(readlink -f "$0")
SCRIPT_BASE=`dirname "$SCRIPT"`

if[!-z $1 ]
then
  MODULE_INSTANCE=$1
  shift
fi

$SCRIPT_BASE/k8s-start.sh --servicename $MODULE_NAME --serviceinstance $MODULE_INSTANCE