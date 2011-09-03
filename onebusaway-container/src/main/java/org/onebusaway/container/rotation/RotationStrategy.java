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
