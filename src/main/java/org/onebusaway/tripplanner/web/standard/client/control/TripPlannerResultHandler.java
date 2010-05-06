package org.onebusaway.tripplanner.web.standard.client.control;

import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.TripPlanModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public class TripPlannerResultHandler implements AsyncCallback<List<TripBean>> {

  private TripPlanModel _model;

  public void setModel(TripPlanModel model) {
    _model = model;
  }

  public void onSuccess(List<TripBean> beans) {
    System.out.println("results are in");
    _model.setTripPlans(beans);
  }

  public void onFailure(Throwable ex) {
    ex.printStackTrace();
  }
}
