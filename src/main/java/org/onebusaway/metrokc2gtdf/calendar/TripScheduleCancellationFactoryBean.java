package org.onebusaway.metrokc2gtdf.calendar;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TripScheduleCancellationFactoryBean extends AbstractFactoryBean {

  private static final DateFormat _format = new SimpleDateFormat("yyyy.MM.dd");

  private File _path;

  public void setPath(File path) {
    _path = path;
  }

  @Override
  public Class<?> getObjectType() {
    return TripScheduleCancellationStrategy.class;
  }

  @Override
  protected Object createInstance() throws Exception {

    DefaultTripScheduleCancellationStrategy strategy = new DefaultTripScheduleCancellationStrategy();

    System.out.println("{" + _path + "}");

    BufferedReader reader = new BufferedReader(new FileReader(_path));
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
            strategy.addCancellations(key, date);
          } else {
            Date fromDate = _format.parse(dateToken.substring(0, index));
            Date toDate = _format.parse(dateToken.substring(index + 1));
            Calendar c = Calendar.getInstance();
            c.setTime(fromDate);
            Date current = c.getTime();
            while (current.compareTo(toDate) <= 0) {
              strategy.addCancellations(key, current);
              c.add(Calendar.DAY_OF_YEAR, 1);
              current = c.getTime();
            }
          }
        } catch (ParseException ex) {
          throw new IllegalStateException("invalid date: " + dateToken, ex);
        }
      }
    }

    return strategy;
  }
}
