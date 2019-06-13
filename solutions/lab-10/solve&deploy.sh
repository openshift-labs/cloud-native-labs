###################
# Lab 10 Solution  #
###################

DIRECTORY=`dirname $0`

# Deploy to OpenShift
$DIRECTORY/solve.sh
mvn clean fabric8:deploy -f $DIRECTORY/../../inventory-wildfly-swarm