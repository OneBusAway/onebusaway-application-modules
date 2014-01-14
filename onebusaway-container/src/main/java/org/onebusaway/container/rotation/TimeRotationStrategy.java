/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
