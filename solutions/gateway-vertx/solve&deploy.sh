##########################
# gateway-vertx Solution #
##########################

DIRECTORY=`dirname $0`

$DIRECTORY/solve.sh
mvn clean fabric8:deploy -f $DIRECTORY/../../gateway-vertx