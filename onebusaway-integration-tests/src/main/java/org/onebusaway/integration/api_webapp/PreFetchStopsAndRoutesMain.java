package org.onebusaway.integration.api_webapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.RuleSetBase;
import org.xml.sax.SAXException;

public class PreFetchStopsAndRoutesMain {

  public static void main(String[] args) throws Exception {
    
    String serverName = "soak-api.onebusaway.org";    
    //String serverName = "localhost:8080/onebusaway-api-webapp";
    //String serverName = "aarhus:8080/onebusaway-api-webapp";    
    
    if (args.length == 1)
      serverName = args[0];

    PreFetchStopsAndRoutesMain m = new PreFetchStopsAndRoutesMain();
    m.setServerName(serverName);

    m.run();
    // http://soak-api.onebusaway.org/api/where/agencies-with-coverage.xml?key=org.onebusaway.iphone
  }

  private String _serverName;

  public void setServerName(String serverName) {
    _serverName = serverName;
  }

  public void run() throws Exception {
    List<String> agencyIds = new ArrayList<String>();
    digest("/api/where/agencies-with-coverage.xml", new AgencyRules(),
        agencyIds);
    
    //agencyIds = Arrays.asList("3");
    
    for (String agencyId : agencyIds) {

      System.out.println("agencyId=" + agencyId);

      List<String> routeIds = new ArrayList<String>();
      digest("/api/where/route-ids-for-agency-id/" + agencyId + ".xml",
          new IdRules(), routeIds);

      for (String routeId : routeIds) {
        System.out.println("  routeId=" + routeId);
        digest("/api/where/route/" + routeId + ".xml", new NoRules(),
            new Object());
      }

      for (String routeId : routeIds) {
        System.out.println("  routeId=" + routeId);
        digest("/api/where/stops-for-route/" + routeId + ".xml", new NoRules(),
            new Object());
      }

      List<String> stopIds = new ArrayList<String>();
      digest("/api/where/stop-ids-for-agency-id/" + agencyId + ".xml",
          new IdRules(), stopIds);

      for (String stopId : stopIds) {
        System.out.println("  stopId=" + stopId);
        digest("/api/where/stop/" + stopId + ".xml", new NoRules(),
            new Object());
      }

    }
  }

  private Object digest(String path, RuleSet rules, Object root)
      throws IOException, SAXException {
    Digester digester = new Digester();
    digester.push(root);
    digester.addRuleSet(rules);
    URL url = url(path);
    digester.parse(url.openStream());
    return root;
  }

  private URL url(String path) {
    try {
      return new URL("http://" + _serverName + path
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

  private static class NoRules extends RuleSetBase {

    @Override
    public void addRuleInstances(Digester digester) {

    }
  }

}
