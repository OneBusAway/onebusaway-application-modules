package org.onebusaway.container.model;

import org.onebusaway.container.cache.CacheableKey;

import java.io.Serializable;

@CacheableKey(keyFactory = IdentityBeanCacheableObjectKeyFactory.class)
public abstract class IdentityBean<T extends Serializable> implements Serializable {

  private static final long serialVersionUID = 1L;

  public abstract T getId();

  public abstract void setId(T id);

  /***************************************************************************
   * {@link Object}
   **************************************************************************/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof IdentityBean<?>))
      return false;
    if (getClass() != obj.getClass())
      return false;
    IdentityBean<?> entity = (IdentityBean<?>) obj;
    return getId().equals(entity.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }
}
