#################################
# Lab Inventory Vertx Solution  #
#################################

DIRECTORY=`dirname $0`

# Deploy to OpenShift
cp $DIRECTORY/*.java $DIRECTORY/../../inventory-vertx/src/main/java/com/redhat/cloudnative/inventory
mvn clean fabric8:deploy -f $DIRECTORY/../../inventory-vertx