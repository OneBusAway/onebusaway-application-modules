package org.onebusaway.hibernate.compat;

import jakarta.persistence.Query;
import org.hibernate.Session;

/**
 * Adapter to help migrate from legacy Hibernate createSQLQuery to createNativeQuery.
 * This provides utility methods to ease the transition during Hibernate 6 migration.
 */
public class QueryAdapter {
    
    private final Session session;
    
    public QueryAdapter(Session session) {
        this.session = session;
    }
    
    /**
     * Creates a native SQL query using the new Hibernate 6 API
     * This replaces the deprecated createSQLQuery method
     */
    public Query createNativeQuery(String sqlString) {
        return session.createNativeQuery(sqlString);
    }
    
    /**
     * Creates a native SQL query with result class using the new Hibernate 6 API
     */
    public <T> Query createNativeQuery(String sqlString, Class<T> resultClass) {
        return session.createNativeQuery(sqlString, resultClass);
    }
    
    /**
     * Creates a native SQL query with result set mapping using the new Hibernate 6 API
     */
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return session.createNativeQuery(sqlString, resultSetMapping);
    }
    
    /**
     * Utility method to help with parameter binding in a fluent way
     */
    public static class ParameterBinder {
        private final Query query;
        
        public ParameterBinder(Query query) {
            this.query = query;
        }
        
        public ParameterBinder setParameter(String name, Object value) {
            query.setParameter(name, value);
            return this;
        }
        
        public ParameterBinder setParameter(int position, Object value) {
            query.setParameter(position, value);
            return this;
        }
        
        public Query getQuery() {
            return query;
        }
    }
    
    /**
     * Creates a parameter binder for fluent parameter setting
     */
    public static ParameterBinder bind(Query query) {
        return new ParameterBinder(query);
    }
}