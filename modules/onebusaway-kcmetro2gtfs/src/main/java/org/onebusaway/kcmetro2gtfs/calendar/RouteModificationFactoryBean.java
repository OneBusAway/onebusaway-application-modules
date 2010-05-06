package org.onebusaway.kcmetro2gtfs.calendar;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.kcmetro2gtfs.TranslationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteModificationFactoryBean {

  private static Pattern _keyValuePattern = Pattern.compile("^(.*)=(.*)$");

  private File _resource;

  public void setResource(File resource) {
    _resource = resource;
  }

  public RouteModificationsStrategy createInstance() throws Exception {

    BufferedReader reader = new BufferedReader(new FileReader(_resource));
    String line = null;
    int lineNumber = 0;

    RouteModificationsImpl impl = new RouteModificationsImpl();

    while ((line = reader.readLine()) != null) {
      lineNumber++;
      line = line.trim();
      if (line.length() == 0 || line.startsWith("#"))
        continue;
      try {
        String[] tokens = line.split("\\s+=>\\s+");
        if (tokens.length != 2)
          throw new IllegalStateException(
              "expected line of form \"MATCHES => ACTION\"");
        MatchSpecification ms = parseMatchSpec(tokens[0]);
        parseReplacementSpec(tokens[1], impl, ms);
      } catch (Exception ex) {
        throw new IllegalStateException("error parsing line: number="
            + lineNumber + " line=" + line, ex);
      }
    }

    reader.close();

    return impl;
  }

  /****
   * Private Methods
   ****/

  private MatchSpecification parseMatchSpec(String spec) {

    spec = spec.trim();
    String[] tokens = spec.split("\\s+");
    if (tokens.length == 0)
      throw new IllegalStateException("no matches specified");

    MatchSpecification ms = new MatchSpecification();

    for (String token : tokens) {
      Matcher m = _keyValuePattern.matcher(token);
      if (!m.matches())
        throw new IllegalStateException("invalid match specification: " + token);
      String key = m.group(1);
      String value = m.group(2);

      if (key.equals("shortName")) {
        ms.setShortNames(ModificationLibrary.parseStringSpec(value));
      } else {
        throw new IllegalStateException("unknown match key: " + key);
      }
    }

    return ms;
  }

  private void parseReplacementSpec(String spec, RouteModificationsImpl impl,
      MatchSpecification match) {
    spec = spec.trim();
    if (spec.length() == 0)
      throw new IllegalStateException("no action specified");
    String[] tokens = spec.split("\\s+");
    if (tokens.length == 0)
      throw new IllegalStateException("no action specified");

    String action = tokens[0];
    Map<String, String> args = ModificationLibrary.parseKeyValuePairs(tokens,
        1, tokens.length);

    if (action.equals("modify")) {
      RouteModificationImpl mod = new RouteModificationImpl();

      String type = args.get("type");
      if (type != null)
        mod.setType(Integer.parseInt(type));

      String agency = args.get("agency");
      if (agency != null)
        mod.setAgency(agency);

      impl.addModifications(match, mod);
    } else {
      throw new IllegalStateException("unknown action: " + action);
    }
  }

  private static class MatchSpecification {

    private Set<String> _shortNames;

    public void setShortNames(Set<String> shortNames) {
      _shortNames = shortNames;
    }

    public boolean hasMatch(Route route) {

      if (_shortNames != null && !_shortNames.contains(route.getShortName())) {
        return false;
      }

      return true;
    }
  }

  private static class RouteModificationsImpl implements
      RouteModificationsStrategy {

    private Map<MatchSpecification, RouteModificationImpl> _modifications = new HashMap<MatchSpecification, RouteModificationImpl>();

    public void addModifications(MatchSpecification match,
        RouteModificationImpl mod) {
      _modifications.put(match, mod);
    }

    public void modifyRoute(TranslationContext context, Route route) {
      for (Map.Entry<MatchSpecification, RouteModificationImpl> entry : _modifications.entrySet()) {

        MatchSpecification match = entry.getKey();

        if (match.hasMatch(route)) {

          RouteModificationImpl mod = entry.getValue();

          if (mod.hasType())
            route.setType(mod.getType());

          if (mod.getAgency() != null) {

            route.setAgency(context.getAgencyForId(mod.getAgency()));

            AgencyAndId id = route.getId();
            id = new AgencyAndId(mod.getAgency(), id.getId());
            route.setId(id);
          }
        }
      }
    }
  }

  private static class RouteModificationImpl {

    private int _type;

    private boolean _hasType = false;

    private String _agency;

    public void setType(int type) {
      _type = type;
      _hasType = true;
    }

    public void setAgency(String agency) {
      _agency = agency;
    }

    public String getAgency() {
      return _agency;
    }

    public boolean hasType() {
      return _hasType;
    }

    public int getType() {
      return _type;
    }
  }

}
