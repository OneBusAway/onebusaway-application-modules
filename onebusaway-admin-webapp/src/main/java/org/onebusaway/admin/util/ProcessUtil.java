/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility to collect console output and return code or executed runtime processes. 
 *
 */
public class ProcessUtil {

  private String cmd =  null;
  private ProcessStream output;
  private ProcessStream error;
  private Exception exception = null;
  private Integer returnCode = null;
  public ProcessUtil(String commandWithArguments) {
    cmd = commandWithArguments;
  }
  
  public void exec() {
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      output = new ProcessStream(process.getInputStream());
      error = new ProcessStream(process.getErrorStream());
      output.start();
      error.start();
      returnCode = process.waitFor();
    } catch (Exception any) {
      exception = any;
    }
  }
  
  public Integer getReturnCode() {
    return returnCode;
  }
  
    public String getOutput() {
    if (output != null)
      return output.toString();
    return null;
  }

   public String getError() {
    if (error != null)
      return error.toString();
    return null;
  }
   
   public Exception getException() {
     return exception;
   }


  class ProcessStream extends Thread {
    private InputStream is;
    private ByteArrayOutputStream capture = new ByteArrayOutputStream();
    public ProcessStream(InputStream is) {
      this.is = is;
    }
    
    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ( (line= br.readLine()) != null) {
          capture.write(line.getBytes());
        }
      } catch (Exception any) {
        // bury
      }
    }
    
    public String toString() {
      return new String(capture.toByteArray());
    }
  }
  
}
