package org.onebusaway.federations.webapp;

import org.onebusaway.federations.FederatedServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/delete-service.action")
public class DeleteServiceController {

  @Autowired
  private FederatedServiceRegistry _registry;

  @RequestMapping()
  public ModelAndView index(@RequestParam() String url) {
    _registry.removeService(url);
    return new ModelAndView("redirect:index.action");
  }
}
