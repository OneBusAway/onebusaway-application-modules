package org.onebusaway.where.web.sms.actions;


import org.onebusaway.common.replacement.IReplacementStrategy;
import org.onebusaway.where.web.actions.StopByNumberAction;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.rpc.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class StopByTextmarkAction extends StopByNumberAction {

  private static final long serialVersionUID = 1L;

  private String _message;

  private IReplacementStrategy _abbreviations;

  @Autowired
  public void setDestinationAbbreviations(
      @Qualifier("destinationAbbreviations") IReplacementStrategy strategy) {
    _abbreviations = strategy;
  }

  public void setMessage(String message) {
    _message = message.trim();
  }

  @Override
  public String execute() throws ServiceException {

    if (_message == null || _message.length() == 0)
      return INPUT;

    String[] tokens = _message.trim().split("\\s+");

    if (tokens.length == 0)
      return INPUT;

    try {
      setId(tokens[0]);
    } catch (NumberFormatException ex) {
      return INPUT;
    }

    if (tokens.length > 1) {
      StringBuilder b = new StringBuilder();
      for (int i = 1; i < tokens.length; i++) {
        if (b.length() > 0)
          b.append(",");
        b.append(tokens[i]);
      }
      setRoute(b.toString());
    }

    String rc = super.execute();

    if (!rc.equals(SUCCESS))
      return rc;

    if (_abbreviations != null) {

      StopWithArrivalsBean result = getResult();
      for (DepartureBean bean : result.getPredictedArrivals()) {
        String dest = bean.getDestination();
        dest = _abbreviations.replace(dest);
        bean.setDestination(dest);
      }
    }

    return rc;
  }

  @Override
  public String getMinutesLabel(DepartureBean pab, long now) {
    String label = super.getMinutesLabel(pab, now);
    if (label.equals("NOW"))
      return label;
    return label + "m";
  }
}
