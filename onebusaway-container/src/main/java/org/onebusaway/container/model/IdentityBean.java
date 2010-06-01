package org.onebusaway.container.model;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.container.cache.CacheableKey;

import java.io.Serializable;

/**
 * Convenience base class for objects that have an id property and whose object
 * equality is determined by that identi Defines abstract methods for getting
 * and setting the objects id while also define {@link #equals(Object)} and
 * {@link #hashCode()} based on the id value. Additionally, defines a
 * {@link CacheableKey} key strategy based on the object id when the object is
 * used in as an argument in a method annotated with {@link Cacheable}.
 * 
 * @author bdferris
 */
@CacheableKey(keyFactory = IdentityBeanCacheableObjectKeyFactory.class)
public abstract class IdentityBean<T extends Serializable> implements
    Serializable {

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
