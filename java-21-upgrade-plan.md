# OneBusAway Java 21 Upgrade Project Plan

## Executive Summary

This document outlines the plan to upgrade OneBusAway from Java 17 to Java 21, building on the successful Spring Boot 3.2.12 foundation already established. The upgrade will provide significant performance improvements through virtual threads, enhanced garbage collection, and modern JVM optimizations while maintaining full compatibility with the existing Spring Boot migration work.

## 🎯 Project Objectives

### Primary Goals
- **Upgrade Runtime**: Migrate from Java 17 to Java 21 across all modules
- **Performance Optimization**: Leverage Java 21 virtual threads and GC improvements
- **Dependency Modernization**: Update key libraries for Java 21 compatibility
- **Zero Regression**: Maintain all existing functionality and API compatibility

### Success Criteria
- All 26 Maven modules compile and build successfully on Java 21
- Complete test suite passes (18/18 tests in api-webapp, full integration tests)
- Performance benchmarks show improvement or parity with Java 17
- Docker development environment supports Java 21
- Production deployment ready with rollback procedures

## 📊 Current State Assessment

### ✅ Strengths (Ready for Java 21)
- **Spring Boot 3.2.12**: Excellent Java 21 support with virtual threads
- **Clean Test Suite**: 18/18 tests passing, stable foundation established
- **Java 17 Compatibility**: All JAXB and dependency issues resolved
- **Modern Dependencies**: Already using Spring Boot 3.x compatible libraries
- **Docker Environment**: Infrastructure supports easy Java version switching

### 🟡 Areas Needing Updates
- **Dependency Versions**: Some libraries need minor version bumps for optimal Java 21 support
- **Build Configuration**: Maven compiler settings and JVM options
- **Performance Tuning**: GC settings and virtual thread optimization

### 🔗 Dependencies on Other Work
- **Spring Boot Migration**: Can proceed in parallel with ongoing XML → Java configuration work
- **Jakarta EE Migration**: Future work, not blocking Java 21 upgrade
- **Struts 2 to Spring MVC**: Independent of Java version

## 🗓️ Implementation Timeline

### Phase 1: Environment & Dependencies (Week 1-2)

#### Week 1: Core Java 21 Setup
**Objectives**: Update build system and core dependencies

**Tasks**:
1. **Update Parent POM Configuration**
   ```xml
   <properties>
       <maven.compiler.source>21</maven.compiler.source>
       <maven.compiler.target>21</maven.compiler.target>
       <maven.compiler.release>21</maven.compiler.release>
   </properties>
   ```

2. **Critical Dependency Updates**
   ```xml
   <!-- Virtual threads compatibility -->
   <guava-version>33.0.0-jre</guava-version>
   
   <!-- Java 21 bytecode support -->
   <asm-version>9.6</asm-version>
   
   <!-- Performance optimizations -->
   <protobuf.version>4.25.1</protobuf.version>
   
   <!-- Enhanced commons libraries -->
   <commons-lang3.version>3.14.0</commons-lang3.version>
   ```

3. **Maven Plugin Updates**
   ```xml
   <maven-compiler-plugin.version>3.12.1</maven-compiler-plugin.version>
   <maven-surefire-plugin.version>3.2.5</maven-surefire-plugin.version>
   ```

#### Week 2: Build System Validation
**Objectives**: Ensure compilation and basic functionality

**Tasks**:
1. **Compile All Modules**
   - Run `./make.sh` to verify all 26 modules compile
   - Address any compilation errors or warnings
   - Update Maven wrapper if needed

2. **Basic Test Validation**
   - Run unit tests for core modules
   - Validate Spring Boot application startup
   - Check API endpoint accessibility

3. **Docker Environment Update**
   ```dockerfile
   # Update base images to Java 21
   FROM openjdk:21-jdk-slim
   ```

**Deliverables**:
- ✅ All modules compile successfully on Java 21
- ✅ Core application starts without errors
- ✅ Docker development environment updated

### Phase 2: Testing & Validation (Week 3)

#### Comprehensive Test Suite Execution
**Objectives**: Validate all functionality works correctly with Java 21

