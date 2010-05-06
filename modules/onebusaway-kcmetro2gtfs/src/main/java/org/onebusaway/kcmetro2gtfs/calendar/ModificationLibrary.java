package org.onebusaway.kcmetro2gtfs.calendar;

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

public class ModificationLibrary {

  private static Pattern _keyValuePattern = Pattern.compile("^(.*)=(.*)$");

  private static final DateFormat _format = new SimpleDateFormat("yyyy.MM.dd");

  public static Map<String, String> parseKeyValuePairs(String[] tokens,
      int fromIndex, int toIndex) {

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

  public static Set<String> parseStringSpec(String spec) {
    Set<String> values = new HashSet<String>();
    for (String token : spec.split(","))
      values.add(token);
    return values;
  }

  public static Set<Date> parseDateSpec(String spec) throws ParseException {

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

}
