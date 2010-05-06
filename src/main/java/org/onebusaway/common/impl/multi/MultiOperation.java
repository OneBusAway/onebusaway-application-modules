package org.onebusaway.common.impl.multi;

public interface MultiOperation<T> {
  public void evaluate(MultiContext<T> context, T entry);
}
