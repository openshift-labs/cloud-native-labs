#!/bin/bash

#######################
# Lab Solution Script #
#######################

# USAGE:    
#   $ solve-labs.sh [lab-no] [lab-no] ... [lab-no]

# EXAMPLES: Run in the working directory
#   $ bash <(curl -sL https://raw.githubusercontent.com/openshift-labs/cloud-native-labs/ocp-3.9/solutions/all/solve-labs.sh) 1 2 3

GITHUB_ACCOUNT=${GITHUB_ACCOUNT:-openshift-labs}
PROJECT_NAME=${PROJECT_NAME:-coolstore}

echo "##########################################################"
echo " Using Project Name: ${PROJECT_NAME}"
echo " You can set the project name by 'export PROJECT_NAME=coolstore'"
echo "##########################################################"

for LAB_NO in "$@"
do
    if [ $LAB_NO -eq 1 ]; then
      echo
      echo "WARNING: Lab 1 must be done manually!"
      echo
    elif [ $LAB_NO -eq 9 ]; then
      echo
      echo "WARNING: Lab 9 cannot be solved using this script. It requires manual steps."
      echo
    else
      echo 
      echo "########################"
      echo " Solving Lab $LAB_NO"
      echo "########################"
      echo
      bash <(curl -sL https://raw.githubusercontent.com/$GITHUB_ACCOUNT/cloud-native-labs/ocp-3.9/solutions/lab-$LAB_NO/commands.txt)
    fi
done