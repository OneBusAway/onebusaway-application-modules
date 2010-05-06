package org.onebusaway.users.impl.hibernate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.onebusaway.users.model.UserProperties;

public class UserPropertiesUserType implements UserType {

  private static final int[] SQL_TYPES = {Types.BLOB};

  @Override
  public Class<?> returnedClass() {
    return UserProperties.class;
  }

  @Override
  public int[] sqlTypes() {
    return SQL_TYPES;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return x == y;
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    return x.hashCode();
  }

  @Override
  public boolean isMutable() {
    return true;
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    UserProperties latest = ensureLatest(value);
    return latest.deepCopy();
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
      throws HibernateException, SQLException {

    byte[] bytes = rs.getBytes(names[0]);

    if (rs.wasNull())
      return null;

    return getBytesAsObject(bytes);
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index)
      throws HibernateException, SQLException {

    if (value == null) {
      st.setNull(index, SQL_TYPES[0]);
    } else {
      byte[] raw = getObjectAsBytes(value);
      st.setBytes(index, raw);
    }
  }

  @Override
  public Object assemble(Serializable cached, Object owner)
      throws HibernateException {
    return deepCopy(cached);
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    return (Serializable) deepCopy(value);
  }

  @Override
  public Object replace(Object original, Object target, Object owner)
      throws HibernateException {
    if (original == null)
      return null;
    return deepCopy(original);
  }

  /****
   * Private Methods
   ****/

  private UserProperties ensureLatest(Object object) {
    return (UserProperties) object;
  }

  private Object getBytesAsObject(byte[] bytes) throws SQLException {
    try {
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
          bytes));
      return ois.readObject();
    } catch (Exception ex) {
      throw new SQLException("Error reading serialized UserProperties object",
          ex);
    }
  }

  private byte[] getObjectAsBytes(Object object) throws SQLException {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bos);
      out.writeObject(object);
      out.close();
      bos.close();
      return bos.toByteArray();
    } catch (Exception ex) {
      throw new SQLException("Error writing serialized UserProperties object",
          ex);
    }
  }
}
