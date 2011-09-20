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
package org.onebusaway.phone.impl;

import java.io.IOException;

import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiOperations;
import org.onebusaway.probablecalls.TextToSpeechFactory;

public class RemoteSwiftTextToSpeechFactoryImpl implements TextToSpeechFactory {

  static final String AGI_SCRIPT = "agi-ensure-swift-stream-file.agi";

  static final String VAR_NAME = "swift_stream_file";

  private String _commandSeparator = ",";

  public void setCommandSeparator(String commandSeparator) {
    _commandSeparator = commandSeparator;
  }

  public char getAudio(AgiOperations opts, String text, String escapeDigits)
      throws IOException, AgiException {

    String options = AGI_SCRIPT + _commandSeparator
            + VAR_NAME + _commandSeparator + escape(text);
    opts.exec("AGI", options);
    String streamFile = opts.getVariable(VAR_NAME);
    return opts.streamFile(streamFile, escapeDigits);
  }
  
  private String escape(String value) {
    value = value.replaceAll("\"", "\\\\\"");
    return value.replaceAll(_commandSeparator, "\\\\"+_commandSeparator);
  }
}
