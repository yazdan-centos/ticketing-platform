# Server
export SERVER_PORT=8047

# Datasource
export DB_URL=jdbc:postgresql://localhost/ticketing_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_actual_password_here   # ← required, no default

# JPA / Hibernate
export JPA_DDL_AUTO=validate
export JPA_SHOW_SQL=false

# Flyway
export FLYWAY_ENABLED=true

# JWT
export APP_JWT_SECRET=VGlja2V0aW5nUGxhdGZvcm1Kd3RTZWNyZXRLZXlGb3JIUzI1NlNpZ25pbmc=
export APP_JWT_EXPIRATION=86400000