package org.onebusaway.presentation.tags;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.components.ContextBean;
import org.apache.struts2.components.Param;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.util.CompoundRoot;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class MessageComponent extends ContextBean implements
    Param.UnnamedParametric {

  private static final Logger LOG = LoggerFactory.getLogger(MessageComponent.class);

  private String _key;

  private List<Object> _arguments = new ArrayList<Object>();

  public MessageComponent(ValueStack stack) {
    super(stack);
  }

  public void setKey(String key) {
    _key = key;
  }

  /****
   * {@link Param.UnnamedParametric} Interface
   ****/

  @Override
  public void addParameter(Object value) {
    _arguments.add(value);
  }

  @Override
  public boolean end(Writer writer, String body) {

    if (_key != null) {

      ValueStack stack = getStack();
      CompoundRoot root = stack.getRoot();
      TextProvider textProvider = null;
      
      for (Object obj : root) {
        if (obj instanceof TextProvider) {
          textProvider = (TextProvider) obj;
          break;
        }
      }

      if (textProvider != null) {
        String message = textProvider.getText(_key, _arguments);
        if (message != null) {
          try {
            writer.write(message);
          } catch (IOException e) {
            LOG.error("Could not write out tag", e);
          }
        }
      }
    }

    return super.end(writer, "");
  }

}
