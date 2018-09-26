###################
# Lab 6 Solution  #
###################

# Health Probes
oc set probe dc/catalog --liveness --readiness --initial-delay-seconds=30 --failure-threshold=3 --get-url=http://:8080/health 
oc set probe dc/inventory --liveness --readiness --initial-delay-seconds=30 --failure-threshold=3 --get-url=http://:8080/node
oc set probe dc/gateway --liveness --readiness --initial-delay-seconds=15 --failure-threshold=3 --get-url=http://:8080/health
oc set probe dc/web --liveness --readiness --get-url=http://:8080/