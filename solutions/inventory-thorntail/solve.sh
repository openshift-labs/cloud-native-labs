################################
# inventory-thorntail Solution #
################################

DIRECTORY=`dirname $0`

cp $DIRECTORY/*.java $DIRECTORY/../../inventory-thorntail/src/main/java/com/redhat/cloudnative/inventory
cp $DIRECTORY/pom.xml $DIRECTORY/../../inventory-thorntail
