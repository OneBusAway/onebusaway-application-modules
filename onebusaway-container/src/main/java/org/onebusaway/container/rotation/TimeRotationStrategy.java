package org.onebusaway.container.rotation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An implementation of {@link RotationStrategy} that writes output to file
 * whose named is specified using a format processed using the the
 * {@link SimpleDateFormat} class. This allows one to get standard time-based
 * file rotation with whatever time granularity and formatting you may desire.
 * 
 * @author bdferris
 */
public class TimeRotationStrategy implements RotationStrategy {

  private DateFormat _timeFormat;

  private String _lastFormat;

  public TimeRotationStrategy(String format) {
    _timeFormat = new SimpleDateFormat(format);
  }

  public Writer getFirstWriter() throws IOException {
    _lastFormat = _timeFormat.format(new Date());
    File path = new File(_lastFormat);
    File parent = path.getParentFile();
    if (parent != null && !parent.exists())
      parent.mkdirs();
    return new FileWriter(new File(_lastFormat), true);
  }

  public Writer getNextWriter(Writer writer, int charactersWritten)
      throws IOException {
    String format = _timeFormat.format(new Date());
    if (_lastFormat.equals(format))
      return writer;
    _lastFormat = format;
    return new FileWriter(format, true);
  }
}
