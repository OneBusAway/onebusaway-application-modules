package org.onebusaway.where.web.sms.actions;

import org.onebusaway.common.replacement.IReplacementStrategy;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.rpc.WhereService;

import edu.emory.mathcs.backport.java.util.Collections;

import com.opensymphony.xwork2.ActionSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StopByTextmarkAction extends ActionSupport {

  private static final long serialVersionUID = 1L;
  
  private static final SortByTime _sort = new SortByTime();

  private WhereService _service;

  private IReplacementStrategy _abbreviations;

  private String _message;

  private StopWithArrivalsBean _result;

  @Autowired
  public void setWhereService(WhereService service) {
    _service = service;
  }

  @Autowired
  public void setDestinationAbbreviations(
      @Qualifier("destinationAbbreviations") IReplacementStrategy strategy) {
    _abbreviations = strategy;
  }

  public void setMessage(String message) {
    _message = message.trim();
  }

  public StopWithArrivalsBean getResult() {
    return _result;
  }

  @Override
  public String execute() throws ServiceException {

    if (_message == null || _message.length() == 0)
      return INPUT;

    String[] tokens = _message.trim().split("\\s+");

    if (tokens.length == 0)
      return INPUT;

    _result = _service.getArrivalsByStopId(tokens[0]);

    // Filter by route
    if (tokens.length > 1) {
      Set<String> routes = new HashSet<String>();
      for (int i = 1; i < tokens.length; i++) {
        String[] routeNames = tokens[i].split(",");
        for (String routeName : routeNames)
          routes.add(routeName);
      }
      Iterator<DepartureBean> it = _result.getPredictedArrivals().iterator();
      while (it.hasNext()) {
        DepartureBean bean = it.next();
        if (!routes.contains(bean.getRoute()))
          it.remove();
      }
    }

    if (_abbreviations != null) {

      for (DepartureBean bean : _result.getPredictedArrivals()) {
        String dest = bean.getDestination();
        dest = _abbreviations.replace(dest);
        bean.setDestination(dest);
      }
    }
    
    // Sort results
    Collections.sort(_result.getPredictedArrivals(),_sort);
    
    return SUCCESS;
  }

  public long getNow() {
    return System.currentTimeMillis();
  }

  public String getMinutesLabel(DepartureBean pab, long now) {
    long t = pab.getBestTime();
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    boolean isNow = Math.abs(minutes) <= 1;
    return isNow ? "NOW" : (Integer.toString(minutes) + "m");
  }
  
  private static class SortByTime implements Comparator<DepartureBean> {
    public int compare(DepartureBean o1, DepartureBean o2) {
      long a = o1.getBestTime();
      long b = o2.getBestTime();
      return a == b ? 0 : (a < b ? -1 : 1);
    }
  }
}
