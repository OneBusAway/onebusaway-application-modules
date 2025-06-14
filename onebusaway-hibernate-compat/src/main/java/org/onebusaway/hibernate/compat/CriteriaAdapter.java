package org.onebusaway.hibernate.compat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.function.Function;

/**
 * Adapter to help migrate from legacy Hibernate Criteria API to JPA Criteria API.
 * This provides utility methods to ease the transition during Hibernate 6 migration.
 */
public class CriteriaAdapter {
    
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    
    public CriteriaAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }
    
    /**
     * Creates a new CriteriaQuery for the specified entity class
     */
    public <T> CriteriaQuery<T> createQuery(Class<T> entityClass) {
        return criteriaBuilder.createQuery(entityClass);
    }
    
    /**
     * Helper method to create a simple equality query
     */
    public <T> CriteriaQuery<T> createEqualityQuery(Class<T> entityClass, String propertyName, Object value) {
        CriteriaQuery<T> query = createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.where(criteriaBuilder.equal(root.get(propertyName), value));
        return query;
    }
    
    /**
     * Helper method to create a query with custom where clause builder
     */
    public <T> CriteriaQuery<T> createQueryWithWhere(Class<T> entityClass, 
            Function<QueryBuilderContext<T>, jakarta.persistence.criteria.Predicate> whereBuilder) {
        CriteriaQuery<T> query = createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        QueryBuilderContext<T> context = new QueryBuilderContext<>(criteriaBuilder, query, root);
        jakarta.persistence.criteria.Predicate predicate = whereBuilder.apply(context);
        query.where(predicate);
        return query;
    }
    
    /**
     * Context object to provide access to CriteriaBuilder, CriteriaQuery, and Root
     * for building complex where clauses
     */
    public static class QueryBuilderContext<T> {
        private final CriteriaBuilder criteriaBuilder;
        private final CriteriaQuery<T> query;
        private final Root<T> root;
        
        public QueryBuilderContext(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> query, Root<T> root) {
            this.criteriaBuilder = criteriaBuilder;
            this.query = query;
            this.root = root;
        }
        
        public CriteriaBuilder getCriteriaBuilder() {
            return criteriaBuilder;
        }
        
        public CriteriaQuery<T> getQuery() {
            return query;
        }
        
        public Root<T> getRoot() {
            return root;
        }
    }
}