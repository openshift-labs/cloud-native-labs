#!/bin/bash

project_name=$1
if [ -z "${project_name}" ]
then
    echo "Usage: ./runGatewayService.sh <project_name>"
    exit 
fi

url=http://istio-ingressgateway.istio-system.svc/${project_name}/api/products

while true; do 
    if curl -s ${url} | grep -q OFFICIAL
    then
        echo "Gateway => Catalog GoLang (v2)";
    else
        echo "Gateway => Catalog Spring Boot (v1)";
    fi
    sleep 1
done