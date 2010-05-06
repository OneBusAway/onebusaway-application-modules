package org.onebusaway.oba.web.commute.client.model;

import org.onebusaway.common.web.common.client.model.AbstractModel;
import org.onebusaway.oba.web.common.client.model.LocationBounds;
import org.onebusaway.oba.web.common.client.model.MinTransitTimeResult;

import java.util.List;

public class ResultsModel extends AbstractModel<ResultsModel> {

  private List<LocationBounds> _grid;

  private List<Integer> _times;

  public void setSearchResult(MinTransitTimeResult result) {
    _grid = result.getTimeGrid();
    _times = result.getTimes();
    fireModelChange(this);
  }

  public int size() {
    if (_grid == null)
      return 0;
    return _grid.size();
  }

  public LocationBounds getGrid(int index) {
    return _grid.get(index);
  }

  public int getTime(int index) {
    return _times.get(index);
  }
}
