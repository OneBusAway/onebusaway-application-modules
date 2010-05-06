package org.onebusaway.webapp.gwt.common.context;


import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContextManager implements ContextManager {

  private List<ContextListener> _listeners = new ArrayList<ContextListener>();

  private Context _context = null;

  public AbstractContextManager() {

  }

  public void addContextListener(ContextListener listener) {
    _listeners.add(listener);
  }

  public void removeContextListener(ContextListener listener) {
    _listeners.remove(listener);
  }

  public Context getContext() {
    return _context;
  }

  protected void setInternalContext(Context context) {
    _context = context;
  }

  protected void fireContextChanged(Context context) {
    _context = context;
    for (ContextListener listener : _listeners)
      listener.onContextChanged(context);
  }
}
