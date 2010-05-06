package org.onebusaway.tripplanner.web.standard.client.control;

import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.TripPlannerConstraintsBean;
import org.onebusaway.tripplanner.web.common.client.rpc.TripPlannerWebServiceAsync;
import org.onebusaway.tripplanner.web.standard.client.view.SearchWidget;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public class GeocoderHandlers {

  private Geocoder _geocoder = new Geocoder();

  private SearchWidget _searchWidget;

  private AsyncCallback<List<TripBean>> _tripPlannerResultHandler;

  private String _startAddress;

  private Placemark _startLocation;

  private String _destinationAddress;

  private Placemark _destinationLocation;

  private LatLngBounds _view;

  public GeocoderHandlers() {
    _view = LatLngBounds.newInstance();
    _view.extend(LatLng.newInstance(47.97430795395781, -121.79454591726969));
    _view.extend(LatLng.newInstance(47.152554314370924, -122.50104172828858));
    _geocoder.setViewport(_view);
  }

  public void setSearchWidget(SearchWidget widget) {
    _searchWidget = widget;
  }

  public void setTripPlannerResultHandler(AsyncCallback<List<TripBean>> handler) {
    _tripPlannerResultHandler = handler;
  }

  public void query(String startAddress, String destinationAddress) {

    _startAddress = startAddress;
    _startLocation = null;

    _destinationAddress = destinationAddress;
    _destinationLocation = null;

    _geocoder.getLocations(startAddress, new LocationHandler(false));
    _geocoder.getLocations(destinationAddress, new LocationHandler(true));
  }

  public void setStartLocation(Placemark start) {
    _startLocation = start;
    checkResults();
  }

  public void setDestinationLocation(Placemark destination) {
    _destinationLocation = destination;
    checkResults();
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private void handleResults(boolean isDestination, JsArray<Placemark> locations) {

    System.out.println("isDestination=" + isDestination + " count=" + locations.length());

    if (locations.length() == 0) {
      _searchWidget.setAddressError(isDestination);
    } else if (locations.length() > 1) {

      Placemark placeHit = null;
      int hits = 0;

      for (int i = 0; i < locations.length(); i++) {
        Placemark place = locations.get(i);
        if (_view.containsLatLng(place.getPoint())) {
          placeHit = place;
          hits++;
        }
      }

      if (hits == 1) {

        System.out.println("found single hit by filtering using bounds");

        if (isDestination)
          _destinationLocation = placeHit;
        else
          _startLocation = placeHit;
        checkResults();
      } else {
        _searchWidget.setMultipleAddresses(isDestination, locations);
      }
    } else {
      if (isDestination)
        _destinationLocation = locations.get(0);
      else
        _startLocation = locations.get(0);
      checkResults();
    }
  }

  private void checkResults() {
    
    System.out.println("checking=" + _startLocation + " to " + _destinationLocation);
    
    if (_startLocation != null && _destinationLocation != null) {

      LatLng from = _startLocation.getPoint();
      LatLng to = _destinationLocation.getPoint();
      TripPlannerConstraintsBean constraints = _searchWidget.getConstraints();

      TripPlannerWebServiceAsync service = TripPlannerWebServiceAsync.SERVICE;

      System.out.println("here we go");
      service.getTripsBetween(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude(),
          constraints, _tripPlannerResultHandler);
    }
  }

  private class LocationHandler implements LocationCallback {

    private boolean _isDestinationHandler;

    public LocationHandler(boolean isDestinationHandler) {
      _isDestinationHandler = isDestinationHandler;
    }

    public void onSuccess(JsArray<Placemark> locations) {
      handleResults(_isDestinationHandler, locations);
    }

    public void onFailure(int statusCode) {
      System.err.println("error");
      _searchWidget.setAddressError(_isDestinationHandler);
    }
  }
}
