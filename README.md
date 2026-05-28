# CV Builder

CV Builder is a Spring Boot microservices project for creating, publishing, and browsing public CV profiles. It is built as a Maven monorepo with five services, service discovery through Eureka, asynchronous updates through Kafka, MySQL storage, and a small Thymeleaf UI for the public CV flow.

The project is intentionally split by responsibility: identity owns users, CV management owns draft/publish behavior, and the discovery service keeps a public read model that can be shown in the feed.

## Services

| Service | Port | Purpose |
| --- | ---: | --- |
| `discovery-server` | `8761` | Eureka registry used by the other services. |
| `api-gateway` | `8080` | Public entry point and route layer for API/UI traffic. |
| `identity-service` | `8081`, `9091` | User registration/login, account status, and gRPC identity checks. |
| `cv-management-service` | `8082` | Creates CV drafts and publishes CV events. |
| `discovery-social-service` | `8083` | Consumes identity/CV events, serves the public feed, and renders the UI. |

Supporting containers:

| Dependency | Port | Notes |
| --- | ---: | --- |
| MySQL 8 | `3306` | Uses separate databases for auth, CVs, and public discovery data. |
| Kafka | `9092`, `29092` | Carries user registration and CV publication events. |
| Zookeeper | `2181` | Required by the current Kafka image. |

## How the Pieces Talk

1. A user registers or logs in through `identity-service`.
2. `identity-service` publishes user registration events to Kafka.
3. A CV is saved and published through `cv-management-service`.
4. `cv-management-service` publishes the CV payload to Kafka.
5. `discovery-social-service` consumes those events and builds the public feed.
6. When a public CV is opened, `discovery-social-service` calls `identity-service` over gRPC to confirm the owner account is still active.

## Requirements

- Java 17
- Maven 3.8+
- Docker and Docker Compose
- Bash, curl, and Python 3 if you want to run the smoke test script

## Start Here: From Clone to First CV

This is the fastest path if you only want to run the app and try it.

1. Clone the repository:

```bash
git clone https://github.com/YousefBZo/cv-builder.git
cd cv-builder
```

1. Start Docker Desktop, then run the full stack:

```bash
docker compose up --build
```

The first build can take a few minutes because Docker has to download Java, Maven, MySQL, Kafka, and the service dependencies.

1. Wait until the services are up.

You should see logs from these containers:

- `cv-discovery-server`
- `cv-api-gateway`
- `cv-identity-service`
- `cv-management-service`
- `cv-discovery-social-service`
- `cv-mysql`
- `cv-kafka`
- `cv-zookeeper`

You can also check the Eureka dashboard:

```text
http://localhost:8761
```

When the app is ready, the services should start appearing in Eureka.

1. Open the web app:

```text
http://localhost:8080/ui/login
```

1. Log in with one of the demo users:

```text
Email: demo@example.com
Password: password
```

or:

```text
Email: yousef@example.com
Password: password
```

1. Create a CV.

From the dashboard, click `Forge New Public Profile`, fill in the form, move through the sections, then click `Create and Publish CV`.

1. View the published CV.

After publishing, you will return to the dashboard. Your CV should appear in the public profile list. Open it to see the full profile page. Behind the scenes, the app checks the account through gRPC before showing the public CV.

1. Stop the app when you are done:

```bash
docker compose down
```

If you also want to remove the local MySQL data volume and start fresh next time:

```bash
docker compose down -v
```

## Optional: Create a New User

The UI currently logs in existing users. To create another account, use the API through the gateway:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"new.user@example.com","password":"123456","fullName":"New User"}'
```

Then sign in at:

```text
http://localhost:8080/ui/login
```

Use the email and password you just registered.

## Useful Local URLs

| Page/API | URL |
| --- | --- |
| Web login | `http://localhost:8080/ui/login` |
| Dashboard | `http://localhost:8080/ui/dashboard` |
| Create CV | `http://localhost:8080/ui/cv/new` |
| Public feed API | `http://localhost:8080/api/social/feed` |
| Eureka dashboard | `http://localhost:8761` |
| Identity API | `http://localhost:8080/api/auth/...` |
| CV API | `http://localhost:8080/api/cv/...` |

## Common Problems

If a page does not load right away, give the stack another minute. Kafka, MySQL, and the Spring services do not all become ready at the same time.

If a port is already in use, stop the other process or change the matching port in `docker-compose.yml`.

If the dashboard is empty, create and publish a CV from `/ui/cv/new`. The feed only shows CVs that have been published.

If you want a completely clean database, run:

```bash
docker compose down -v
docker compose up --build
```

## Run Everything with Docker Compose

From the repository root:

```bash
docker compose up --build
```

Useful URLs once the stack is running:

- Gateway: `http://localhost:8080`
- Eureka dashboard: `http://localhost:8761`
- Web UI login: `http://localhost:8080/ui/login`
- Social feed API: `http://localhost:8080/api/social/feed`

The compose file uses local development credentials:

- MySQL user: `root`
- MySQL password: `password`

These values are fine for a local demo. Do not reuse them for a shared or production environment.

## Build and Test Locally

Build every module:

```bash
mvn clean package
```

Run tests:

```bash
mvn test
```

Build one service and its dependencies:

```bash
mvn -pl identity-service -am clean package
```

## API Quick Check

Register a user:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"api.user@example.com","password":"123456","fullName":"API User"}'
```

Log in:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"api.user@example.com","password":"123456"}'
```

Create a CV draft:

```bash
curl -X POST http://localhost:8080/api/cv/save \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "USER_ID_FROM_REGISTER",
    "fullName": "API User",
    "title": "Backend Developer",
    "summary": "Spring Boot developer with microservice experience.",
    "skills": ["Java", "Spring Boot", "Kafka"]
  }'
```

Publish the CV:

```bash
curl -X PUT http://localhost:8080/api/cv/CV_ID_FROM_SAVE/publish
```

Read the public feed:

```bash
curl http://localhost:8080/api/social/feed
```

## Smoke Test

After the Docker Compose stack is healthy, run:

```bash
bash scripts/production-smoke-test.sh
```

The script registers a user, creates and publishes a CV, checks the public detail page, suspends the user in MySQL, and confirms the CV is blocked from the feed/detail flow.

## CI/CD

GitHub Actions is configured in `.github/workflows/monorepo-cicd.yml`.

On pushes to `main` or `master`, the workflow detects which service folders changed and only builds/pushes Docker images for those services. Images are tagged as:

- `DOCKERHUB_USERNAME/service-name:latest`
- `DOCKERHUB_USERNAME/service-name:<commit-sha>`

Required GitHub repository secrets:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

`DOCKERHUB_USERNAME` must be your Docker Hub username, not your email address. `DOCKERHUB_TOKEN` should be a Docker Hub access token created from Docker Hub account settings. If GitHub Actions shows `unauthorized: incorrect username or password`, recreate the Docker Hub access token and update this repository secret.

## Repository Layout

```text
.
|-- api-gateway/
|-- cv-management-service/
|-- discovery-server/
|-- discovery-social-service/
|-- identity-service/
|-- scripts/
|-- docker-compose.yml
|-- pom.xml
`-- .github/workflows/monorepo-cicd.yml
```

## Development Habit

The service boundaries are the most important part of this project. Keep writes in the owning service, publish events when another service needs to know about a change, and let read-heavy services build their own view instead of reaching across databases.
