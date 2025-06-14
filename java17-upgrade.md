# OneBusAway Java 17 Upgrade Project Plan

## Executive Summary

This document outlines a comprehensive plan to upgrade OneBusAway from Java 11 to Java 17, focusing on maintaining stability while modernizing the dependency stack and addressing security vulnerabilities.

## Current State Analysis

### Current Configuration
- **Java Version:** 11 (maven.compiler.source/target: 11)
- **Spring Framework:** 5.2.24.RELEASE (EOL - security risk)
- **Struts:** 2.5.33
- **Hibernate:** 5.4.24.Final
- **Maven Compiler Plugin:** 3.8.1 (outdated)
- **Log4j:** 2.17.2 (current)
- **Jackson:** 2.12.0 (has known CVEs)
- **Guava:** 16.0.1 (very outdated)

### Key Risk Areas
1. **Spring Framework 5.2.x** - End of life, security vulnerabilities
2. **Jackson 2.12.0** - Known security issues (CVE-2022-42003, CVE-2022-42004)
3. **Guava 16.0.1** - Extremely outdated, 16-year version jump with breaking changes
4. **Maven plugins** - Too old for Java 17 support
5. **Protocol Buffers** - GTFS-realtime compatibility with Java 17 module system

## Phase 1: Pre-Upgrade Preparation (Week 1)

### 1.1 Update Build Tools and Maven Plugins

**Root pom.xml updates:**
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <!-- Keep existing properties intact -->
</properties>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version> <!-- Current: 3.8.1 -->
            <configuration>
                <release>17</release>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version> <!-- Current: 3.0.0-M3 -->
            <configuration>
                <argLine>
                    --add-opens java.base/java.lang=ALL-UNNAMED
                    --add-opens java.base/java.lang.reflect=ALL-UNNAMED
                    --add-opens java.base/java.text=ALL-UNNAMED
                    --add-opens java.desktop/java.awt.font=ALL-UNNAMED
                </argLine>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>3.6.0</version> <!-- Current: 2.6 -->
        </plugin>
    </plugins>
</build>
```

### 1.2 Docker Environment Updates

**Update mise.toml:**
```toml
[tools]
java = 'temurin-17'  # Changed from 'temurin-11'
maven = '3'
```

**Update Dockerfile:**
```dockerfile
# Update base image
FROM openjdk:17-jdk-slim
# Update MAVEN_VERSION if needed
ARG MAVEN_VERSION=3.9.9
```

### 1.3 Testing Setup
- Create Java 17 test branch
- Verify Docker build succeeds with Java 17
- Test basic Maven compilation

## Phase 2: Low-Risk Dependency Updates (Weeks 2-3)

### 2.1 Safe Security Updates (Week 2)

**Critical security fixes with minimal compatibility risk:**
```xml
<properties>
    <!-- High-priority security updates -->
    <jackson-version>2.15.2</jackson-version> <!-- Was: 2.12.0 -->
    <junit-version>4.13.2</junit-version> <!-- Was: 4.12 -->
    <mockito-version>5.4.0</mockito-version> <!-- Was: 3.3.3 -->
    
    <!-- HTTP client security update -->
    <httpclient-version>4.5.14</httpclient-version> <!-- Was: 4.5.9 -->
    
    <!-- Protocol Buffers - Java 17 compatibility -->
    <protobuf.version>3.21.12</protobuf.version> <!-- Was: 3.17.3 -->
</properties>

<dependencyManagement>
    <dependencies>
        <!-- Update vulnerable commons-collections -->
        <dependency>
            <groupId>commons-collections</groupId>
            <artifactId>commons-collections</artifactId>
            <version>3.2.2</version> <!-- Was: 3.2 -->
        </dependency>
        
        <!-- ASM update for Java 17 compatibility -->
        <dependency>
            <groupId>org.ow2.asm</groupId>
            <artifactId>asm</artifactId>
            <version>9.5</version> <!-- Was: 8.0.1 -->
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Testing:** Run full test suite after each dependency update, including GTFS-realtime feed processing.

### 2.2 Staged Guava Upgrade (Week 3)

**Strategy:** Staged upgrade to minimize breaking changes from 16-year version jump

