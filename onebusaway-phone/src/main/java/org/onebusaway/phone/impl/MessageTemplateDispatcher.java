package org.onebusaway.phone.impl;

import org.onebusaway.probablecalls.AgiActionName;
import org.onebusaway.probablecalls.agitemplates.AgiTemplate;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateDispatcher;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

public class MessageTemplateDispatcher extends AgiTemplateDispatcher {

  private static final long serialVersionUID = 1L;

  private MessageSource _messageSource;
  
  public static final String DEFAULT_PARAM = "message";

  public void setMessage(String message) {
    _messageSource = new MessageSource(message);
  }

  @Override
  protected AgiActionName executeTemplate(ActionContext context,
      AgiTemplate template) throws Exception {

    if (_messageSource != null) {
      ValueStack stack = context.getValueStack();
      stack.push(_messageSource);
    }

    return super.executeTemplate(context, template);
  }

  public static class MessageSource {

    private String _message;

    public MessageSource(String message) {
      _message = message;
    }

    public String getMessage() {
      return _message;
    }
  }

}
