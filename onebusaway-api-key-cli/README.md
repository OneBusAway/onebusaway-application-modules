# OneBusAway API Key CLI

A command-line tool for managing OneBusAway API keys directly against the database, without requiring the admin webapp.

## Building

From the `app-modules` directory:

```bash
mvn clean package -pl onebusaway-api-key-cli -am
```

This creates a standalone JAR with all dependencies at:
```
onebusaway-api-key-cli/target/onebusaway-api-key-cli-2.7.1-withAllDependencies.jar
```

## Prerequisites

Before using this tool, you need a `data-sources.xml` file that configures the database connection. Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/onebusaway?useSSL=false&amp;allowPublicKeyRetrieval=true"/>
        <property name="username" value="oba_user"/>
        <property name="password" value="oba_password"/>
    </bean>
</beans>
```

The database must already have the OneBusAway schema created (tables `oba_users`, `oba_user_indices`, etc.).

## Usage

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar <command> [options]
```

### Example

```bash
java -jar onebusaway-api-key-cli/target/onebusaway-api-key-cli-2.7.1-withAllDependencies.jar \
  list \
  --config api-key-cli-data-source.xml
```

### Commands

| Command | Description |
|---------|-------------|
| `create` | Create a new API key |
| `list` | List all API keys |
| `get` | Get details for a specific key |
| `update` | Update an existing key |
| `delete` | Delete an API key |
| `help` | Show help message |

### Global Options

| Option | Description |
|--------|-------------|
| `-c, --config <path>` | Path to data-sources.xml (required for all commands except help) |
| `-j, --json` | Output in JSON format (for scripting and automation) |

### Create Options

| Option | Description |
|--------|-------------|
| `-k, --key <value>` | API key value (optional, UUID generated if not provided) |
| `-n, --name <value>` | Contact name |
| `-o, --company <value>` | Contact company |
| `-e, --email <value>` | Contact email |
| `-d, --details <value>` | Contact details |
| `-m, --minApiReqInt <ms>` | Minimum API request interval in milliseconds (default: 100) |

### Get/Delete Options

| Option | Description |
|--------|-------------|
| `-k, --key <value>` | API key value (required) |

### Update Options

| Option | Description |
|--------|-------------|
| `-k, --key <value>` | API key value (required) |
| `-n, --name <value>` | Contact name |
| `-o, --company <value>` | Contact company |
| `-e, --email <value>` | Contact email |
| `-d, --details <value>` | Contact details |
| `-m, --minApiReqInt <ms>` | Minimum API request interval in milliseconds |

## Examples

### Create Command

Create a new API key with auto-generated UUID:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar create \
    --config /path/to/data-sources.xml
```

Create a key with contact information:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar create \
    --config /path/to/data-sources.xml \
    --name "Admin User" \
    --email admin@example.com
```

Create a key with a specific value instead of auto-generated UUID:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar create \
    --config /path/to/data-sources.xml \
    --key my-custom-api-key
```

Create a key with all options:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar create \
    --config /path/to/data-sources.xml \
    --key my-custom-api-key \
    --name "Jane Developer" \
    --company "Transit Agency" \
    --email developer@example.com \
    --details "Mobile app integration - approved 2024-01" \
    --minApiReqInt 50
```

### List Command

List all API keys:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar list \
    --config /path/to/data-sources.xml
```

### Get Command

Get details for a specific key:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar get \
    --config /path/to/data-sources.xml \
    --key my-custom-api-key
```

### Update Command

Update just the email for an existing key:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar update \
    --config /path/to/data-sources.xml \
    --key my-custom-api-key \
    --email newemail@example.com
```

Update the rate limit for an existing key:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar update \
    --config /path/to/data-sources.xml \
    --key my-custom-api-key \
    --minApiReqInt 200
```

Update all fields for an existing key:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar update \
    --config /path/to/data-sources.xml \
    --key my-custom-api-key \
    --name "John Smith" \
    --company "New Transit Corp" \
    --email updated@example.com \
    --details "Updated contact - transferred ownership 2024-06" \
    --minApiReqInt 100
```

### Delete Command

Delete an API key:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar delete \
    --config /path/to/data-sources.xml \
    --key my-custom-api-key
```

### Help Command

Show general help:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar help
```

### JSON Output

All commands support the `--json` flag for machine-readable output. This is useful for scripting and automation.

List keys in JSON format:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar list \
    --config /path/to/data-sources.xml \
    --json
```

Output:
```json
{
  "total" : 3,
  "keys" : [ "key1", "key2", "key3" ]
}
```

Get key details in JSON format:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar get \
    --config /path/to/data-sources.xml \
    --key my-api-key \
    --json
```

Output:
```json
{
  "key" : "my-api-key",
  "contactName" : "Jane Developer",
  "contactCompany" : "Transit Agency",
  "contactEmail" : "jane@example.com",
  "contactDetails" : "Mobile app integration",
  "minApiRequestInterval" : 100
}
```

Create a key with JSON output:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar create \
    --config /path/to/data-sources.xml \
    --key new-key \
    --email dev@example.com \
    --json
```

Output:
```json
{
  "success" : true,
  "message" : "API key created successfully",
  "key" : "new-key",
  "contactName" : "",
  "contactCompany" : "",
  "contactEmail" : "dev@example.com",
  "contactDetails" : "",
  "minApiRequestInterval" : 100
}
```

Delete a key with JSON output:

```bash
java -jar onebusaway-api-key-cli-2.7.1-withAllDependencies.jar delete \
    --config /path/to/data-sources.xml \
    --key old-key \
    --json
```

Output:
```json
{
  "success" : true,
  "message" : "API key deleted successfully",
  "key" : "old-key"
}
```

## API Key Validation

API keys must:
- Contain only alphanumeric characters, dashes (`-`), underscores (`_`), and dots (`.`)
- Be no longer than 128 characters
- Be unique (creating a duplicate key will fail)

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Error (invalid arguments, key not found, database error, etc.) |

## Database Compatibility

This tool uses Hibernate 5 with automatic dialect detection. It has been tested with:
- MySQL 8.x
- PostgreSQL (should work with appropriate JDBC driver)

The tool runs with `hibernate.hbm2ddl.auto=validate`, meaning it will verify the database schema matches the expected entity mappings but will not modify the schema.
