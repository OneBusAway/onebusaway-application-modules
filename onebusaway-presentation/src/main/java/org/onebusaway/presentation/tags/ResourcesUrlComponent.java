package org.onebusaway.presentation.tags;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.struts2.components.ContextBean;
import org.apache.struts2.components.Param;
import org.onebusaway.presentation.services.resources.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class ResourcesUrlComponent extends ContextBean implements
    Param.UnnamedParametric {

  private static final Logger LOG = LoggerFactory.getLogger(ResourcesUrlComponent.class);

  private ResourceService _resourceService;

  private List<String> _resourcePaths = new ArrayList<String>();

  private String _id;

  public ResourcesUrlComponent(ValueStack stack) {
    super(stack);
  }

  @Autowired
  public void setResourceService(ResourceService resourceService) {
    _resourceService = resourceService;
  }

  @Override
  public void addParameter(Object value) {
    _resourcePaths.add(value.toString());
  }
  
  public void setId(String id) {
    _id = id;
  }

  @Override
  public boolean end(Writer writer, String body) {
    
    Locale locale = Locale.getDefault();
    ActionContext ctx = ActionContext.getContext();
    if (ctx != null)
      locale = ctx.getLocale();
    
    String url = _resourceService.getExternalUrlForResources(_id, _resourcePaths, locale);

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
