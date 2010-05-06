package org.onebusaway.webapp.gwt.mobile_application.view;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.webapp.gwt.mobile_application.MobileApplicationContext;
import org.onebusaway.webapp.gwt.mobile_application.control.ActivityHandler;
import org.onebusaway.webapp.gwt.mobile_application.control.StopWithArrivalsAndDeparturesListViewModel;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationCssResource;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationResources;
import org.onebusaway.webapp.gwt.viewkit.BarButtonItem;
import org.onebusaway.webapp.gwt.viewkit.ListViewController;
import org.onebusaway.webapp.gwt.viewkit.NavigationItem;
import org.onebusaway.webapp.gwt.viewkit.ViewController;
import org.onebusaway.webapp.gwt.viewkit.BarButtonItem.EBarButtonSystemItem;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StopWithArrivalsAndDeparturesViewController extends
    ListViewController {

  private MobileApplicationCssResource _css = MobileApplicationResources.INSTANCE.getCSS();

  private StopWithArrivalsAndDeparturesListViewModel _model = new StopWithArrivalsAndDeparturesListViewModel();

  private DataRequestTimer _timer = new DataRequestTimer();

  private StopHandler _stopHandler = new StopHandler();

  private final String _stopId;

  private StopBean _stop;

  public StopWithArrivalsAndDeparturesViewController(String stopId) {

    if (stopId == null)
      throw new IllegalArgumentException("stopId must not be null");

    _stopId = stopId;
    setModel(_model);

    NavigationItem navigationItem = getNavigationItem();
    navigationItem.setTitle("Stop");
    navigationItem.setRightBarButtonItem(new BarButtonItem(
        EBarButtonSystemItem.REFRESH, new RefreshClickHandler()));
  }

  public String getStopId() {
    return _stopId;
  }

  /****
   * {@link ViewController} Interface
   ****/

  @Override
  protected void loadView() {
    super.loadView();
    _view.addStyleName(_css.StopWithArrivalAndDeparturesListViewController());
  }

  @Override
  public void viewDidAppear() {
    super.viewDidAppear();
    refresh();
    _timer.cancel();
    _timer.scheduleRepeating(30 * 1000);
  }

  @Override
  public void viewWillDisappear() {
    super.viewWillDisappear();
    _timer.cancel();
  }

  /****
   * Private Methods
   ****/

  private void refresh() {
    System.out.println("refresh");
    WebappServiceAsync service = WebappServiceAsync.SERVICE;
    service.getArrivalsByStopId(_stopId, _stopHandler);
  }

  private class DataRequestTimer extends Timer {
    @Override
    public void run() {
      refresh();
    }
  }

  private class StopHandler implements
      AsyncCallback<StopWithArrivalsAndDeparturesBean> {

    @Override
    public void onSuccess(StopWithArrivalsAndDeparturesBean bean) {

      if (_stop == null || !_stop.getId().equals(bean.getStop().getId())) {
        _stop = bean.getStop();
        ActivityHandler handler = MobileApplicationContext.getActivityHandler();
        handler.onStopAccessed(_stop);
      }

      _model.setData(bean);
      refreshModel();
    }

    @Override
    public void onFailure(Throwable ex) {
      ex.printStackTrace();
    }
  }

  private class RefreshClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent arg0) {
      refresh();
    }
  }

}
