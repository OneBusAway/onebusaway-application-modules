package org.onebusaway.transit_data_federation.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.springframework.context.ConfigurableApplicationContext;

public class UtilityLibrary {

  public static List<GtfsBundle> getGtfsBundlesForArguments(List<String> args) {

    List<GtfsBundle> allBundles = new ArrayList<GtfsBundle>();
    List<String> contextPaths = new ArrayList<String>();

    int defaultAgencyIdOffset = 99990;

    for (String arg : args) {

      if (arg.endsWith(".xml"))
        contextPaths.add("file:" + arg);
      else {

        String defaultAgencyId = null;

        int index = arg.indexOf(':');

        if (index != -1) {
          defaultAgencyId = arg.substring(index + 1);
          arg = arg.substring(0, index);
        } else {
          defaultAgencyId = Integer.toString(defaultAgencyIdOffset++);
        }

        GtfsBundle bundle = new GtfsBundle();
        bundle.setPath(new File(arg));
        bundle.setDefaultAgencyId(defaultAgencyId);
        allBundles.add(bundle);
      }
    }

    if (!contextPaths.isEmpty()) {

      // This will get us basic behavior like property expansion
      contextPaths.add(0,
          "classpath:org/onebusaway/container/application-context-common.xml");

      ConfigurableApplicationContext context = ContainerLibrary.createContext(contextPaths);
      GtfsBundles bundles = (GtfsBundles) context.getBean("gtfs-bundles");
      allBundles.addAll(bundles.getBundles());
    }

    return allBundles;
  }
}
