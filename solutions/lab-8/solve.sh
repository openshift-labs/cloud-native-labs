###################
# Lab 8 Solution  #
###################

# Inventory DB
oc new-app postgresql-persistent \
    --param=DATABASE_SERVICE_NAME=inventory-postgresql \
    --param=POSTGRESQL_DATABASE=inventory \
    --param=POSTGRESQL_USER=inventory \
    --param=POSTGRESQL_PASSWORD=inventory \
    --labels=app=inventory

# Catalog DB
oc new-app postgresql-persistent \
    --param=DATABASE_SERVICE_NAME=catalog-postgresql \
    --param=POSTGRESQL_DATABASE=catalog \
    --param=POSTGRESQL_USER=catalog \
    --param=POSTGRESQL_PASSWORD=catalog \
    --labels=app=catalog

# Inventory ConfigMap
cat <<EOF > ./project-defaults.yml
swarm:
  datasources:
    data-sources:
      InventoryDS:
        driver-name: postgresql
        connection-url: jdbc:postgresql://inventory-postgresql:5432/inventory
        user-name: inventory
        password: inventory
EOF


oc create configmap inventory --from-file=project-defaults.yml=./project-defaults.yml
oc volume dc/inventory --add --configmap-name=inventory --mount-path=/app/config
oc set env dc/inventory JAVA_ARGS="-s /app/config/project-defaults.yml"

# Catalog Config Map
cat <<EOF > ./application.properties
spring.datasource.url=jdbc:postgresql://catalog-postgresql:5432/catalog
spring.datasource.username=catalog
spring.datasource.password=catalog
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=create
EOF

oc create configmap catalog --from-file=application.properties=./application.properties
oc delete pod -l app=catalog