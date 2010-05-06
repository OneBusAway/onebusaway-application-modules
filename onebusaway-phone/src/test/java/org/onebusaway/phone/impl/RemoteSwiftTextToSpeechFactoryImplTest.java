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