**Tasks**:
1. **API Webapp Tests**
   - Run complete test suite: `mvn test`
   - Target: 18/18 tests passing
   - Validate SIRI XML serialization with Java 21
   - Test API key validation and security features

2. **Integration Testing**
   - GTFS-realtime data processing
   - Federation service communication
   - Real-time arrival predictions
   - Vehicle location tracking

3. **Performance Baseline**
   - Measure startup time (Java 17 vs Java 21)
   - API response times under load
   - Memory usage patterns
   - Garbage collection metrics

4. **Cross-Module Validation**
   - Test inter-module dependencies
   - Validate Hessian remoting compatibility
   - Check database connection pooling
   - Verify cache functionality

**Acceptance Criteria**:
- All existing tests pass without modification
- No performance regressions
- Memory usage within expected bounds
- API endpoints return identical responses

### Phase 3: Optimization & Production Readiness (Week 4)

#### Java 21 Feature Enablement
**Objectives**: Leverage Java 21 specific optimizations

**Tasks**:
1. **Virtual Threads Configuration**
   ```yaml
   # application.yml
   spring:
     threads:
       virtual:
         enabled: true
   ```

2. **JVM Optimization**
   ```bash
   # Enhanced GC settings for Java 21
   JAVA_OPTS="-XX:+UseZGC -XX:+UnlockExperimentalVMOptions"
   ```

3. **Performance Tuning**
   - Optimize thread pool configurations
   - Tune connection pool settings for virtual threads
   - Configure garbage collection for transit workloads

4. **Production Configuration**
   - Update deployment scripts
   - Configure monitoring for Java 21 metrics
   - Prepare rollback procedures

**Deliverables**:
- ✅ Virtual threads enabled and configured
- ✅ Production deployment scripts updated
- ✅ Performance monitoring configured
- ✅ Rollback procedures documented

## 🔧 Technical Implementation Details

### Dependency Update Strategy

#### High Priority (Required)
| Dependency | Current Version | Target Version | Reason |
|------------|----------------|----------------|---------|
| Guava | 32.1.2-jre | 33.0.0-jre | Virtual threads compatibility |
| ASM | 8.0.1 | 9.6 | Java 21 bytecode support |
| Maven Compiler Plugin | 3.11.0 | 3.12.1 | Java 21 compiler support |

#### Medium Priority (Recommended)
| Dependency | Current Version | Target Version | Reason |
|------------|----------------|----------------|---------|
| Protocol Buffers | 3.21.12 | 4.25.1 | Performance optimizations |
| Commons Lang3 | 3.12.0 | 3.14.0 | Enhanced utilities |

#### Low Priority (Optional)
| Dependency | Current Version | Target Version | Reason |
|------------|----------------|----------------|---------|
| Jackson | 2.15.x | 2.16.x | Minor performance gains |

### Build Configuration Changes

#### Parent POM Updates
```xml
<properties>
    <!-- Java 21 Configuration -->
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <maven.compiler.release>21</maven.compiler.release>
    
    <!-- Enhanced dependency versions -->
    <guava-version>33.0.0-jre</guava-version>
    <asm-version>9.6</asm-version>
    <protobuf.version>4.25.1</protobuf.version>
    
    <!-- Plugin versions -->
    <maven-compiler-plugin.version>3.12.1</maven-compiler-plugin.version>
    <maven-surefire-plugin.version>3.2.5</maven-surefire-plugin.version>
</properties>
```

#### Compiler Plugin Configuration
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.12.1</version>
    <configuration>
        <release>21</release>
        <compilerArgs>
            <arg>--enable-preview</arg> <!-- If using preview features -->
        </compilerArgs>
    </configuration>
</plugin>
```

### Docker Environment Updates

#### Development Environment
```dockerfile
# docker_app_server/Dockerfile
FROM openjdk:21-jdk-slim

# Install Maven with Java 21 support
ENV MAVEN_VERSION=3.9.6
RUN wget https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    && tar -xzf apache-maven-$MAVEN_VERSION-bin.tar.gz \
    && mv apache-maven-$MAVEN_VERSION /opt/maven \
    && ln -s /opt/maven/bin/mvn /usr/bin/mvn

