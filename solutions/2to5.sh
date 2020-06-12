oc new-project coolstore-XX
oc project coolstore-XX
oc policy add-role-to-user view -n coolstore-XX -z default
chmod 777 ./labs/solutions/lab-2/solve.sh
./labs/solutions/lab-2/solve.sh
chmod 777 ./labs/solutions/lab-3/solve.sh
./labs/solutions/lab-3/solve.sh
chmod 777 ./labs/solutions/lab-4/solve.sh
./labs/solutions/lab-4/solve.sh
oc new-app nodejs:8~https://github.com/nmoctezum/cloud-native-labs.git#nmoctezum-patch-1 --context-dir=web-nodejs --name=web
oc expose svc/web