**Stage 1: Guava 25.1-jre (Safe intermediate version)**
```xml
<properties>
    <guava-version>25.1-jre</guava-version> <!-- Was: 16.0.1 -->
</properties>
```

**Stage 2: Test thoroughly, then upgrade to modern version**
```xml
<properties>
    <guava-version>32.1.2-jre</guava-version> <!-- Final target -->
</properties>
```

**Critical changes to watch for:**
- `Optional` usage (moved to `java.util.Optional`)
- `Function` and `Predicate` (moved to `java.util.function`)
- Collection utility API changes

## Phase 3: Core Framework Upgrades (Weeks 4-5)

### 3.1 Spring Framework Upgrade (Week 4)

**Strategy:** Upgrade to Spring 5.3.x (stay with javax.* packages)

```xml
<properties>
    <spring-version>5.3.29</spring-version> <!-- Was: 5.2.24.RELEASE -->
</properties>
```

**Focus modules:**
- onebusaway-api-webapp
- onebusaway-transit-data-federation-webapp  
- onebusaway-transit-data-federation

**Testing checklist:**
- [ ] Spring context loading in all webapps
- [ ] Hessian remoting functionality  
- [ ] Database connections via Spring
- [ ] Struts 2.5.33 integration compatibility

### 3.2 ORM and Database Updates (Week 5)

**Hibernate update (stay with javax.persistence):**
```xml
<properties>
    <hibernate-version>5.6.15.Final</hibernate-version> <!-- Was: 5.4.24.Final -->
</properties>
```

**Database driver updates:**
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version> <!-- Already current -->
</dependency>
```

**Testing checklist:**
- [ ] GTFS data loading and processing
- [ ] Real-time data persistence
- [ ] Bundle creation database operations
- [ ] API data queries

## Phase 4: Java 17 Compatibility (Week 6)

### 3.1 Add Required Dependencies

**For annotation processing:**
```xml
<dependency>
    <groupId>javax.annotation</groupId>
    <artifactId>javax.annotation-api</artifactId>
    <version>1.3.2</version>
</dependency>
```

**For XML processing (if needed):**
```xml
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>2.3.8</version>
</dependency>
```

### 3.2 Update Supporting Libraries

**Lucene (for search indices):**
```xml
<!-- In onebusaway-transit-data-federation-builder -->
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-core</artifactId>
    <version>8.11.2</version> <!-- Was: 7.1.0 -->
