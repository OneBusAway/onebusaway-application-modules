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
