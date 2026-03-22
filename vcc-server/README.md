# VCC Server

Virtual Credit Card management backend server.

## Tech Stack
- Spring Boot 3.x
- MySQL 8.0
- Redis 7.x
- MyBatis-Plus
- Spring Security

## Modules
- `vcc-admin` - Admin API entry
- `vcc-card` - Card business logic
- `vcc-user` - User management
- `vcc-finance` - Finance module
- `vcc-upstream` - Upstream provider integration
- `vcc-common` - Common utilities
- `vcc-framework` - Framework configuration
- `vcc-system` - System management

## Quick Start
```bash
# Build
mvn clean package

# Run
java -jar vcc-admin/target/vcc-admin.jar
```

## API Documentation
After startup: http://localhost:8080/swagger-ui.html
