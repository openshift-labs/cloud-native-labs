###################
# Lab 5 Solution  #
###################

# Deploy Web on OpenShift
oc new-app nodejs~https://github.com/openshift-labs/cloud-native-labs.git#ocp-3.11 \
        --context-dir=web-nodejs \
        --name=web 

oc expose svc/web
