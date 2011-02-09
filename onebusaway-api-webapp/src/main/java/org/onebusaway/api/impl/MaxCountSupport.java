package org.onebusaway.api.impl;

import java.util.Collections;
import java.util.List;

public class MaxCountSupport {

  private int _maxCount;

  private int _absoluteMaxCount;

  public MaxCountSupport() {
    _maxCount = Integer.MAX_VALUE;
    _absoluteMaxCount = Integer.MAX_VALUE;
  }

  public MaxCountSupport(int defaultMaxCount, int absoluteMaxCount) {
    _maxCount = defaultMaxCount;
    _absoluteMaxCount = absoluteMaxCount;
  }

  public void setMaxCount(int maxCount) {
    _maxCount = maxCount;
  }

  public int getMaxCount() {
    return Math.min(_maxCount, _absoluteMaxCount);
  }

  public <T> List<T> filter(List<T> values, boolean shuffle) {

    int maxCount = getMaxCount();

    if (values.size() > maxCount) {
      if (shuffle)
        Collections.shuffle(values);
      while (values.size() > maxCount)
        values.remove(values.size() - 1);
    }
    
    return values;
  }
}
