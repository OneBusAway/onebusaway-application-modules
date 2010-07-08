package org.onebusaway.users.impl.logging;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.users.services.logging.UserInteractionLoggingOutlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserInteractionLoggingOutletImpl implements
    UserInteractionLoggingOutlet {

  private static Logger _log = LoggerFactory.getLogger(UserInteractionLoggingOutletImpl.class);

  private static ExecutorService _executorService = Executors.newFixedThreadPool(2);

  private String _path;

  private Process _process;

  private PrintWriter _out;

  private boolean _logProcessOutput = true;

  public void setPath(String path) {
    _path = path;
  }

  public void setLogProcessOutput(boolean logProcessOutput) {
    _logProcessOutput = logProcessOutput;
  }

  @PostConstruct
  public void setup() throws IOException {
    if (_path.startsWith("|")) {
      String path = _path.substring(1);
      String[] args = path.split("\\s+");
      if (_log.isDebugEnabled())
        _log.debug("opening process for logging: " + Arrays.toString(args));
      Runtime runtime = Runtime.getRuntime();
      _process = runtime.exec(args);
      _out = new PrintWriter(
          new OutputStreamWriter(_process.getOutputStream()), true);

      if (_logProcessOutput) {
        _executorService.execute(new OutputLogger("stdout", new BufferedReader(
            new InputStreamReader(_process.getInputStream()))));
        _executorService.execute(new OutputLogger("stderr", new BufferedReader(
            new InputStreamReader(_process.getErrorStream()))));
      }
    } else {
      if (_log.isDebugEnabled())
        _log.debug("opening file for logging: " + _path);
      _out = new PrintWriter(new FileWriter(_path, true), true);
    }
  }

  @PreDestroy
  public void tearDown() throws InterruptedException {
    if (_out != null)
      _out.close();
    if (_process != null) {

      try {
        _process.getOutputStream().close();
      } catch (IOException ex) {
        _log.warn("error closing process output stream", ex);
      }

      int exitValue = _process.waitFor();
      if (exitValue != 0)
        _log.warn("logging process did not return normally: exitValue="
            + exitValue);
    }
    if (_executorService != null)
      _executorService.shutdownNow();
  }

  @Override
  public void logInteraction(String serialized) {
    if (_log.isDebugEnabled())
      _log.debug("logging interaction: " + serialized);
    _out.println(serialized);
  }

  private static class OutputLogger implements Runnable {

    private BufferedReader _reader;
    private String _prefix;

    public OutputLogger(String prefix, BufferedReader reader) {
      _prefix = prefix;
      _reader = reader;
    }

    @Override
    public void run() {
      try {
        String line = null;
        while ((line = _reader.readLine()) != null) {
          _log.info("output: prefix=" + _prefix + " output=" + line);
        }
      } catch (IOException ex) {
        _log.warn("error reading output: prefix=" + _prefix, ex);
      } finally {
        try {
          _reader.close();
        } catch (IOException e) {
        }
      }

      _log.debug("exiting output logger: prefix=" + _prefix);
    }

  }
}