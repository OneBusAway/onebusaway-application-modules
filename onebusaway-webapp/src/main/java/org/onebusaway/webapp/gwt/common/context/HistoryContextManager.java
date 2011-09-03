/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
