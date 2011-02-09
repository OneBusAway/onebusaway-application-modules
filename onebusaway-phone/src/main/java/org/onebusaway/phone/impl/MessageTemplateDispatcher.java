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
