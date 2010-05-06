package org.onebusaway.container.cache;

import org.aspectj.lang.ProceedingJoinPoint;

import java.io.Serializable;
import java.util.Arrays;

public class DefaultCacheableKeyFactory implements CacheableMethodKeyFactory, Serializable {

  private static final long serialVersionUID = 1L;

  private CacheableObjectKeyFactory[] _keyFactories;

  public DefaultCacheableKeyFactory(CacheableObjectKeyFactory[] keyFactories) {
    _keyFactories = keyFactories;
  }

  public Serializable createKey(ProceedingJoinPoint point) {
    Object[] args = point.getArgs();
    if (args.length != _keyFactories.length)
      throw new IllegalArgumentException();
    KeyImpl keys = new KeyImpl(args.length);
    for (int i = 0; i < args.length; i++)
      keys.set(i, _keyFactories[i].createKey(args[i]));
    return keys;
  }

  private static class KeyImpl implements Serializable {

    private static final long serialVersionUID = 1L;

    private Serializable[] _keys;

    public KeyImpl(int entries) {
      _keys = new Serializable[entries];
    }

    public void set(int index, Serializable key) {
      _keys[index] = key;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof KeyImpl))
        return false;
      KeyImpl other = (KeyImpl) obj;
      return Arrays.equals(_keys, other._keys);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(_keys);
    }

    @Override
    public String toString() {
      return Arrays.toString(_keys);
    }
  }
}
