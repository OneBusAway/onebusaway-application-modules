package org.onebusaway.hibernate.compat;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides compatibility mapping for Hibernate dialect changes between versions 5 and 6.
 * This helps maintain existing configuration while supporting the new dialect names.
 */
public class DialectCompatibility {
    
    private static final Map<String, String> DIALECT_MAPPINGS = new HashMap<>();
    
    static {
        // MySQL dialect mappings
        DIALECT_MAPPINGS.put("org.hibernate.dialect.MySQL5Dialect", "org.hibernate.dialect.MySQLDialect");
        DIALECT_MAPPINGS.put("org.hibernate.dialect.MySQL57Dialect", "org.hibernate.dialect.MySQLDialect");
        DIALECT_MAPPINGS.put("org.hibernate.dialect.MySQL8Dialect", "org.hibernate.dialect.MySQLDialect");
        
        // PostgreSQL dialect mappings
        DIALECT_MAPPINGS.put("org.hibernate.dialect.PostgreSQL9Dialect", "org.hibernate.dialect.PostgreSQLDialect");
        DIALECT_MAPPINGS.put("org.hibernate.dialect.PostgreSQL10Dialect", "org.hibernate.dialect.PostgreSQLDialect");
        DIALECT_MAPPINGS.put("org.hibernate.dialect.PostgreSQL95Dialect", "org.hibernate.dialect.PostgreSQLDialect");
        
        // HSQLDB dialect mappings (mostly unchanged but included for completeness)
        DIALECT_MAPPINGS.put("org.hibernate.dialect.HSQLDialect", "org.hibernate.dialect.HSQLDialect");
        
        // Oracle dialect mappings
        DIALECT_MAPPINGS.put("org.hibernate.dialect.Oracle9iDialect", "org.hibernate.dialect.OracleDialect");
        DIALECT_MAPPINGS.put("org.hibernate.dialect.Oracle10gDialect", "org.hibernate.dialect.OracleDialect");
        DIALECT_MAPPINGS.put("org.hibernate.dialect.Oracle12cDialect", "org.hibernate.dialect.OracleDialect");
        
        // SQL Server dialect mappings
        DIALECT_MAPPINGS.put("org.hibernate.dialect.SQLServer2008Dialect", "org.hibernate.dialect.SQLServerDialect");
        DIALECT_MAPPINGS.put("org.hibernate.dialect.SQLServer2012Dialect", "org.hibernate.dialect.SQLServerDialect");
        
        // H2 dialect mappings
        DIALECT_MAPPINGS.put("org.hibernate.dialect.H2Dialect", "org.hibernate.dialect.H2Dialect");
    }
    
    /**
     * Maps an old Hibernate 5 dialect name to the appropriate Hibernate 6 dialect.
     * If no mapping is found, returns the original dialect name.
     * 
     * @param oldDialect The Hibernate 5 dialect class name
     * @return The appropriate Hibernate 6 dialect class name
     */
    public static String mapDialect(String oldDialect) {
        if (oldDialect == null) {
            return null;
        }
        
        String newDialect = DIALECT_MAPPINGS.get(oldDialect);
        return newDialect != null ? newDialect : oldDialect;
    }
    
    /**
     * Checks if a dialect name needs to be updated for Hibernate 6 compatibility.
     * 
     * @param dialectName The dialect class name to check
     * @return true if the dialect needs to be updated, false otherwise
     */
    public static boolean needsUpdate(String dialectName) {
        return dialectName != null && DIALECT_MAPPINGS.containsKey(dialectName) 
            && !dialectName.equals(DIALECT_MAPPINGS.get(dialectName));
    }
    
    /**
     * Gets all supported dialect mappings for reference.
     * 
     * @return A copy of the dialect mappings map
     */
    public static Map<String, String> getAllMappings() {
        return new HashMap<>(DIALECT_MAPPINGS);
    }
    
    /**
     * Utility method to check if a dialect is valid for Hibernate 6.
     * This can be used in configuration validation.
     * 
     * @param dialectName The dialect class name to validate
     * @return true if the dialect is compatible with Hibernate 6
     */
    public static boolean isHibernate6Compatible(String dialectName) {
        if (dialectName == null) {
            return false;
        }
        
        // Check if it's already a Hibernate 6 dialect or can be mapped to one
        return DIALECT_MAPPINGS.containsValue(dialectName) || 
               DIALECT_MAPPINGS.containsKey(dialectName);
    }
}