# Employee Service (Production notes)

This project contains a simple file-store microservice with optional S3 uploads.

Production-related changes added:

- Updated project coordinates in `build.gradle` to production-style values.
- Added `application-prod.properties` which reads database and AWS configuration from environment variables.
- Added `Dockerfile` (multi-stage) to build and run the application in a container.
- Added `logback-spring.xml` for centralized logging configuration.

Required environment variables for production (example):

```bash
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@db-host:1521/PRODDB
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=prod_password
HIKARI_MAX_POOL=30
AWS_S3_BUCKET=your-bucket
AWS_S3_REGION=ap-south-1
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
APP_JAVA_OPTS="-Xms512m -Xmx1g"
```

Build and run (locally):

```bash
# Build
./gradlew clean bootJar -x test

# Run with prod profile and env vars
SPRING_PROFILES_ACTIVE=prod \
  SPRING_DATASOURCE_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1 \
  SPRING_DATASOURCE_USERNAME=system \
  SPRING_DATASOURCE_PASSWORD=oracle \
  ./gradlew bootRun --args='--spring.profiles.active=prod'
```

Build Docker image and run container:

```bash
docker build -t employee-service:1.0.0 .
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL} \
  -e SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME} \
  -e SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD} \
  -e AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} \
  -e AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} \
  employee-service:1.0.0
```

