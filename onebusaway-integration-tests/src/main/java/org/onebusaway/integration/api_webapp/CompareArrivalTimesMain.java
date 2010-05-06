package org.onebusaway.integration.api_webapp;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.RuleSetBase;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.xml.sax.SAXException;

import edu.washington.cs.rse.collections.CollectionsLibrary;

public class CompareArrivalTimesMain {

  private DateFormat _format = new SimpleDateFormat("HH:mm:ss");

  public static void main(String[] args) throws Exception {
    CompareArrivalTimesMain m = new CompareArrivalTimesMain();
    m.setServerNameA("localhost:8080/onebusaway-api-webapp");
    m.setServerNameB("api.onebusaway.org");
    m.run();
  }

  private String _serverNameA;
  
  private String _serverNameB;

  public void setServerNameA(String serverNameA) {
    _serverNameA = serverNameA;
  }
  
  public void setServerNameB(String serverNameB) {
    _serverNameB = serverNameB;
  }

  public void run() throws Exception {
    List<String> agencyIds = new ArrayList<String>();
    digest("/api/where/agencies-with-coverage.xml", new AgencyRules(),
        agencyIds);

    agencyIds = Arrays.asList("1");

    for (String agencyId : agencyIds) {

      System.out.println("agencyId=" + agencyId);

      List<String> routeIds = new ArrayList<String>();
      digest("/api/where/route-ids-for-agency-id/" + agencyId + ".xml",
          new IdRules(), routeIds);

      List<String> stopIds = new ArrayList<String>();
      digest("/api/where/stop-ids-for-agency-id/" + agencyId + ".xml",
          new IdRules(), stopIds);

      for (String stopId : stopIds) {
        System.out.println("  stopId=" + stopId);
        try {
          ArrayList<ArrivalAndDepartureBean> arrivalsAndDeparturesA = digest(
              _serverNameA,
              "/api/where/arrivals-and-departures-for-stop/" + stopId + ".xml",
              new ArrivalTimesRules(), new ArrayList<ArrivalAndDepartureBean>());
          ArrayList<ArrivalAndDepartureBean> arrivalsAndDeparturesB = digest(
              _serverNameB,
              "/api/where/arrivals-and-departures-for-stop/" + stopId + ".xml",
              new ArrivalTimesRules(), new ArrayList<ArrivalAndDepartureBean>());

          compareArrivalsAndDepartures(stopId, arrivalsAndDeparturesA,
              arrivalsAndDeparturesB);
        } catch (FileNotFoundException ex) {
          System.out.println("    missing stop...");
        }
      }

    }
  }

  private void compareArrivalsAndDepartures(String stopId,
      List<ArrivalAndDepartureBean> arrivalsAndDeparturesA,
      List<ArrivalAndDepartureBean> arrivalsAndDeparturesB) {

    Map<String, ArrivalAndDepartureBean> byTripIdA = getByTripId(arrivalsAndDeparturesA);
    Map<String, ArrivalAndDepartureBean> byTripIdB = getByTripId(arrivalsAndDeparturesB);

    Set<String> allIds = new HashSet<String>();
    allIds.addAll(byTripIdA.keySet());
    allIds.addAll(byTripIdB.keySet());

    for (String tripId : allIds) {
      ArrivalAndDepartureBean beanA = byTripIdA.get(tripId);
      ArrivalAndDepartureBean beanB = byTripIdB.get(tripId);
      if (beanA == null) {
        System.out.println("  only in b: " + desc(beanB));
      } else if (beanB == null) {
        System.out.println("  only in a: " + desc(beanA));
      } else {
        if (hasDiff(beanA.getScheduledArrivalTime(),
            beanB.getScheduledArrivalTime(), 5 * 1000))
          System.out.println("    tripId=" + tripId + " schedDiff: a="
              + time(beanA.getScheduledArrivalTime()) + " b="
              + time(beanB.getScheduledArrivalTime()));
        if (hasDiff(beanA.getPredictedArrivalTime(),
            beanB.getPredictedArrivalTime(), 60 * 1000)) {
          System.out.println("    tripId=" + tripId + " schedDiff: a="
              + time(beanA.getScheduledArrivalTime()) + " b="
              + time(beanB.getScheduledArrivalTime()));
          System.out.println("    tripId=" + tripId + " predDiff: a="
              + time(beanA.getPredictedArrivalTime()) + " b="
              + time(beanB.getPredictedArrivalTime()));
        }

      }
    }
  }

  private boolean hasDiff(long a, long b, long delta) {
    return Math.abs(b - a) > delta;
  }

  private String desc(ArrivalAndDepartureBean bean) {
    return "tripId=" + bean.getTrip().getId() + " sched="
        + time(bean.getScheduledArrivalTime()) + " pred="
        + time(bean.getPredictedArrivalTime());
  }

  private String time(long value) {
    if (value <= 0)
      return "xx:xx:xx";
    return _format.format(new Date(value));
  }

  private Map<String, ArrivalAndDepartureBean> getByTripId(
      List<ArrivalAndDepartureBean> beans) {
    return CollectionsLibrary.mapToValue(beans, "tripId", String.class);
  }

  private <T> T digest(String path, RuleSet rules, T root) throws IOException,
      SAXException {
    return digest(_serverNameA, path, rules, root);
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
