package org.onebusaway.federations.webapp;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.federations.FederatedServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/add-service.action")
public class AddServiceController {

  @Autowired
  private FederatedServiceRegistry _registry;

  @RequestMapping()
  public ModelAndView index(@RequestParam() String url,
      @RequestParam() String serviceClass, @RequestParam() String properties) {
    
    Map<String, String> propsAsMap = parseProperties(properties);
    _registry.addService(url, serviceClass, propsAsMap);
    
    return new ModelAndView("redirect:index.action");
  }

  private Map<String, String> parseProperties(String params) {

    Map<String, String> m = new HashMap<String, String>();

    String[] tokens = params.split("&");

    for (String token : tokens) {
      int index = token.indexOf('=');
      if (index != -1) {
        String key = token.substring(0, index);
        String value = token.substring(index + 1);
        if (key.length() > 0 && value.length() > 0)
          m.put(key, value);
      }
    }

    return m;
  }
}
