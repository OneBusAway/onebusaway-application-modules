package org.onebusaway.hibernate.compat;

import org.hibernate.usertype.UserType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Base adapter class to help migrate custom UserType implementations to Hibernate 6.
 * This provides a foundation for updating existing UserType implementations.
 */
public abstract class UserTypeAdapter<T> implements UserType<T> {
    
    @Override
    public int getSqlType() {
        // Default to VARCHAR, subclasses should override
        return SqlTypes.VARCHAR;
    }
    
    @Override
    public boolean equals(T x, T y) {
        if (x == null && y == null) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }
    
    @Override
    public int hashCode(T x) {
        return x == null ? 0 : x.hashCode();
    }
    
    @Override
    public T nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) 
            throws SQLException {
        String value = rs.getString(position);
        return rs.wasNull() ? null : convertFromString(value);
    }
    
    @Override
    public void nullSafeSet(PreparedStatement st, T value, int index, SharedSessionContractImplementor session) 
            throws SQLException {
        if (value == null) {
            st.setNull(index, getSqlType());
        } else {
            st.setString(index, convertToString(value));
        }
    }
    
    @Override
    public T deepCopy(T value) {
        // Default implementation for immutable types
        return value;
    }
    
    @Override
    public boolean isMutable() {
        // Default to immutable, subclasses can override
        return false;
    }
    
    @Override
    public Serializable disassemble(T value) {
        return (Serializable) deepCopy(value);
    }
    
    @Override
    public T assemble(Serializable cached, Object owner) {
        return deepCopy((T) cached);
    }
    
    /**
     * Convert the object to string representation for database storage
     */
    protected abstract String convertToString(T value);
    
    /**
     * Convert string from database to object
     */
    protected abstract T convertFromString(String value);
}