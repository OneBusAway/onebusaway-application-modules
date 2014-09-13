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

    String uri = request.getRequestURI();

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
