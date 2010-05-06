package org.onebusaway.common.web.common.client.context;

import org.onebusaway.common.web.common.client.url.ParaUrlCodingStrategy;
import org.onebusaway.common.web.common.client.url.UrlCodingStrategy;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;

import java.util.Map;

public class HistoryContextManager extends AbstractContextManager implements HistoryListener {

  private UrlCodingStrategy _codingStrategy = new ParaUrlCodingStrategy();

  public HistoryContextManager() {
    History.addHistoryListener(this);
    String current = History.getToken();
    if (current != null) {
      setInternalContext(parseContext(current));
    }
  }

  public void setUrlCodingStrategy(UrlCodingStrategy strategy) {
    _codingStrategy = strategy;
  }

  public void setContext(Context context) {
    String token = _codingStrategy.getParamMapAsString(context.getParams());
    History.newItem(token);
  }

  public void onHistoryChanged(String token) {
    ContextImpl context = parseContext(token);
    fireContextChanged(context);
  }

  private ContextImpl parseContext(String token) {
    Map<String, String> m = _codingStrategy.getParamStringAsMap(token);
    return new ContextImpl(m);
  }
}
