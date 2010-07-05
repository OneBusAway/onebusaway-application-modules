package org.onebusaway.users.impl.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.users.services.logging.UserInteractionLoggingOutlet;

public class UserInterfactionLoggingOutletImpl implements
    UserInteractionLoggingOutlet {

  private String _path;

  private Process _process;

  private PrintWriter _out;

  public void setPath(String path) {
    _path = path;
  }

  @PostConstruct
  public void setup() throws IOException {
    if (_path.startsWith("|")) {
      String path = _path.substring(1);
      String[] args = path.split("\\s+");
      Runtime runtime = Runtime.getRuntime();
      _process = runtime.exec(args);
      _out = new PrintWriter(new OutputStreamWriter(_process.getOutputStream()),true);
    } else {
      _out = new PrintWriter(new FileWriter(_path,true),true);
    }
  }

  @PreDestroy
  public void tearDown() throws InterruptedException {
    if (_out != null)
      _out.close();
    if( _process != null)
      _process.waitFor();
  }

  @Override
  public void logInteraction(String serialized) {
    _out.println(serialized);
  }
}