</dependency>
```

**AspectJ (for Spring AOP):**
```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.19</version>
</dependency>
```

### 3.3 Handle Module System Issues

**Add JVM arguments for tests:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/java.lang.reflect=ALL-UNNAMED
            --add-opens java.base/java.text=ALL-UNNAMED
            --add-opens java.desktop/java.awt.font=ALL-UNNAMED
            --add-opens java.base/java.util=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

## Phase 5: Comprehensive Testing & Validation (Weeks 7-8)

### 4.1 Automated Testing

**Unit Tests:**
- [ ] All existing unit tests pass
- [ ] Integration tests in onebusaway-gtfsrt-integration-tests
- [ ] Performance benchmarks

**Component Testing:**
- [ ] GTFS bundle creation end-to-end
- [ ] Real-time data processing pipeline
- [ ] API endpoint functionality
- [ ] Web application deployment

### 5.2 OneBusAway-Specific Testing

**Critical OneBusAway Features:**
- [ ] GTFS bundle processing with various agency feeds
- [ ] GTFS-realtime feed processing accuracy
- [ ] Real-time vehicle positioning calculations
- [ ] API response format consistency (JSON, XML, Protocol Buffers)
- [ ] Federation routing between multiple instances
- [ ] Search index functionality (Lucene upgrade impact)
- [ ] Geospatial calculations (JTS library compatibility)

### 5.3 Manual Testing

**Core Functionality:**
- [ ] Docker environment setup and build
- [ ] Bundle creation with sample GTFS data
- [ ] API endpoints respond correctly
- [ ] Real-time feed processing
- [ ] Web interfaces load properly

**Performance Testing:**
- [ ] Memory usage comparison (Java 11 vs 17)
- [ ] API response times
- [ ] Bundle creation performance
- [ ] Application startup time

### 5.4 Security Validation

- [ ] Dependency vulnerability scanning
- [ ] Security audit of updated libraries
- [ ] Penetration testing of API endpoints

## Phase 6: Documentation & Deployment (Week 9)

### 6.1 Update Documentation

- [ ] Update README.md with Java 17 requirements
- [ ] Update CLAUDE.md files with new dependency versions
- [ ] Update Docker configuration documentation
- [ ] Create migration guide for deployments

### 6.2 Deployment Preparation

- [ ] Update CI/CD pipelines for Java 17
- [ ] Create deployment rollback plan
- [ ] Update production environment specifications

## Risk Assessment & Mitigation

### High Risk Items

**Spring Framework Upgrade**
- **Risk:** Breaking changes in dependency injection or web stack
- **Mitigation:** Thorough testing of all Spring contexts and web functionality
- **Rollback:** Revert to Spring 5.2.x if critical issues found

**Guava Major Version Jump (16-year gap)**
- **Risk:** Breaking API changes (Optional, Function, Predicate moved to java.util)
- **Mitigation:** Staged upgrade (16→25→32), automated code scanning for deprecated APIs
- **Rollback:** Stay with compatible intermediate version (25.x)

**Protocol Buffers/GTFS-realtime**
- **Risk:** Java 17 module system compatibility issues with binary serialization
- **Mitigation:** Early testing with sample GTFS-realtime feeds in Phase 1
- **Rollback:** Maintain protobuf 3.17.3 if compatibility issues arise

**Java Module System**
- **Risk:** Reflection and internal API access issues
- **Mitigation:** Add necessary --add-opens JVM arguments
- **Rollback:** Use compatibility flags for problematic modules

### Medium Risk Items

**Hibernate Upgrade**
- **Risk:** Query behavior changes, dialect compatibility
- **Mitigation:** Database integration testing, SQL logging review

**Struts 2.5.33 + Spring 5.3.x Integration**
- **Risk:** Framework compatibility issues between updated versions
- **Mitigation:** Comprehensive web application testing, verify Struts-Spring integration

**Spatial/GIS Libraries (JTS)**
- **Risk:** Geospatial calculation precision or performance changes
- **Mitigation:** Geographic calculation regression testing, coordinate accuracy validation

### Low Risk Items

**Jackson Update**
- **Risk:** Minimal, generally backward compatible
- **Mitigation:** JSON serialization testing

**Build Tool Updates**
- **Risk:** Low, Maven plugin updates are typically safe
- **Mitigation:** CI/CD pipeline testing

## Success Criteria

1. **Functional:** All existing functionality works without regression
2. **Security:** All known vulnerabilities in dependencies resolved
3. **Performance:** No significant performance degradation (< 5% slowdown acceptable)
4. **Compatibility:** Successful deployment in Docker environment
5. **Maintainability:** Updated dependency stack supports future development

## Rollback Plan

If critical issues are discovered:

1. **Immediate:** Revert to Java 11 and previous dependency versions
2. **Selective:** Rollback specific problematic dependencies while keeping safe updates
3. **Staged:** Implement upgrade in smaller increments with more testing

## Timeline Summary

- **Week 1:** Build tools and preparation  
- **Week 2:** Safe security updates (Jackson, JUnit, Mockito, ASM, Protocol Buffers)
- **Week 3:** Staged Guava upgrade (16→25→32)
- **Week 4:** Spring Framework upgrade to 5.3.x
- **Week 5:** Hibernate and database layer upgrades
- **Week 6:** Java 17 compatibility fixes and module system handling
- **Weeks 7-8:** Comprehensive testing and validation
- **Week 9:** Documentation and deployment preparation

**Total Estimated Time:** 9 weeks for complete upgrade and validation

## Post-Upgrade Considerations

### Future Jakarta EE Migration

The current plan maintains javax.* packages for stability. A future phase could migrate to Jakarta EE:

```xml
<!-- Future Jakarta EE migration -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>5.0.0</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>6.0.x</version> <!-- Requires jakarta.* -->
</dependency>
```

### Long-term Maintenance

- Establish regular dependency update schedule
- Implement automated vulnerability scanning
- Plan for Java 21 LTS migration in 2-3 years