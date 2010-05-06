package org.onebusaway.metrokc2gtdf.calendar;

import org.onebusaway.metrokc2gtdf.model.MetroKCChangeDate;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TripScheduleReplacementFactoryBean extends AbstractFactoryBean {

  private static Pattern _keyValuePattern = Pattern.compile("^(.*)=(.*)$");

  private static final DateFormat _format = new SimpleDateFormat("yyyy.MM.dd");

  private File _path;

  public void setPath(File path) {
    _path = path;
  }

  @Override
  public Class<?> getObjectType() {
    return ReplacementImpl.class;
  }

  @Override
  protected Object createInstance() throws Exception {

    BufferedReader reader = new BufferedReader(new FileReader(_path));
    String line = null;
    int lineNumber = 1;

    ReplacementImpl impl = new ReplacementImpl();

    while ((line = reader.readLine()) != null) {
      try {
        String[] tokens = line.split("\\s+=>\\s+");
        if (tokens.length != 2)
          throw new IllegalStateException(
              "expected line of form \"MATCHES => REPLACEMENT\"");
        MatchSpecification ms = parseMatchSpec(tokens[0]);
        ReplacementSpecification rs = parseReplacementSpec(tokens[1]);
        impl.addReplacement(ms, rs);
      } catch (Exception ex) {
        throw new IllegalStateException("error parsing line: number="
            + lineNumber + " line=" + line);
      }
      lineNumber++;
    }

    return impl;
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
          ms.date = _format.parse(value);
        } catch (ParseException e) {
          throw new IllegalStateException("error parsing date: " + value);
        }
      } else if (key.equals("scheduleType")) {
        ms.scheduleType = value;
      } else if (key.equals("exceptionCode")) {
        ms.exceptionCode = value;
      }
    }

    return ms;
  }

  private ReplacementSpecification parseReplacementSpec(String spec) {
    spec = spec.trim();
    String[] tokens = spec.split("\\s+");
    if (tokens.length == 0)
      throw new IllegalStateException("no replacements specified");

    ReplacementSpecification rs = new ReplacementSpecification();

    for (String token : tokens) {
      Matcher m = _keyValuePattern.matcher(token);
      if (!m.matches())
        throw new IllegalStateException("invalid match specification: " + token);
      String key = m.group(1);
      String value = m.group(2);

      if (key.equals("scheduleType")) {
        rs.scheduleType = value;
      }
    }

    return rs;
  }

  private static class ReplacementImpl implements
      TripScheduleReplacementStrategy {

    private Map<MatchSpecification, ReplacementSpecification> _replacements = new LinkedHashMap<MatchSpecification, ReplacementSpecification>();

    public void addReplacement(MatchSpecification ms,
        ReplacementSpecification rs) {
      _replacements.put(ms, rs);
    }

    public boolean hasReplacement(CalendarKey key, Date date) {
      return findReplacement(key, date) != null;
    }

    public CalendarKey getReplacement(CalendarKey key, Date date) {

      ReplacementSpecification rs = findReplacement(key, date);

      if (rs == null)
        return key;

      MetroKCChangeDate cd = key.getChangeDate();
      String ec = key.getExceptionCode();
      String scheduleType = key.getScheduleType();

      if (rs.scheduleType != null)
        scheduleType = rs.scheduleType;

      return new CalendarKey(cd, scheduleType, ec);
    }

    private ReplacementSpecification findReplacement(CalendarKey key, Date date) {
      for (Map.Entry<MatchSpecification, ReplacementSpecification> entry : _replacements.entrySet()) {
        
        MatchSpecification ms = entry.getKey();
        
        if (ms.date != null && !ms.date.equals(date))
          continue;
        if (ms.scheduleType != null
            && !ms.scheduleType.equals(key.getScheduleType()))
          continue;
        if (ms.exceptionCode != null
            && !ms.exceptionCode.equals(key.getExceptionCode()))
          continue;

        return entry.getValue();
      }
      return null;
    }

  }

  private static class MatchSpecification {
    public Date date;
    public String scheduleType;
    public String exceptionCode;
  }

  private static class ReplacementSpecification {
    public String scheduleType;
  }
}
