package org.onebusaway.presentation.tags;

import java.io.IOException;
import java.io.Writer;

import org.apache.struts2.components.ContextBean;
import org.onebusaway.presentation.services.resources.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class ResourceUrlComponent extends ContextBean {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceUrlComponent.class);

  private ResourceService _resourceService;

  private String _value;

  public ResourceUrlComponent(ValueStack stack) {
    super(stack);
  }

  @Autowired
  public void setResourceService(ResourceService resourceService) {
    _resourceService = resourceService;
  }

  public void setValue(String value) {
    _value = value;
  }

  @Override
  public boolean end(Writer writer, String body) {

    if (_value == null)
      _value = "top";

    String value = findStringIfAltSyntax(_value);

    String url = _resourceService.getExternalUrlForResource(value);

    if (url != null) {
      if (getVar() != null) {
        /**
         * We either write the url out to a variable
         */
        putInContext(url);
      } else {
        /**
         * Or otherwise print out the url directly
         */
        try {
          writer.write(url);
        } catch (IOException e) {
          LOG.error("Could not write out resource-url tag", e);
        }
      }
    }

    return super.end(writer, "");
  }
}
