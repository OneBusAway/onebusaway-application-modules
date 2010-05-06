package org.onebusaway.kcmetro2gtfs.calendar;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TripScheduleModificationFactoryBean extends AbstractFactoryBean {

  private File _path;

  public void setPath(File path) {
    _path = path;
  }

  public TripScheduleModificationStrategy createModificationStrategy()
      throws IOException {

    BufferedReader reader = new BufferedReader(new FileReader(_path));
    String line = null;
    int lineNumber = 0;

    ModificationImpl impl = new ModificationImpl();

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

    return impl;
  }

  @Override
  public Class<?> getObjectType() {
    return ModificationImpl.class;
  }

  @Override
  protected Object createInstance() throws Exception {
    return createModificationStrategy();
  }

  private MatchSpecification parseMatchSpec(String spec) {

    spec = spec.trim();
    String[] tokens = spec.split("\\s+");
    if (tokens.length == 0)
      throw new IllegalStateException("no matches specified");

    MatchSpecification ms = new MatchSpecification();

    Map<String, String> keyValuePairs = ModificationLibrary.parseKeyValuePairs(tokens, 0, tokens.length);
    
    for(Map.Entry<String,String> entry : keyValuePairs.entrySet() ) {
    
      String key = entry.getKey();
      String value = entry.getValue();

      if (key.equals("date")) {
        try {
          ms.setDates(ModificationLibrary.parseDateSpec(value));
        } catch (ParseException e) {
          throw new IllegalStateException("error parsing date: " + value);
        }
      } else if (key.equals("scheduleType")) {
        ms.setScheduleType(ModificationLibrary.parseStringSpec(value));
      } else if (key.equals("exceptionCode")) {
        ms.setExceptionCode(ModificationLibrary.parseStringSpec(value));
      } else if (key.equals("changeDate")) {
        ms.setChangeDate(ModificationLibrary.parseStringSpec(value));
      } else {
        throw new IllegalStateException("unknown match key: " + key);
      }
    }

    return ms;
  }

  private void parseReplacementSpec(String spec, ModificationImpl impl,
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

    if (action.equals("add")) {
      try {
        Set<Date> dates = ModificationLibrary.parseDateSpec(args.get("date"));
        impl.addAdditions(match, dates);
      } catch (ParseException ex) {
        throw new IllegalStateException("error parsing dates for add action");
      }
    } else if (action.equals("remove")) {
      Set<Date> dates = match.getDates();
      impl.addCancellations(match, dates);
    } else if (action.equals("modify")) {
      ServiceIdModificationImpl mod = new ServiceIdModificationImpl();
      Set<Date> dates = match.getDates();
      mod.setDates(dates);
      String offset = args.get("offset");
      if (offset != null)
        mod.setOffset(Integer.parseInt(offset));
      String suffix = args.get("suffix");
      if (suffix != null)
        mod.setSuffix(suffix);
      impl.addModifications(match, mod);
    } else {
      throw new IllegalStateException("unknown action: " + action);
    }
  }

  private static class ModificationImpl implements
      TripScheduleModificationStrategy {

    private Map<MatchSpecification, Set<Date>> _additions = new HashMap<MatchSpecification, Set<Date>>();

    private Map<MatchSpecification, Set<Date>> _cancellations = new HashMap<MatchSpecification, Set<Date>>();

    private Map<MatchSpecification, Set<ServiceIdModificationImpl>> _modifications = new HashMap<MatchSpecification, Set<ServiceIdModificationImpl>>();

    public void addAdditions(MatchSpecification match, Set<Date> dates) {
      getValuesForMatch(_additions, match).addAll(dates);
    }

    public void addCancellations(MatchSpecification match, Set<Date> dates) {
      getValuesForMatch(_cancellations, match).addAll(dates);
    }

    public void addModifications(MatchSpecification match,
        ServiceIdModificationImpl modification) {
      getValuesForMatch(_modifications, match).add(modification);
    }

    public Set<Date> getAdditions(MetroKCServiceId key, Set<Date> dates) {
      return getMatches(_additions, key, dates);
    }

    public Set<Date> getCancellations(MetroKCServiceId key, Set<Date> dates) {
      Set<Date> toCancel = new HashSet<Date>(dates);
      Set<Date> cancelled = getMatches(_cancellations, key, dates);
      toCancel.retainAll(cancelled);
      return toCancel;
    }

    public Set<ServiceIdModificationImpl> getModifications(
        MetroKCServiceId key, Set<Date> dates) {
      return getMatches(_modifications, key, dates);
    }

    private <T> Set<T> getValuesForMatch(
        Map<MatchSpecification, Set<T>> matches, MatchSpecification match) {
      Set<T> values = matches.get(match);
      if (values == null) {
        values = new HashSet<T>();
        matches.put(match, values);
      }
      return values;
    }

    private <T> Set<T> getMatches(Map<MatchSpecification, Set<T>> matches,
        MetroKCServiceId key, Set<Date> dates) {
      Set<T> results = new HashSet<T>();
      for (Map.Entry<MatchSpecification, Set<T>> entry : matches.entrySet()) {
        MatchSpecification match = entry.getKey();
        if (match.hasMatch(key, dates))
          results.addAll(entry.getValue());
      }
      return results;
    }
  }

  private static class MatchSpecification {
    private Set<String> _scheduleType;
    private Set<String> _exceptionCode;
    private Set<String> _changeDates;
    private Set<Date> _dates;

    public void setScheduleType(Set<String> scheduleType) {
      _scheduleType = scheduleType;
    }

    public void setExceptionCode(Set<String> exceptionCode) {
      _exceptionCode = exceptionCode;
    }

    public void setChangeDate(Set<String> changeDate) {
      _changeDates = changeDate;
    }

    public void setDates(Set<Date> dates) {
      _dates = dates;
    }

    public Set<Date> getDates() {
      return _dates;
    }

    public boolean hasMatch(MetroKCServiceId key, Set<Date> dates) {

      if (_changeDates != null && !key.getChangeDateIds().equals(_changeDates))
        return false;

      if (_scheduleType != null
          && !_scheduleType.contains(key.getScheduleType()))
        return false;

      if (_exceptionCode != null
          && !_exceptionCode.contains(key.getExceptionCode()))
        return false;

      if (this._dates != null) {
        for (Date date : this._dates) {
          if (dates.contains(date))
            return true;
        }
        return false;
      }

      return true;
    }
  }
}
