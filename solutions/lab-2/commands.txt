###################
# Lab 2 Solution  #
###################

DIRECTORY=`dirname $0`

# Deploy to OpenShift
cp $DIRECTORY/*.java $DIRECTORY/../../inventory-wildfly-swarm/src/main/java/com/redhat/cloudnative/inventory
mvn clean fabric8:deploy -f $DIRECTORY/../../inventory-wildfly-swarm