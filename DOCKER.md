# AlmaLinux Docker deployment

The backend image uses AlmaLinux 10 and OpenJDK 17. During image builds, Docker
clones the Spring Boot backend and React frontend directly from GitHub over
HTTPS. The React production build is served by Nginx, which proxies API,
uploaded-file, Swagger, and OpenAPI requests to Spring Boot. PostgreSQL data and
uploaded attachments are kept in named Docker volumes.

## Start

For a new AlmaLinux 10 server, run the unattended installer as root:

```bash
sudo bash src/main/resources/bash-script/docker-deploy.sh
```

The script installs Docker CE, opens HTTP plus SSH port `9011` for TCP and UDP,
clones or updates the deployment project over HTTPS, generates persistent
secrets, builds both GitHub projects without cache, starts the stack, and waits
for both the React page and backend OpenAPI endpoint to respond.

Configuration can be overridden before execution:

```bash
sudo APP_PORT=8080 SOURCE_BRANCH=main \
  bash src/main/resources/bash-script/docker-deploy.sh
```

The default checkout directory is `/opt/ticketing-docker`; override it with
`APP_ROOT`. Generated configuration is stored in `/etc/ticketing-docker/app.env`,
so re-running or resetting the Git checkout keeps the database and JWT secrets.

## Manual Start

```bash
cp .env.example .env
```

Edit `.env`, replace both placeholder secrets, then run:

```bash
docker compose build --no-cache app frontend
docker compose up -d
docker compose logs -f app frontend
```

Use `--no-cache` whenever the image must pull the newest commit from the
configured GitHub branch; otherwise Docker may reuse the previous clone layer.

Backend and frontend repositories must use HTTPS Git URLs. Their defaults are:

```text
https://github.com/yazdan-centos/ticketing-platform.git
main
https://github.com/yazdan-centos/collaboration2.git
main
```

`REACT_APP_API_BASE_URL` is embedded into the React bundle during the Nginx
image build. Set it to the public Nginx origin, for example
`http://155.117.13.33` or `http://155.117.13.33:8080`.

To build specific backend and frontend branches without editing `.env`:

```bash
SOURCE_BRANCH=release FRONTEND_BRANCH=release \
  docker compose build --no-cache app frontend
docker compose up -d
```

This setup expects a public GitHub repository. Do not place a GitHub personal
access token in the Dockerfile, `.env`, build arguments, or repository URL,
because it can be retained in shell history or image metadata.

With `.env.example`, the React application is available at
`http://localhost:8080`. API requests use the same origin under `/api`, and
Swagger UI is at `http://localhost:8080/swagger-ui/index.html`.

## Common commands

```bash
# Show service state
docker compose ps

# Pull the configured GitHub branch and rebuild
docker compose build --no-cache app frontend
docker compose up -d

# Stop without deleting data
docker compose down

# Stop and delete the database and uploaded files
docker compose down --volumes
```

To use an external PostgreSQL server, run the image directly and provide
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`, `APP_JWT_SECRET`, and `FILE_UPLOAD_DIR` as
environment variables.
