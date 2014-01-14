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
package org.onebusaway.phone.impl;

import org.onebusaway.probablecalls.AgiActionName;
import org.onebusaway.probablecalls.agitemplates.AgiTemplate;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateDispatcher;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

public class MessageTemplateDispatcher extends AgiTemplateDispatcher {

  private static final long serialVersionUID = 1L;

  private String _message;

  private String _nextAction;
  
  public static final String DEFAULT_PARAM = "message";

  public void setMessage(String message) {
    _message = message;
  }
  
  public void setNextAction(String nextAction) {
    _nextAction = nextAction;
  }
  
  public String getNextAction() {
    return _nextAction;
  }

  @Override
  protected AgiActionName executeTemplate(ActionContext context,
      AgiTemplate template) throws Exception {

    if (_message != null) {
      ValueStack stack = context.getValueStack();
      MessageSource source = new MessageSource(_message,_nextAction);
      stack.push(source);
    }

    return super.executeTemplate(context, template);
  }

  public static class MessageSource {

    private String _message;
    
    private String _nextAction;

    public MessageSource(String message, String nextAction) {
      _message = message;
      _nextAction = nextAction;
    }

    public String getMessage() {
      return _message;
    }
    
    public void setNextAction(String nextAction) {
      _nextAction = nextAction;
    }
    
    public String getNextAction() {
      return _nextAction;
    }
  }
}
