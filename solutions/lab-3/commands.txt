###################
# Lab 3 Solution  #
###################

DIRECTORY=`dirname $0`

# Deploy to OpenShift
cp $DIRECTORY/*.java $DIRECTORY/../../catalog-spring-boot/src/main/java/com/redhat/cloudnative/catalog
mvn clean fabric8:deploy -f $DIRECTORY/../../catalog-spring-boot