# JVM optimization for Java 21
ENV JAVA_OPTS="-XX:+UseZGC -XX:+UnlockExperimentalVMOptions"
```

#### Production Deployment
```yaml
# docker-compose.yml updates
services:
  oba-api:
    image: openjdk:21-jre-slim
    environment:
      - JAVA_OPTS=-XX:+UseZGC -Xmx2g
```

## 🧪 Testing Strategy

### Test Categories

#### 1. Unit Tests
- **Scope**: All existing unit tests must pass
- **Target**: 18/18 tests in api-webapp
- **Method**: `mvn test` in each module
- **Acceptance**: 100% pass rate, no test modifications required

#### 2. Integration Tests
- **Scope**: Cross-module functionality
- **Focus Areas**:
  - Spring Boot application startup
  - Database connectivity
  - API endpoint functionality
  - SIRI XML serialization
  - Real-time data processing

#### 3. Performance Tests
- **Metrics to Track**:
  - Application startup time
  - API response latency (p50, p95, p99)
  - Memory usage patterns
  - Garbage collection frequency/duration
  - Thread utilization with virtual threads

#### 4. Compatibility Tests
- **Legacy Features**:
  - Struts 2 action compatibility
  - Hessian remoting functionality
  - GTFS-realtime processing
  - Federation architecture

### Test Execution Plan

#### Pre-Upgrade Baseline
```bash
# Establish Java 17 baseline metrics
mvn clean test -Dtest.performance=true
./scripts/performance-benchmark.sh --baseline
```

#### Post-Upgrade Validation
```bash
# Java 21 validation
mvn clean test -Djava.version=21
./scripts/performance-benchmark.sh --compare-baseline
```

#### Regression Testing
```bash
# Full integration test suite
./make.sh --test --integration
mvn verify -Pintegration-tests
```

## 📈 Performance Optimization

### Virtual Threads Configuration

#### Spring Boot Setup
```yaml
# application.yml
spring:
  threads:
    virtual:
      enabled: true
  task:
    execution:
      pool:
        virtual-threads: true
```

#### Custom Thread Pool Configuration
```java
@Configuration
public class VirtualThreadConfiguration {
    
    @Bean
    public TaskExecutor virtualThreadTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setVirtualThreads(true);
        executor.initialize();
        return executor;
    }
}
```

### JVM Tuning for Transit Workloads

#### Garbage Collection
```bash
# ZGC for low-latency requirements
JAVA_OPTS="-XX:+UseZGC -XX:+UnlockExperimentalVMOptions"

