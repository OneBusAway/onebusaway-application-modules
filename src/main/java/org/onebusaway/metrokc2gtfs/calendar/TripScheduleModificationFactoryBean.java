package org.onebusaway.metrokc2gtfs.calendar;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TripScheduleModificationFactoryBean extends AbstractFactoryBean {

  private static Pattern _keyValuePattern = Pattern.compile("^(.*)=(.*)$");

  private static final DateFormat _format = new SimpleDateFormat("yyyy.MM.dd");

  private File _path;

  public void setPath(File path) {
    _path = path;
  }

  public TripScheduleModificationStrategy createModificationStrategy() throws IOException {

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
          throw new IllegalStateException("expected line of form \"MATCHES => ACTION\"");
        MatchSpecification ms = parseMatchSpec(tokens[0]);
        parseReplacementSpec(tokens[1], impl, ms);
      } catch (Exception ex) {
        throw new IllegalStateException("error parsing line: number=" + lineNumber + " line=" + line, ex);
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

    for (String token : tokens) {
      Matcher m = _keyValuePattern.matcher(token);
      if (!m.matches())
        throw new IllegalStateException("invalid match specification: " + token);
      String key = m.group(1);
      String value = m.group(2);

      if (key.equals("date")) {
        try {
          ms.setDates(parseDateSpec(value));
        } catch (ParseException e) {
          throw new IllegalStateException("error parsing date: " + value);
        }
      } else if (key.equals("scheduleType")) {
        ms.setScheduleType(parseStringSpec(value));
      } else if (key.equals("exceptionCode")) {
        ms.setExceptionCode(parseStringSpec(value));
      } else if (key.equals("changeDate")) {
        ms.setChangeDate(parseIntegerSpec(value));
      } else {
        throw new IllegalStateException("unknown match key: " + key);
      }
    }

    return ms;
  }

  private void parseReplacementSpec(String spec, ModificationImpl impl, MatchSpecification match) {
    spec = spec.trim();
    if (spec.length() == 0)
      throw new IllegalStateException("no action specified");
    String[] tokens = spec.split("\\s+");
    if (tokens.length == 0)
      throw new IllegalStateException("no action specified");

    String action = tokens[0];
    Map<String, String> args = parseKeyValuePairs(tokens, 1, tokens.length);

    if (action.equals("add")) {
      try {
        Set<Date> dates = parseDateSpec(args.get("date"));
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

  private Map<String, String> parseKeyValuePairs(String[] tokens, int fromIndex, int toIndex) {

    Map<String, String> kvp = new HashMap<String, String>();

    for (int index = fromIndex; index < toIndex; index++) {
      String token = tokens[index];
      Matcher m = _keyValuePattern.matcher(token);
      if (!m.matches())
        throw new IllegalStateException("invalid match specification: " + token);
      String key = m.group(1);
      String value = m.group(2);
      if (kvp.containsKey(key))
        throw new IllegalStateException("duplicate key: " + key);
      kvp.put(key, value);
    }
    return kvp;
  }

  private Set<String> parseStringSpec(String spec) {
    Set<String> values = new HashSet<String>();
    for (String token : spec.split(","))
      values.add(token);
    return values;
  }

  private Set<Integer> parseIntegerSpec(String spec) {
    Set<Integer> values = new HashSet<Integer>();
    for (String token : spec.split(","))
      values.add(Integer.parseInt(token));
    return values;
  }

  private Set<Date> parseDateSpec(String spec) throws ParseException {

    Set<Date> dates = new HashSet<Date>();

    if (spec == null)
      return dates;

    spec = spec.trim();

    if (spec.length() == 0)
      return dates;

    for (String token : spec.split(",")) {

      if (token.length() == 0)
        continue;

      int index = token.indexOf('-');

      if (index == -1) {
        dates.add(_format.parse(token));
      } else {
        Date fromDate = _format.parse(token.substring(0, index));
        Date toDate = _format.parse(token.substring(index + 1));
        Calendar c = Calendar.getInstance();
        c.setTime(fromDate);
        while (c.getTime().compareTo(toDate) <= 0) {
          dates.add(c.getTime());
          c.add(Calendar.DAY_OF_YEAR, 1);
        }
      }
    }

    return dates;
  }

  private static class ModificationImpl implements TripScheduleModificationStrategy {

    private Map<MatchSpecification, Set<Date>> _additions = new HashMap<MatchSpecification, Set<Date>>();

    private Map<MatchSpecification, Set<Date>> _cancellations = new HashMap<MatchSpecification, Set<Date>>();

    private Map<MatchSpecification, Set<ServiceIdModificationImpl>> _modifications = new HashMap<MatchSpecification, Set<ServiceIdModificationImpl>>();

    public void addAdditions(MatchSpecification match, Set<Date> dates) {
      getValuesForMatch(_additions, match).addAll(dates);
    }

    public void addCancellations(MatchSpecification match, Set<Date> dates) {
      getValuesForMatch(_cancellations, match).addAll(dates);
    }

    public void addModifications(MatchSpecification match, ServiceIdModificationImpl modification) {
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

    public Set<ServiceIdModificationImpl> getModifications(MetroKCServiceId key, Set<Date> dates) {
      return getMatches(_modifications, key, dates);
    }

    private <T> Set<T> getValuesForMatch(Map<MatchSpecification, Set<T>> matches, MatchSpecification match) {
      Set<T> values = matches.get(match);
      if (values == null) {
        values = new HashSet<T>();
        matches.put(match, values);
      }
      return values;
    }

    private <T> Set<T> getMatches(Map<MatchSpecification, Set<T>> matches, MetroKCServiceId key, Set<Date> dates) {
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
    private Set<Integer> _changeDate;
    private Set<Date> _dates;

    public void setScheduleType(Set<String> scheduleType) {
      _scheduleType = scheduleType;
    }

    public void setExceptionCode(Set<String> exceptionCode) {
      _exceptionCode = exceptionCode;
    }

    public void setChangeDate(Set<Integer> changeDate) {
      _changeDate = changeDate;
    }

    public void setDates(Set<Date> dates) {
      _dates = dates;
    }

    public Set<Date> getDates() {
      return _dates;
    }

    public boolean hasMatch(MetroKCServiceId key, Set<Date> dates) {

      if (_changeDate != null && !_changeDate.contains(key.getChangeDate().getId()))
        return false;

      if (_scheduleType != null && !_scheduleType.contains(key.getScheduleType()))
        return false;

      if (_exceptionCode != null && !_exceptionCode.contains(key.getExceptionCode()))
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
