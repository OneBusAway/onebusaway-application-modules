package org.onebusaway.presentation.impl.struts;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.dispatcher.mapper.ActionMapper;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;

import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.inject.Inject;

public class MultiActionMapper extends DefaultActionMapper {

  public static final String MAPPINGS = "org.onebusaway.presentation.impl.struts.MultiActionMapper.mapping";

  private List<Prefixed<ActionMapper>> _prefixedActionMappers = new ArrayList<Prefixed<ActionMapper>>();

  @Inject(MAPPINGS)
  public void setActionMappers(String list) {
    if (list != null) {
      String[] tokens = list.split(",");
      for (String token : tokens) {
        String[] kvp = token.split("=");
        String key = kvp[0];
        String name = kvp[1];
        ActionMapper mapper = container.getInstance(ActionMapper.class, name);
        if (mapper != null) {
          _prefixedActionMappers.add(new Prefixed<ActionMapper>(key, mapper));
        } else {
          throw new IllegalStateException("unknown ActionMapper " + name);
        }
      }
    }
  }

  /****
   * {@link ActionMapper}
   */

  @Override
  public ActionMapping getMapping(HttpServletRequest request,
      ConfigurationManager configManager) {

    String uri = getUri(request);

    for (Prefixed<ActionMapper> prefixedActionMapper : _prefixedActionMappers) {

      String prefix = prefixedActionMapper.getPrefix();
      if (uri.startsWith(prefix)) {
        ActionMapper actionMapper = prefixedActionMapper.getValue();
        return actionMapper.getMapping(request, configManager);
      }
    }

    throw new IllegalArgumentException("no ActionMapper for uri: " + uri);
  }
}
