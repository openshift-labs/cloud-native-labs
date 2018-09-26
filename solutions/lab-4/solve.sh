###################
# Lab 4 Solution  #
###################

export PROJECT_NAME=coolstore

DIRECTORY=`dirname $0`

# Enable Vert.x Service Discovery
oc policy add-role-to-user view -n $PROJECT_NAME -z default

# Deploy to OpenShift
cp $DIRECTORY/*.java $DIRECTORY/../../gateway-vertx/src/main/java/com/redhat/cloudnative/gateway
mvn clean fabric8:deploy -f $DIRECTORY/../../gateway-vertx