# Alternative: G1GC with Java 21 improvements
JAVA_OPTS="-XX:+UseG1GC -XX:G1HeapRegionSize=16m"
```

#### Memory Management
```bash
# Optimized for GTFS data processing
JAVA_OPTS="$JAVA_OPTS -Xmx4g -Xms2g"
JAVA_OPTS="$JAVA_OPTS -XX:NewRatio=1"
JAVA_OPTS="$JAVA_OPTS -XX:+UseStringDeduplication"
```

## 🚨 Risk Management

### Identified Risks

#### High Risk
1. **Virtual Threads + Hessian Remoting**
   - *Risk*: Compatibility issues with legacy remoting
   - *Mitigation*: Comprehensive testing, fallback to platform threads
   - *Timeline*: Test during Phase 2

2. **Performance Regression**
   - *Risk*: Slower performance than Java 17
   - *Mitigation*: Extensive benchmarking, JVM tuning
   - *Timeline*: Continuous monitoring through all phases

#### Medium Risk
1. **Third-Party Library Compatibility**
   - *Risk*: Dependencies not fully Java 21 compatible
   - *Mitigation*: Staged dependency updates, testing
   - *Timeline*: Address during Phase 1

2. **Memory Usage Changes**
   - *Risk*: Different memory patterns with virtual threads
   - *Mitigation*: Memory profiling, heap tuning
   - *Timeline*: Monitor during Phase 3

#### Low Risk
1. **Docker Build Issues**
   - *Risk*: Base image compatibility
   - *Mitigation*: Test image updates early
   - *Timeline*: Address during Phase 1

### Rollback Procedures

#### Quick Rollback (< 30 minutes)
1. Revert Docker images to Java 17
2. Restart applications with previous images
3. Verify functionality

#### Full Rollback (< 2 hours)
1. Revert all POM changes
2. Rebuild with Java 17
3. Redeploy applications
4. Run validation tests

#### Rollback Triggers
- More than 10% performance degradation
- Any test failures not resolvable within 4 hours
- Critical production issues related to Java 21

## 📋 Pre-Upgrade Checklist

### Environment Preparation
- [ ] Java 21 JDK installed on all development machines
- [ ] Maven 3.9.6+ installed and configured
- [ ] Docker images updated to Java 21 base images
- [ ] CI/CD pipeline configured for Java 21 builds

### Code Preparation
- [ ] Current codebase builds successfully on Java 17
- [ ] All tests pass (18/18 in api-webapp)
- [ ] Performance baseline established
- [ ] Backup of current working state created

### Team Preparation
- [ ] Team familiar with Java 21 features
- [ ] Virtual threads concepts understood
- [ ] Rollback procedures reviewed and tested
- [ ] Performance monitoring tools configured

## 📊 Success Metrics

### Technical Metrics
- **Build Success**: 100% of modules compile successfully
- **Test Pass Rate**: 100% of existing tests pass
- **Performance**: ≤ 5% variance in API response times
- **Memory Usage**: ≤ 10% increase in heap usage
- **Startup Time**: ≤ 10% variance in application startup

### Operational Metrics
- **Deployment Time**: Successful upgrade within 4-week timeline
- **Zero Downtime**: No service interruption during upgrade
- **Issue Resolution**: All issues resolved within 48 hours
- **Team Productivity**: No significant impact on ongoing development

## 🔄 Post-Upgrade Activities

### Immediate (Week 5)
1. **Performance Monitoring**
   - Establish Java 21 performance baselines
   - Monitor garbage collection patterns
   - Track virtual thread utilization

2. **Documentation Updates**
   - Update development setup guides
   - Document new JVM tuning parameters
   - Create Java 21 troubleshooting guide

### Short-term (Weeks 6-8)
1. **Optimization Opportunities**
   - Identify virtual thread adoption candidates
   - Optimize garbage collection settings
   - Fine-tune connection pools

2. **Feature Exploration**
   - Evaluate pattern matching for instanceof
   - Consider text blocks for SQL/XML
   - Explore switch expressions adoption

### Long-term (Months 2-3)
1. **Advanced Features**
   - Migrate to Java 21 language features
   - Evaluate Project Loom integration
   - Consider structured concurrency patterns

2. **Performance Validation**
   - Production performance analysis
   - Cost optimization opportunities
   - Scalability improvements

## 📚 Resources and References

### Documentation
- [OpenJDK 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [Spring Boot 3.2 Java 21 Support](https://spring.io/blog/2023/09/20/hello-java-21)
- [Virtual Threads Guide](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)

### Internal Documentation
- `spring-boot-upgrade.md` - Overall Spring Boot migration plan
- `CLAUDE.md` - AI assistant context and guidelines
- `docker-compose.yml` - Development environment configuration

### Team Contacts
- **Technical Lead**: [Define based on team structure]
- **DevOps Engineer**: [Define based on team structure]
- **QA Lead**: [Define based on team structure]

## 🎯 Conclusion

The Java 21 upgrade is well-positioned for success, building on the solid Spring Boot 3.2.12 foundation already established. The upgrade will provide immediate performance benefits through virtual threads and enhanced garbage collection while maintaining full compatibility with ongoing Spring Boot migration work.

The 4-week timeline is achievable with proper planning and execution. The risk profile is low-to-medium, with comprehensive mitigation strategies in place. This upgrade will position OneBusAway for optimal performance and provide a strong foundation for completing the broader Spring Boot modernization effort.

**Next Steps**: Review this plan with the development team, approve timeline and resource allocation, and proceed with Phase 1 implementation.

---

*Document Version*: 1.0  
*Created*: 2025-06-14  
*Author*: Claude Code Assistant  
*Project*: OneBusAway Spring Boot Upgrade