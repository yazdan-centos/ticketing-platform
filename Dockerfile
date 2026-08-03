FROM almalinux:10 AS build

ARG SOURCE_REPOSITORY=https://github.com/yazdan-centos/ticketing-platform.git
ARG SOURCE_BRANCH=main

RUN dnf -y install \
        ca-certificates \
        curl \
        findutils \
        git \
        java-17-openjdk-devel \
        unzip \
    && dnf clean all \
    && rm -rf /var/cache/dnf

WORKDIR /workspace

RUN case "$SOURCE_REPOSITORY" in https://*) ;; *) echo "SOURCE_REPOSITORY must use HTTPS" >&2; exit 1 ;; esac \
    && git clone --depth 1 --branch "$SOURCE_BRANCH" "$SOURCE_REPOSITORY" source \
    && cd source \
    && chmod +x mvnw \
    && ./mvnw -B -ntp -DskipTests package \
    && cp "$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' -print -quit)" /workspace/app.jar

FROM almalinux:10

WORKDIR /app

RUN dnf -y install java-17-openjdk-headless shadow-utils \
    && dnf clean all \
    && rm -rf /var/cache/dnf \
    && groupadd --system ticketing \
    && useradd --system --gid ticketing --home-dir /app --shell /sbin/nologin ticketing \
    && mkdir -p /var/lib/ticketing/uploads \
    && chown -R ticketing:ticketing /app /var/lib/ticketing

COPY --from=build --chown=ticketing:ticketing /workspace/app.jar /app/app.jar

USER ticketing

EXPOSE 8080

ENV FILE_UPLOAD_DIR=/var/lib/ticketing/uploads

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
