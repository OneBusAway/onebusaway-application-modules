package org.onebusaway.hibernate.compat;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Analyzer to scan the codebase and identify Hibernate usage patterns
 * that need to be migrated from Hibernate 5 to Hibernate 6.
 */
public class HibernateUsageAnalyzer {
    
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    
    // Patterns to identify Hibernate 5 usage that needs migration
    private static final Map<String, String> MIGRATION_PATTERNS = new HashMap<>();
    static {
        MIGRATION_PATTERNS.put("Hibernate Criteria API", "createCriteria|org\\.hibernate\\.Criteria");
        MIGRATION_PATTERNS.put("SQL Query API", "createSQLQuery");
        MIGRATION_PATTERNS.put("Custom UserType", "implements\\s+UserType|extends\\s+.*UserType");
        MIGRATION_PATTERNS.put("EhCache Configuration", "hibernate\\.cache\\.provider_class|EhCacheProvider");
        MIGRATION_PATTERNS.put("Hibernate Annotations", "@Type\\s*\\(|@GenericGenerator");
        MIGRATION_PATTERNS.put("Session Factory", "SessionFactory|LocalSessionFactoryBean");
    }
    
    @Test
    public void analyzeHibernateUsage() throws IOException {
        System.out.println("=== Hibernate Usage Analysis ===");
        System.out.println("Scanning project directory: " + PROJECT_ROOT);
        
        Map<String, List<String>> findings = new HashMap<>();
        
        // Initialize findings map
        for (String category : MIGRATION_PATTERNS.keySet()) {
            findings.put(category, new ArrayList<>());
        }
        
        // Scan Java files
        try (Stream<Path> paths = Files.walk(Paths.get(PROJECT_ROOT))) {
            paths.filter(path -> path.toString().endsWith(".java"))
                 .forEach(path -> analyzeFile(path, findings));
        }
        
        // Scan XML configuration files
        try (Stream<Path> paths = Files.walk(Paths.get(PROJECT_ROOT))) {
            paths.filter(path -> path.toString().endsWith(".xml"))
                 .forEach(path -> analyzeFile(path, findings));
        }
        
        // Report findings
        reportFindings(findings);
    }
    
    private void analyzeFile(Path filePath, Map<String, List<String>> findings) {
        try {
            String content = Files.readString(filePath);
            String relativePath = Paths.get(PROJECT_ROOT).relativize(filePath).toString();
            
            for (Map.Entry<String, String> pattern : MIGRATION_PATTERNS.entrySet()) {
                String category = pattern.getKey();
                String regex = pattern.getValue();
                
                Pattern compiledPattern = Pattern.compile(regex, Pattern.MULTILINE);
                Matcher matcher = compiledPattern.matcher(content);
                
                while (matcher.find()) {
                    String match = matcher.group();
                    int lineNumber = getLineNumber(content, matcher.start());
                    String finding = String.format("%s:%d - %s", relativePath, lineNumber, match);
                    findings.get(category).add(finding);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath + " - " + e.getMessage());
        }
    }
    
    private int getLineNumber(String content, int index) {
        return (int) content.substring(0, index).chars().filter(ch -> ch == '\n').count() + 1;
    }
    
    private void reportFindings(Map<String, List<String>> findings) {
        System.out.println("\n=== HIBERNATE MIGRATION ANALYSIS RESULTS ===\n");
        
        int totalIssues = 0;
        for (Map.Entry<String, List<String>> entry : findings.entrySet()) {
            String category = entry.getKey();
            List<String> issues = entry.getValue();
            
            System.out.println("--- " + category + " (" + issues.size() + " occurrences) ---");
            
            if (issues.isEmpty()) {
                System.out.println("  ✓ No issues found");
            } else {
                for (String issue : issues) {
                    System.out.println("  • " + issue);
                }
                totalIssues += issues.size();
            }
            System.out.println();
        }
        
        System.out.println("=== SUMMARY ===");
        System.out.println("Total migration points identified: " + totalIssues);
        
        if (totalIssues > 0) {
            System.out.println("\nPRIORITY MIGRATION ORDER:");
            System.out.println("1. Hibernate Criteria API - Must migrate to JPA Criteria API");
            System.out.println("2. SQL Query API - Replace createSQLQuery with createNativeQuery");
            System.out.println("3. Custom UserType - Update interface implementation");
            System.out.println("4. EhCache Configuration - Migrate to JCache with EhCache 3.x");
            System.out.println("5. Hibernate Annotations - Update deprecated annotations");
            System.out.println("6. Session Factory - Update configuration for Hibernate 6");
        }
    }
    
    @Test
    public void validateTestSetup() {
        // Validate that the test environment is properly configured
        File projectDir = new File(PROJECT_ROOT);
        assert projectDir.exists() : "Project directory should exist";
        assert projectDir.isDirectory() : "Project root should be a directory";
        
        // Check for key project files
        File mainPom = new File(projectDir, "pom.xml");
        assert mainPom.exists() : "Main POM file should exist";
        
        System.out.println("✓ Test environment validation passed");
        System.out.println("  - Project root: " + PROJECT_ROOT);
        System.out.println("  - Main POM exists: " + mainPom.exists());
    }
}