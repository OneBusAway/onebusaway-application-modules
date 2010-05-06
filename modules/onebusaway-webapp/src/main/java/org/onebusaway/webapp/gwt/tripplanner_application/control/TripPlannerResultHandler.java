package org.onebusaway.webapp.gwt.tripplanner_application.control;

import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.webapp.gwt.tripplanner_library.model.TripPlanModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public class TripPlannerResultHandler implements AsyncCallback<List<TripPlanBean>> {

  private TripPlanModel _model;

  public void setModel(TripPlanModel model) {
    _model = model;
  }

  public void onSuccess(List<TripPlanBean> beans) {
    System.out.println("results are in");
    _model.setTripPlans(beans);
  }

  public void onFailure(Throwable ex) {
    ex.printStackTrace();
  }
}
