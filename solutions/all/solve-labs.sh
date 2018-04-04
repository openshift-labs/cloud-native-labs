#!/bin/bash

#######################
# Lab Solution Script #
#######################

# USAGE:    
#   $ solve-labs.sh [lab-no] [lab-no] ... [lab-no]

# EXAMPLES: Run in the working directory
#   $ bash <(curl -sL https://raw.githubusercontent.com/openshift-labs/cloud-native-labs/ocp-3.9/solutions/all/solve-labs.sh) 1 2 3

GITHUB_ACCOUNT=${GITHUB_ACCOUNT:-openshift-labs}

for LAB_NO in "$@"
do
    if [ $LAB_NO -eq 9 ]; then
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