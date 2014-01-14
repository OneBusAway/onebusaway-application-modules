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

import java.io.IOException;
import java.io.Writer;

/**
 * A {@link Writer} extension that supports rotating between different output
 * {@link Writer} objects based on a {@link RotationStrategy}. Rotation will
 * only occur after a call to {@link #flush()}.
 * 
 * @author bdferris
 * @see RotationStrategy
 * @see TimeRotationStrategy
 */
public class RotationWriter extends Writer {

  private Writer _writer;

  private RotationStrategy _strategy;

  private int _charactersWritten;

  public RotationWriter(RotationStrategy strategy) throws IOException {
    _strategy = strategy;
    _writer = _strategy.getFirstWriter();
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    _writer.write(cbuf, off, len);
    _charactersWritten += len;
  }

  @Override
  public void flush() throws IOException {
    _writer.flush();
    Writer writer = _strategy.getNextWriter(_writer, _charactersWritten);
    if (writer == null || writer.equals(_writer))
      return;
    _writer.close();
    _writer = writer;
    _charactersWritten = 0;
  }

  @Override
  public void close() throws IOException {
    _writer.close();
  }
}
