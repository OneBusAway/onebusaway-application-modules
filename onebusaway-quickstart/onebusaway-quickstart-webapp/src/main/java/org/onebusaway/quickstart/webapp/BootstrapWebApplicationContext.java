/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.quickstart.webapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.cli.CommandLine;
import org.onebusaway.quickstart.WebappCommon;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * A custom extension of {@link XmlWebApplicationContext} that loads custom
 * Spring bean definitions as determined by command-line arguments to the
 * quickstart webapp. See {@link WebappCommon} for a list of the command-line
 * arguments. This class expects an instance of {@link CommandLine} to be stored
 * in a {@link ServletContext} attribute identified by the key
 * {@link WebappCommon#COMMAND_LINE_CONTEXT_ATTRIBUTE}.
 * 
 * @author bdferris
 * 
 */
public class BootstrapWebApplicationContext extends XmlWebApplicationContext {

  @Override
  protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory)
      throws BeansException, IOException {
    super.loadBeanDefinitions(beanFactory);
    
    System.out.println("=== BootstrapWebApplicationContext! ============================");

    ServletContext servletContext = getServletContext();
    final CommandLine cli = (CommandLine) servletContext.getAttribute(WebappCommon.COMMAND_LINE_CONTEXT_ATTRIBUTE);
    if (cli == null)
      throw new IllegalStateException(
          "expected a CommandLine object stored in the webapp context");

    final Map<String, BeanDefinition> beanDefinitions = configureBeanDefinitions(cli);
    if (beanDefinitions.isEmpty())
      return;

    for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
      String beanName = entry.getKey();
      BeanDefinition beanDefinition = entry.getValue();
      beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }
  }

  protected Map<String, BeanDefinition> configureBeanDefinitions(CommandLine cli) {
    Map<String, BeanDefinition> beanDefinitions = new HashMap<String, BeanDefinition>();
    configureGtfsRealtimeBeanDefinition(cli, beanDefinitions);
    return beanDefinitions;
  }

  private void configureGtfsRealtimeBeanDefinition(CommandLine cli,
      Map<String, BeanDefinition> beanDefinitions) {
    boolean tripUpdates = cli.hasOption(WebappCommon.ARG_GTFS_REALTIME_TRIP_UPDATES_URL);
    boolean vehiclePositions = cli.hasOption(WebappCommon.ARG_GTFS_REALTIME_VEHICLE_POSITIONS_URL);
    boolean alertsUrl = cli.hasOption(WebappCommon.ARG_GTFS_REALTIME_ALERTS_URL);

    if (tripUpdates || vehiclePositions || alertsUrl) {
      BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition("org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource");
      if (tripUpdates)
        bean.addPropertyValue("tripUpdatesUrl",
            cli.getOptionValue(WebappCommon.ARG_GTFS_REALTIME_TRIP_UPDATES_URL));
      if (vehiclePositions)
        bean.addPropertyValue(
            "vehiclePositionsUrl",
            cli.getOptionValue(WebappCommon.ARG_GTFS_REALTIME_VEHICLE_POSITIONS_URL));
      if (alertsUrl)
        bean.addPropertyValue("alertsUrl",
            cli.getOptionValue(WebappCommon.ARG_GTFS_REALTIME_ALERTS_URL));
      if (cli.hasOption(WebappCommon.ARG_GTFS_REALTIME_REFRESH_INTERVAL)) {
        String refreshInterval = cli.getOptionValue(WebappCommon.ARG_GTFS_REALTIME_REFRESH_INTERVAL);
        bean.addPropertyValue("refreshInterval", refreshInterval);
      }
      beanDefinitions.put("gtfsRealtimeSource", bean.getBeanDefinition());
      System.out.println("=== GTFS REALTIME! ============================");
    }
    
    if (cli.hasOption("P")) {
      Properties props = cli.getOptionProperties("P");
      BeanDefinitionBuilder propertyOverrides = BeanDefinitionBuilder.genericBeanDefinition("org.onebusaway.container.spring.PropertyOverrideConfigurer");
      propertyOverrides.addPropertyValue("properties", props);
      beanDefinitions.put("myCustomPropertyOverrides",
          propertyOverrides.getBeanDefinition());
    }
  }
}