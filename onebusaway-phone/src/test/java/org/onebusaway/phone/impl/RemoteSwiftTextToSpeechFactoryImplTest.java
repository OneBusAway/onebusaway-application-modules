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
import org.junit.Test;
import org.mockito.Mockito;


public class RemoteSwiftTextToSpeechFactoryImplTest {
  
  @Test
  public void test() throws IOException, AgiException {
    
      RemoteSwiftTextToSpeechFactoryImpl factory = new RemoteSwiftTextToSpeechFactoryImpl();
      AgiOperations opts = Mockito.mock(AgiOperations.class);
      
      Mockito.when(opts.getVariable("swift_stream_file")).thenReturn("path/to/file");
      
      factory.getAudio(opts, "Hello A, Hello B", "0123456789*#");
      
      Mockito.verify(opts).exec("AGI","agi-ensure-swift-stream-file.agi,swift_stream_file,Hello A\\, Hello B");
      Mockito.verify(opts).getVariable("swift_stream_file");
      Mockito.verify(opts).streamFile("path/to/file", "0123456789*#");
  }
}
