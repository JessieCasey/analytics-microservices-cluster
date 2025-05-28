# Define and build infrastructure services
k8s_yaml([
    "kubernetes/infrastructure/mongodb/mongodb.yml",
    "kubernetes/infrastructure/prometheus/prometheus.yml",
    "kubernetes/infrastructure/fluent-bit/fluent-bit.yml",
    "kubernetes/infrastructure/loki/loki.yml",
    "kubernetes/infrastructure/tempo/tempo.yml",
    "kubernetes/infrastructure/grafana/grafana.yml"

])

# Define infrastructure resources
k8s_resource("prometheus", labels=["observability"], auto_init=True)
k8s_resource("fluent-bit", labels=["observability"], auto_init=True)
k8s_resource("loki", labels=["observability"], auto_init=True)
k8s_resource("tempo", labels=["observability"], auto_init=True)
k8s_resource("grafana", labels=["observability"], auto_init=True)
k8s_resource("price-mongodb", labels=["infra"], auto_init=True)

# Define and build price-service
docker_build(
    "price-service",
    context="./microservices/price-service",
    dockerfile="./microservices/price-service/Dockerfile",
    live_update=[
        sync("./microservices/price-service/src", "/application/src"),
        run("mvn package -DskipTests", trigger=["/application/src"]),
    ]
)
k8s_yaml([
    "microservices/price-service/kubernetes/deployment.yml",
    "microservices/price-service/kubernetes/service.yml"
])
k8s_resource(
    "price-service",
    port_forwards="9002:9002",
    labels=["services"]
)

# Define and build gateway-service
docker_build(
    "gateway-service",
    context="./spring-cloud/gateway-service",
    dockerfile="./spring-cloud/gateway-service/Dockerfile",
    live_update=[
        sync("./spring-cloud/gateway-service/src", "/application/src"),
        run("mvn package -DskipTests", trigger=["/application/src"]),
    ]
)
k8s_yaml([
    "spring-cloud/gateway-service/kubernetes/deployment.yml",
    "spring-cloud/gateway-service/kubernetes/service.yml",
    "spring-cloud/gateway-service/kubernetes/ingress.yml"
])
k8s_resource(
    "gateway-service",
    labels=["services"]
)