package org.onebusaway.container.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;

/**
 * A custom Hibernate {@link EnhancedUserType} for mapping a Java enum to an SQL
 * varchar in the database as opposed to the default behavior of an int.
 * 
 * @author bdferris
 * 
 */
public class EnumUserType implements EnhancedUserType, ParameterizedType {

  @SuppressWarnings("rawtypes")
  private Class<Enum> enumClass;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void setParameterValues(Properties parameters) {
    String enumClassName = parameters.getProperty("enumClassName");
    try {
      Class<?> clazz = Class.forName(enumClassName);
      enumClass = (Class<Enum>) clazz;
    } catch (ClassNotFoundException cnfe) {
      throw new HibernateException("Enum class not found", cnfe);
    }
  }

  public Object assemble(Serializable cached, Object owner)
      throws HibernateException {
    return cached;
  }

  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  @SuppressWarnings("rawtypes")
  public Serializable disassemble(Object value) throws HibernateException {
    return (Enum) value;
  }

  public boolean equals(Object x, Object y) throws HibernateException {
    return x == y;
  }

  public int hashCode(Object x) throws HibernateException {
    return x.hashCode();
  }

  public boolean isMutable() {
    return false;
  }

  @SuppressWarnings("unchecked")
  public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
      throws HibernateException, SQLException {
    String name = rs.getString(names[0]);
    return rs.wasNull() ? null : Enum.valueOf(enumClass, name);
  }

  @SuppressWarnings("rawtypes")
  public void nullSafeSet(PreparedStatement st, Object value, int index)
      throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, Types.VARCHAR);
    } else {
      st.setString(index, ((Enum) value).name());
    }
  }

  public Object replace(Object original, Object target, Object owner)
      throws HibernateException {
    return original;
  }

  @SuppressWarnings("rawtypes")
  public Class returnedClass() {
    return enumClass;
  }

  public int[] sqlTypes() {
    return new int[] {Types.VARCHAR};
  }

  @SuppressWarnings("unchecked")
  public Object fromXMLString(String xmlValue) {
    return Enum.valueOf(enumClass, xmlValue);
  }

  @SuppressWarnings("rawtypes")
  public String objectToSQLString(Object value) {
    return '\'' + ((Enum) value).name() + '\'';
  }

  @SuppressWarnings("rawtypes")
  public String toXMLString(Object value) {
    return ((Enum) value).name();
  }

}
