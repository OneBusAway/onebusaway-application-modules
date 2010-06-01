package org.onebusaway.container.rotation;

import java.io.IOException;
import java.io.Writer;

/**
 * Defines a {@link Writer} rotation strategy that can be used in cooperation
 * with {@link RotationWriter} to have one virtual {@link Writer} that seamless
 * rotates output to multiple output writers.
 * 
 * @author bdferris
 * @see RotationWriter
 * @see TimeRotationStrategy
 */
public interface RotationStrategy {

  /**
   * @return the Writer that will be used for initial output
   * @throws IOException
   */
  public Writer getFirstWriter() throws IOException;

  /**
   * Called to get the next output writer when a call is made to
   * {@link RotationWriter#flush()}. The current writer is provided as a
   * parameter, so if no rotation should occur, just return the current writer.
   * 
   * @param writer the current output writer
   * @param charactersWritten the number of characters written to the current
   *          writer
   * @return the writer to output to in the future
   * @throws IOException
   */
  public Writer getNextWriter(Writer writer, int charactersWritten)
      throws IOException;
}
