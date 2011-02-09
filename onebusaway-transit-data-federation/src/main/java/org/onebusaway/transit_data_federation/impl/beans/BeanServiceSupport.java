package org.onebusaway.transit_data_federation.impl.beans;

import java.util.Collections;
import java.util.List;

public class BeanServiceSupport {

  public static <T> boolean checkLimitExceeded(List<T> elements, int maxCount) {
    boolean limitExceeded = elements.size() > maxCount;
    if (limitExceeded) {
      Collections.shuffle(elements);
      while (elements.size() > maxCount)
        elements.remove(elements.size() - 1);
    }
    return limitExceeded;
  }

}
