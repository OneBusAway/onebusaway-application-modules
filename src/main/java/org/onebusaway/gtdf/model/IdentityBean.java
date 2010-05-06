package org.onebusaway.gtdf.model;

import java.io.Serializable;

public abstract class IdentityBean<T extends Serializable> implements Serializable {

  private static final long serialVersionUID = 1L;

  public abstract T getId();

  public abstract void setId(T id);

  /***************************************************************************
   * {@link Object}
   **************************************************************************/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof IdentityBean))
      return false;
    IdentityBean<?> entity = (IdentityBean<?>) obj;
    return getId().equals(entity.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }
}
