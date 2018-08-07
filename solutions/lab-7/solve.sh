###################
# Lab 7 Solution  #
###################

DIRECTORY=`dirname $0`

# Web Auto-Scaler
oc set resources dc/web --limits=cpu=400m,memory=512Mi --requests=cpu=200m,memory=256Mi
oc autoscale dc/web --min 1 --max 5 --cpu-percent=40

# Add Circuit Breaker to Gateway
cp $DIRECTORY/*.java $DIRECTORY/../../gateway-vertx/src/main/java/com/redhat/cloudnative/gateway
mvn clean fabric8:deploy -f $DIRECTORY/../../gateway-vertx