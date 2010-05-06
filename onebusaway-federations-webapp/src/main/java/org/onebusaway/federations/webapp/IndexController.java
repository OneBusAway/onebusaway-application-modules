package org.onebusaway.federations.webapp;

import java.util.List;

import org.onebusaway.federations.FederatedServiceRegistry;
import org.onebusaway.federations.FederatedServiceRegistryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/index.action")
public class IndexController {

  @Autowired
  private FederatedServiceRegistry _registry;

  @RequestMapping()
  public ModelAndView index() {
    List<FederatedServiceRegistryEntry> entries = _registry.getAllServices();
    ModelAndView mv = new ModelAndView("index.jspx", "entries", entries.iterator());
    mv.addObject("v", "hello");
    return mv;
  }
}
