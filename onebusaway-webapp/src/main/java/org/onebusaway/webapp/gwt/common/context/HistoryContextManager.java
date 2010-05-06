package org.onebusaway.webapp.gwt.common.context;

import org.onebusaway.webapp.gwt.common.url.ParaUrlCodingStrategy;
import org.onebusaway.webapp.gwt.common.url.UrlCodingStrategy;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

import java.util.Map;

public class HistoryContextManager extends AbstractContextManager implements
    ValueChangeHandler<String> {

  private UrlCodingStrategy _codingStrategy = new ParaUrlCodingStrategy();

  public HistoryContextManager() {
    History.addValueChangeHandler(this);
    String current = History.getToken();
    if (current != null) {
      setInternalContext(parseContext(current));
    }
  }

  public void setUrlCodingStrategy(UrlCodingStrategy strategy) {
    _codingStrategy = strategy;
  }

  @Override
  public void setContext(Context context) {
    String token = getContextAsString(context);
    History.newItem(token);
  }

  @Override
  public String getContextAsString(Context context) {
    return _codingStrategy.getParamMapAsString(context.getParams());
  }

  public void onValueChange(ValueChangeEvent<String> event) {
    String token = event.getValue();
    ContextImpl context = parseContext(token);
    fireContextChanged(context);
  }

  private ContextImpl parseContext(String token) {
    Map<String, String> m = _codingStrategy.getParamStringAsMap(token);
    return new ContextImpl(m);
  }

}
