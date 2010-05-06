package org.onebusaway.metrokc2gtdf.calendar;

import edu.washington.cs.rse.collections.FactoryMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultTripScheduleCancellationStrategy implements
    TripScheduleCancellationStrategy {

  private static final DateFormat _format = new SimpleDateFormat("yyyy.MM.dd");

  private Map<String, Set<Date>> _exceptions = new FactoryMap<String, Set<Date>>(
      new HashSet<Date>());

  public boolean isCancellation(String exceptionCode, Date date) {

    if (!_exceptions.containsKey(exceptionCode))
      return false;
    Set<Date> dates = _exceptions.get(exceptionCode);
    return dates.contains(date);
  }

  public void addCancellations(String exceptionCode, Date date) {
    _exceptions.get(exceptionCode).add(date);
  }

  public void setCancellationsFromFile(File file) throws IOException {

    System.out.println(file);
    
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;

    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("=");
      if (tokens.length != 2)
        throw new IllegalStateException("Invalid line: " + line);
      String key = tokens[0];
      String dates = tokens[1];
      String[] dateTokens = dates.split("\\s+");
      for (String dateToken : dateTokens) {
        try {
          int index = dateToken.indexOf('-');
          if (index == -1) {
            Date date = _format.parse(dateToken);
            addCancellations(key, date);
          } else {
            Date fromDate = _format.parse(dateToken.substring(0, index));
            Date toDate = _format.parse(dateToken.substring(index + 1));
            Calendar c = Calendar.getInstance();
            c.setTime(fromDate);
            Date current = c.getTime();
            while (current.compareTo(toDate) <= 0) {
              addCancellations(key, current);
              c.add(Calendar.DAY_OF_YEAR, 1);
              current = c.getTime();
            }
          }
        } catch (ParseException ex) {
          throw new IllegalStateException("invalid date: " + dateToken, ex);
        }
      }
    }
  }
}
