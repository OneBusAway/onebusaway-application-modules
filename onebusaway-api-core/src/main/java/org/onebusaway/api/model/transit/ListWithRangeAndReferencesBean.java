package org.onebusaway.api.model.transit;

import java.util.List;

public class ListWithRangeAndReferencesBean<T> extends ListWithReferencesBean<T> {
  
  private static final long serialVersionUID = 1L;

  private boolean outOfRange = false;

  public ListWithRangeAndReferencesBean() {

  }

  public ListWithRangeAndReferencesBean(List<T> list, boolean limitExceeded, boolean outOfRange, ReferencesBean references) {
    super(list,limitExceeded,references);
    this.outOfRange = outOfRange;
  }

  public boolean isOutOfRange() {
    return outOfRange;
  }

  public void setOutOfRange(boolean outOfRange) {
    this.outOfRange = outOfRange;
  }
}
