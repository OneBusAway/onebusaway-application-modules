package org.onebusaway.integration.api_webapp;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.RuleSetBase;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.xml.sax.SAXException;

public class FetchArrivalTimesMain {

  public static void main(String[] args) throws Exception {
    FetchArrivalTimesMain m = new FetchArrivalTimesMain();
    m.setServerName("localhost:8080/onebusaway-api-webapp");
    m.run();
  }

  private String _serverName;

  public void setServerName(String serverName) {
    _serverName = serverName;
  }

  public void run() throws Exception {
    List<String> agencyIds = new ArrayList<String>();
    digest("/api/where/agencies-with-coverage.xml", new AgencyRules(),
        agencyIds);

    agencyIds = Arrays.asList("1");

    for (String agencyId : agencyIds) {

      System.out.println("agencyId=" + agencyId);

      List<String> stopIds = new ArrayList<String>();
      digest("/api/where/stop-ids-for-agency-id/" + agencyId + ".xml",
          new IdRules(), stopIds);

      for (String stopId : stopIds) {
        System.out.println("  stopId=" + stopId);
        try {
          digest("/api/where/arrivals-and-departures-for-stop/" + stopId
              + ".xml", new ArrivalTimesRules(),
              new ArrayList<ArrivalAndDepartureBean>());
        } catch (FileNotFoundException ex) {
          System.out.println("    missing stop...");
        }
      }

    }
  }

  private <T> T digest(String path, RuleSet rules, T root) throws IOException,
      SAXException {
    return digest(_serverName, path, rules, root);
  }

  private <T> T digest(String serverName, String path, RuleSet rules, T root)
      throws IOException, SAXException {
    Digester digester = new Digester();
    digester.push(root);
    digester.addRuleSet(rules);
    URL url = url(serverName, path);
    digester.parse(url.openStream());
    return root;
  }

  private URL url(String serverName, String path) {
    try {
      return new URL("http://" + serverName + path
          + "?key=org.onebusaway.iphone");
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

  private static class AgencyRules extends RuleSetBase {
    @Override
    public void addRuleInstances(Digester digester) {
      digester.addCallMethod("response/data/agency-with-coverage/agency/id",
          "add", 0);
    }
  }

  private static class IdRules extends RuleSetBase {
    @Override
    public void addRuleInstances(Digester digester) {
      digester.addCallMethod("response/data/list/string", "add", 0);
    }
  }

  private static class ArrivalTimesRules extends RuleSetBase {

    @Override
    public void addRuleInstances(Digester digester) {

      PropertySetterRule rule = new PropertySetterRule();

      digester.addObjectCreate(
          "response/data/arrivalsAndDepartures/arrivalAndDeparture",
          ArrivalAndDepartureBean.class);
      digester.addRule(
          "response/data/arrivalsAndDepartures/arrivalAndDeparture/tripId",
          rule);
      digester.addRule(
          "response/data/arrivalsAndDepartures/arrivalAndDeparture/predictedArrivalTime",
          rule);
      digester.addRule(
          "response/data/arrivalsAndDepartures/arrivalAndDeparture/scheduledArrivalTime",
          rule);
      digester.addRule(
          "response/data/arrivalsAndDepartures/arrivalAndDeparture/predictedDepartureTime",
          rule);
      digester.addRule(
          "response/data/arrivalsAndDepartures/arrivalAndDeparture/scheduledDepartureTime",
          rule);
      digester.addSetNext(
          "response/data/arrivalsAndDepartures/arrivalAndDeparture", "add");
    }
  }

  private static class PropertySetterRule extends Rule {

    private Map<Class<?>, Map<String, PropertyDescriptor>> _cache = new HashMap<Class<?>, Map<String, PropertyDescriptor>>();

    @Override
    public void body(String namespace, String name, String text)
        throws Exception {

      Object object = digester.peek();
      PropertyDescriptor descriptor = getPropertyDescriptorCached(
          object.getClass(), name);
      Object value = convert(descriptor.getPropertyType(), text);
      Method writeMethod = descriptor.getWriteMethod();
      writeMethod.invoke(object, value);
    }

    private PropertyDescriptor getPropertyDescriptorCached(Class<?> beanType,
        String property) {

      Map<String, PropertyDescriptor> byName = _cache.get(beanType);

      if (byName == null) {
        byName = new HashMap<String, PropertyDescriptor>();
        _cache.put(beanType, byName);
      }

      PropertyDescriptor descriptor = byName.get(property);
      if (descriptor == null) {
        descriptor = getPropertyDescriptor(beanType, property);
        byName.put(property, descriptor);
      }

      return descriptor;
    }

    private PropertyDescriptor getPropertyDescriptor(Class<?> beanType,
        String property) {
      try {
        BeanInfo info = Introspector.getBeanInfo(beanType);
        for (PropertyDescriptor desc : info.getPropertyDescriptors()) {
          if (property.equals(desc.getName()))
            return desc;
        }
        throw new IllegalStateException("no such property: " + property);
      } catch (Exception ex) {
        throw new IllegalStateException(ex);
      }
    }

    private Object convert(Class<?> propertyType, String value) {
      if (propertyType.equals(Long.TYPE) || propertyType.equals(Long.class))
        return Long.parseLong(value);
      if (propertyType.equals(String.class))
        return value;
      throw new IllegalStateException("nope");
    }

  }
}
