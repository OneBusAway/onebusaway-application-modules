package org.onebusaway.kcmetro_tcip;

import static com.sun.akuma.CLibrary.LIBC;

import com.sun.akuma.Daemon;
import com.sun.akuma.JavaVMArguments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

public class Daemonizer {

  private File _outputFile;

  private File _errorFile;

  private File _pidFile;

  private File _workingDirectory;

  private Collection<String> _jvmArgs;

  public void setOutputFile(File outputFile) {
    _outputFile = outputFile;
  }

  public void setErrorFile(File errorFile) {
    _errorFile = errorFile;
  }

  public void setPidFile(File pidFile) {
    _pidFile = pidFile;
  }

  public void setWorkingDirectory(File workingDirectory) {
    _workingDirectory = workingDirectory;
  }

  public void setJvmArgs(Collection<String> jvmArgs) {
    _jvmArgs = jvmArgs;
  }

  public void daemonize() throws Exception {

    Daemon d = new DaemonImpl();

    if (d.isDaemonized()) {
      d.init();
    } else {

      JavaVMArguments arguments = JavaVMArguments.current();

      if (_jvmArgs != null)
        arguments.addAll(1, _jvmArgs);

      // This effectively performs a fork...
      d.daemonize(arguments);
      System.exit(0);
    }
  }

  /****
   * Private Methods
   ****/

  private boolean isRedirectOutputToError() {
    if (_outputFile == null || _errorFile == null)
      return false;
    return _outputFile.getName().equals("&2");
  }

  private boolean isRedirectErrorToOutput() {
    if (_outputFile == null || _errorFile == null)
      return false;
    return _errorFile.getName().equals("&1");
  }

  private class DaemonImpl extends Daemon {

    @Override
    protected void chdirToRoot() {
      super.chdirToRoot();
      if (_workingDirectory != null) {
        LIBC.chdir(_workingDirectory.getAbsolutePath());
        System.setProperty("user.dir", _workingDirectory.getAbsolutePath());
      }
    }

    @Override
    protected void closeDescriptors() throws IOException {

      boolean redirectOutputToError = isRedirectOutputToError();
      boolean redirectErrorToOutput = isRedirectErrorToOutput();

      if (redirectOutputToError && redirectErrorToOutput) {
        throw new IllegalStateException(
            "circular redirection amongst output and error");
      }

      super.closeDescriptors();

      if (redirectErrorToOutput) {
        PrintStream stream = new PrintStream(_outputFile);
        System.setOut(stream);
        System.setErr(stream);
      } else if (redirectOutputToError) {
        PrintStream stream = new PrintStream(_errorFile);
        System.setOut(stream);
        System.setErr(stream);
      } else {
        if (_outputFile != null)
          System.setOut(new PrintStream(_outputFile));
        if (_errorFile != null)
          System.setErr(new PrintStream(_errorFile));
      }
    }

    @Override
    protected void writePidFile() throws IOException {

      if (_pidFile == null)
        return;

      try {
        FileWriter fw = new FileWriter(_pidFile);
        fw.write(String.valueOf(LIBC.getpid()));
        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
