# Зміст

* [Передумови](#передумови)
* [Запуск застосунку](#запуск-застосунку)
* [Керівництво з Grafana](#керівництво-з-grafana)

# Схема контейнерів

## Опис

**Система управління цінами** складається з кількох мікросервісів Spring Boot, шлюзу, баз даних і зовнішніх систем для
моніторингу, які розгортаються через Docker Compose або Kubernetes (Tilt).

## Контейнери

| Контейнер            | Технологія           | Опис                                                                       |
|----------------------|----------------------|----------------------------------------------------------------------------|
| **Gateway Service**  | Spring Cloud Gateway | Маршрутизує запити до мікросервісів, забезпечує балансування навантаження. |
| **Price Service**    | Spring Boot          | Управляє даними про ціни, взаємодіє з MongoDB.                             |
| **MongoDB Database** | MongoDB              | Зберігає дані оглядів для сервісу оглядів.                                 |
| **Keycloak**         | Keycloak             | Зовнішній сервер аутентифікації для SSO і керування доступом.              |
| **Grafana**          | Grafana              | Візуалізація метрик, логів і трасувань.                                    |
| **Loki**             | Loki                 | Система агрегації логів.                                                   |
| **Tempo**            | Tempo                | Система розподіленого трасування.                                          |
| **Fluent-bit**       | Fluent-bit           | Агент для пересилання логів.                                               |
| **Otel Collector**   | OpenTelemetry        | Збирає і експортує телеметричні дані.                                      |

## Взаємодії

* **Користувачі/Адміністратор** → **Gateway Service**: HTTP-запити через браузер/API-клієнт.
* **Gateway Service** → **Price Service**: Маршрутизує запити щодо курсів.
* **Price Service** ↔ **MongoDB Database**: Операції CRUD для даних оглядів.
* **Всі сервіси** → **Fluent-bit**: Відправляють логи.
* **Всі сервіси** → **Otel Collector**: Відправляють метрики і трасування.
* **Fluent-bit** → **Loki**: Пересилає логи.
* **Otel Collector** → **Tempo**: Відправляє трасування.
* **Otel Collector** → **Grafana**: Відправляє метрики.
* **Grafana** ← **Loki**: Запитує логи.
* **Grafana** ← **Tempo**: Запитує трасування.

### Додаткові нотатки

* **Технології**: Spring Boot, Spring Cloud, JPA, MongoDB driver, OTel, Fluent-bit.
* **Взаємодії**: REST/HTTP, JDBC, MongoDB протокол.
* **Масштабування**: Kubernetes підтримує репліки; Docker Compose — для локальної розробки.

# Передумови

* **Java 17+** (рекомендується для Spring Boot 3.x)
* **Maven 3.8+**
* **Docker & Docker Compose** (для локального розгортання в контейнерах)
* **Minikube & Tilt** (для розгортання на Kubernetes)
* **Httpie / cURL** (для тестування API)
* **Postman / Bruno**

# Запуск застосунку

Запустити застосунок можна за допомогою **Kubernetes (Tilt)**.


### Running with Tilt in Kubernetes env `(Minikube)`

1. Спочатку запустимо сервер

```shell
minikube delete --profile microservice-deployment
```

```shell
    minikube start \
                --profile=microservice-deployment \
                --memory=6g \
                --cpus=4 \
                --disk-size=30g \
                --kubernetes-version=v1.27 \
                --driver=docker
```

```html
😄  [microservice-deployment] minikube v1.35.0 on Darwin 15.3.1
▪ MINIKUBE_ACTIVE_DOCKERD=microservice-deployment
👉  Using Kubernetes 1.27.16 since patch version was unspecified
✨  Using the docker driver based on user configuration
📌  Using Docker Desktop driver with root privileges
👍  Starting "microservice-deployment" primary control-plane node in "microservice-deployment" cluster
🚜  Pulling base image v0.0.46 ...
🔥  Creating docker container (CPUs=4, Memory=4096MB) ...
🐳  Preparing Kubernetes v1.27.16 on Docker 27.4.1 ...
▪ Generating certificates and keys ...
▪ Booting up control plane ...
▪ Configuring RBAC rules ...
🔗  Configuring bridge CNI (Container Networking Interface) ...
🔎  Verifying Kubernetes components...
▪ Using image gcr.io/k8s-minikube/storage-provisioner:v5
🌟  Enabled addons: storage-provisioner, default-storageclass
🏄  Done! kubectl is now configured to use "microservice-deployment" cluster and "default" namespace by default
```

2. Підключимо потрібні плагіни

```shell
    minikube addons enable ingress --profile microservice-deployment
```

```shell
  minikube addons enable metrics-server --profile=microservice-deployment
```

```markdown
💡 ingress is an addon maintained by Kubernetes. For any concerns contact minikube on GitHub.
You can view the list of minikube maintainers at: https://github.com/kubernetes/minikube/blob/master/OWNERS
💡 After the addon is enabled, please run "minikube tunnel" and your ingress resources would be available at "127.0.0.1"
▪ Using image registry.k8s.io/ingress-nginx/controller:v1.11.3
▪ Using image registry.k8s.io/ingress-nginx/kube-webhook-certgen:v1.4.4
▪ Using image registry.k8s.io/ingress-nginx/kube-webhook-certgen:v1.4.4
🔎 Verifying ingress addon...
🌟 The 'ingress' addon is enabled
```

3. Для локальної розробки потрібно адресувати локальний докер

```shell
    eval $(minikube -p microservice-deployment docker-env)
```

4. Let's build the images
5. 
> тут потрібно збудувати проекти, це можна зробити в IDEA у вкладці maven, як створються target папки можна продовжити.

```shell
    sh build-images.sh      
```

```html
Building Docker images for Kubernetes using Minikube...
Building course-composite-service...
[+] Building 31.5s (14/14) FINISHED                                                                                                                                                                            docker:default
=> [internal] load build definition from Dockerfile                                                                                                                                                                     0.0s
=> => transferring dockerfile: 871B                                                                                                                                                                                     0.0s
.
.
.
.
gateway-service built successfully!
All images built successfully!
```

5. Можна переглянути images

```shell
    docker images
```

або

```shell
    docker image ls
```

```html
REPOSITORY                                           TAG        IMAGE ID       CREATED         SIZE
gateway-service                                      latest     2e07a4894e3d   3 minutes ago   339MB
review-service                                       latest     857bf2e92e8d   3 minutes ago   336MB
course-service                                       latest     ad9a018bd4a3   3 minutes ago   359MB
course-composite-service                             latest     5e1868bd77bc   3 minutes ago   328MB
registry.k8s.io/ingress-nginx/controller
<none> ee44bc236803 5 months ago 293MB
    registry.k8s.io/ingress-nginx/kube-webhook-certgen
    <none> a62eeff05ba5 5 months ago 63.8MB
        registry.k8s.io/kube-apiserver v1.27.16 1113933272f1 7 months ago 123MB
        registry.k8s.io/kube-controller-manager v1.27.16 2db343b95a4c 7 months ago 115MB
        registry.k8s.io/kube-scheduler v1.27.16 91ad8454afdd 7 months ago 57.7MB
        registry.k8s.io/kube-proxy v1.27.16 ea1f910af975 7 months ago 79.9MB
        registry.k8s.io/etcd 3.5.12-0 3861cfcd7c04 13 months ago 149MB
        registry.k8s.io/coredns/coredns v1.10.1 ead0a4a53df8 2 years ago 53.6MB
        registry.k8s.io/pause 3.9 e6f181688397 2 years ago 744kB
        gcr.io/k8s-minikube/storage-provisioner v5 6e38f40d628d 3 years ago 31.5MB
```

```shell
    minikube image ls --format table --profile microservice-deployment
```

```html
|----------------------------------------------------|----------|---------------|--------|
|                       Image                        |   Tag    |   Image ID    |  Size  |
|----------------------------------------------------|----------|---------------|--------|
| docker.io/library/review-service                   | latest   | 857bf2e92e8d6 | 336MB  |
| docker.io/library/course-composite-service         | latest   | 5e1868bd77bc2 | 328MB  |
| registry.k8s.io/kube-controller-manager            | v1.27.16 | 2db343b95a4c2 | 115MB  |
| registry.k8s.io/kube-scheduler                     | v1.27.16 | 91ad8454afddc | 57.7MB |
| docker.io/library/gateway-service                  | latest   | 2e07a4894e3d5 | 339MB  |
| docker.io/library/course-service                   | latest   | ad9a018bd4a3c | 359MB  |
| registry.k8s.io/ingress-nginx/kube-webhook-certgen |
<none> | a62eeff05ba51 | 63.8MB |
    | registry.k8s.io/kube-apiserver | v1.27.16 | 1113933272f1e | 123MB |
    | registry.k8s.io/kube-proxy | v1.27.16 | ea1f910af975c | 79.9MB |
    | gcr.io/k8s-minikube/storage-provisioner | v5 | 6e38f40d628db | 31.5MB |
    | registry.k8s.io/coredns/coredns | v1.10.1 | ead0a4a53df89 | 53.6MB |
    | registry.k8s.io/pause | 3.9 | e6f1816883972 | 744kB |
    | registry.k8s.io/ingress-nginx/controller |
    <none> | ee44bc2368033 | 293MB |
        | registry.k8s.io/etcd | 3.5.12-0 | 3861cfcd7c04c | 149MB |
        |----------------------------------------------------|----------|---------------|--------|
```

6. Тепер запуск кластеру

```shell
    Tilt up
```

```html
Tilt started on http://localhost:10350/
v0.33.22, built 2025-01-03

(space) to open the browser
(s) to stream logs (--stream=true)
(t) to open legacy terminal mode (--legacy=true)
(ctrl-c) to exit
```

```shell
    tilt get uiresources
```

```html
NAME                       CREATED AT
prometheus                 2025-03-11T10:37:08Z
fluent-bit                 2025-03-11T10:37:08Z
uncategorized              2025-03-11T10:37:08Z
gateway-service            2025-03-11T10:37:08Z
keycloak                   2025-03-11T10:37:08Z
review-mongodb             2025-03-11T10:37:08Z
tempo                      2025-03-11T10:37:08Z
loki                       2025-03-11T10:37:08Z
price-service              2025-03-11T10:37:08Z
grafana                    2025-03-11T10:37:08Z
(Tiltfile)                 2025-03-11T10:37:08Z
```

```shell
    kubectl get pods,svc,ingress
```

![Tilt web page](notes/images/tilt.png)

---

# Перевірка ендпоінтів

> \[!NOTE]
> На macOS і Windows додаток Minikube ingress **не підтримує використання IP-адреси кластера** при запуску на Docker.
> Тому для локального доступу через 127.0.0.1 потрібно виконати команду:
>
> ```shell
> minikube tunnel --profile microservice-deployment
> ```
>
> Додайте наступне до вашого `/etc/hosts`:
>
> ```shell
>   vi /etc/hosts
> ```
>
> ```
>    127.0.0.1       grafana.local
>    127.0.0.1       keycloak.local
>    127.0.0.1       prometheus.local
> ```

| **Компонент**  | **Docker**                                                   | **Kubernetes на Mac**                                    | Примітка                                          |
| -------------- | ------------------------------------------------------------ | -------------------------------------------------------- | ------------------------------------------------- |
| **Gateway**    | [http://localhost:9000](http://localhost:9000)               | [http://127.0.0.1:80](http://127.0.0.1:80)               |                                                   |
| **Price**      | [http://localhost:8080/prices](http://localhost:8080/prices) | [http://127.0.0.1:80/prices](http://127.0.0.1:80/prices) |                                                   |
| **Grafana**    | [http://localhost:3000](http://localhost:3000)               | [http://grafana.local](http://grafana.local)             | Додайте `127.0.0.1 grafana.local` у /etc/hosts    |
| **Loki**       | [http://loki:3100](http://loki:3100)                         | [http://loki:3100](http://loki:3100)                     |                                                   |
| **Tempo**      | [http://tempo:3200](http://tempo:3200)                       | [http://tempo:3200](http://tempo:3200)                   |                                                   |
| **Fluent-bit** | [http://fluent-bit:24224](http://fluent-bit:24224)           | [http://fluent-bit:24224](http://fluent-bit:24224)       | http на 4318 і grpc на 4317                       |
| **Prometheus** | [http://localhost:9090](http://localhost:9090)               | [http://prometheus.local](http://prometheus.local)       | Додайте `127.0.0.1 prometheus.local` у /etc/hosts |
| **Keycloak**   | [http://localhost:8081](http://localhost:8081)               | [http://keycloak.local](http://keycloak.local)           | Додайте `127.0.0.1 keycloak.local` у /etc/hosts   |

> \[!TIP]
> На Linux Minikube працює як рідний процес безпосередньо на хості, а не всередині віртуальної машини або контейнера Docker.
> Це дозволяє отримати справжню IP-адресу, яку можна використовувати для доступу з хост-системи без додаткової конфігурації.
>
> ```
>   $ minikube ip --profile microservice-deployment
>   192.154.19.8 
> ```
>
> Тепер усі ендпоінти з таблиці будуть доступні за адресою [http://192.154.19.8/](http://192.154.19.8/)\*\*

---

Також використовуйте **OpenAPI specs**, **bruno** або **postman** для перегляду API. Я додам Swagger/SpringDoc, коли матиму час!

---

# Гайд по Grafana

### Доступ до Loki, Tempo, Prometheus і дашбордів у Grafana

## Крок 1: Вхід у Grafana

1. Відкрийте браузер і перейдіть за адресою вашого Grafana (наприклад, `http://localhost:3000`).
2. Увійдіть, використовуючи свої облікові дані (за замовчуванням: `admin`/`admin`). Якщо потрібно, змініть пароль.

---

## Крок 2: Додавання джерел даних (Loki, Tempo, Prometheus)

Налаштуйте Loki, Tempo та Prometheus як джерела даних у Grafana.

1. **Перейдіть у Connections**:

   * У лівому меню наведіть курсор на іконку **Connections** (значок штекера) та виберіть **Data sources**.
     Або відкрийте меню (☰) > **Connections** > **Data sources**.

2. **Додайте нове джерело даних**:

   * Натисніть **+ Add new data source** у верхньому правому куті.

3. **Налаштуйте Prometheus**:

   * Знайдіть `Prometheus` і виберіть його.
   * Вкажіть **Name** (наприклад, "Prometheus").
   * Вкажіть **URL** (наприклад, `http://prometheus:9090`).
   * Залиште налаштування за замовчуванням, якщо не потрібна аутентифікація чи інші специфічні опції.
   * Натисніть **Save & test**. Переконайтесь, що з’явиться повідомлення "Data source is working".

4. **Налаштуйте Loki**:

   * Знайдіть `Loki` і виберіть його.
   * Вкажіть **Name** (наприклад, "Loki").
   * Вкажіть **URL** (наприклад, `http://loki:3100`).
   * За бажанням встановіть **Max lines** (наприклад, 1000) у розділі **Additional settings**.
   * Натисніть **Save & test**. Перевірте, чи все працює.

5. **Налаштуйте Tempo**:

   * Знайдіть `Tempo` і виберіть його.
   * Вкажіть **Name** (наприклад, "Tempo").
   * Вкажіть **URL** (наприклад, `http://tempo:3200`).
   * За бажанням зв’яжіть з Loki для кореляції трасування з логами через **Derived Field** (наприклад, `traceID=(\w+)`).
   * Натисніть **Save & test**. Переконайтесь, що джерело працює.

---

## Крок 3: Перехід у Explore

Використовуйте вкладку **Explore** для виконання запитів безпосередньо до Loki, Tempo та Prometheus.

1. **Відкрийте Explore**:

   * Клацніть іконку **Explore** (компас) у лівому меню, або через меню (☰) > **Explore**.

2. **Виберіть джерело даних**:

   * Використайте випадаючий список зверху для вибору:

      * **Prometheus**: Метрики (наприклад, `rate(http_server_requests_seconds_count[5m])`).
      * **Loki**: Логи (наприклад, `{job="fluent-bit"} |= "error"`).
      * **Tempo**: Трейси (шукайте за trace ID або сервісом).

3. **Виконуйте запити**:

   * **Prometheus**: Введіть PromQL-запит і натисніть **Run query**. Переглядайте у вигляді графіку чи таблиці.
   * **Loki**: Використовуйте LogQL або вкладку **Builder** для фільтрації логів. Натисніть **Run query**.
   * **Tempo**: Введіть trace ID або скористайтеся вкладкою **Search** (фільтруйте за сервісом, тривалістю, тегами). Переглядайте візуалізацію трейсу.

4. **Перемикайте вигляд**:

   * Перемикайтесь між вкладками **Logs**, **Graph** або **Traces** над результатами.

---

## Крок 4: Перегляд та створення дашбордів

Візуалізуйте дані з Loki, Tempo і Prometheus за допомогою дашбордів.

1. **Перегляд існуючих дашбордів**:

   * Клацніть іконку **Dashboards** (сітка з чотирьох квадратів) у боковому меню.
   * Виберіть **Browse** для перегляду існуючих дашбордів (наприклад, з `Spring Boot 3.x Statistic`).

2. **Створіть новий дашборд**:

   * Перейдіть у **Dashboards** > **+ New** > **New dashboard**.
   * Натисніть **+ Add visualization**.
   * Виберіть джерело даних (Prometheus, Loki або Tempo).
   * Додайте запити:

      * **Prometheus**: Запит метрики (наприклад, `http_server_requests_seconds_count`), візуалізуйте як графік.
      * **Loki**: Запит до логів (наприклад, `{job="fluent-bit"}`), використовуйте **Logs** або **Time series**.
      * **Tempo**: Введіть trace ID або запит на пошук, візуалізуйте як хронологію трейсу.
   * Налаштуйте панелі та натисніть **Apply**.

3. **Збережіть дашборд**:

   * Клацніть іконку **Save** (дискета) у верхньому правому куті.
   * Вкажіть ім’я та збережіть дашборд.

4. **Імпортуйте готові дашборди**:

   * Перейдіть у **Dashboards** > **+ New** > **Import**.
   * Завантажте JSON-файли, які є у директорії /grafana-dashboard.
   * Призначте відповідні джерела даних і імпортуйте.

---

### Spring Boot Observability Dashboard

![Observability Dashboard](/notes/images/observability.png)

### Spring Boot Statistic Dashboard

![Observability Dashboard](/notes/images/statistic.png)

### Tempo Traces

![Tempo trace](/notes/images/tempo1.png)
![Tempo trace](/notes/images/tempo2.png)

### Loki Logs

![Loki Logs](/notes/images/loki.png)

---
