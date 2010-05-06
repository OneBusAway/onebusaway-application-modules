package org.onebusaway.common.impl.multi;

public interface MultiContext<T> {
  public boolean wantsExit();

  public void add(T element);

  public void add(T element, long delay);
}
