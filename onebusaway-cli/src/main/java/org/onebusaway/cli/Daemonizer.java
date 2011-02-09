package org.onebusaway.cli;

import static com.sun.akuma.CLibrary.LIBC;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.sun.akuma.Daemon;
import com.sun.akuma.JavaVMArguments;
import com.sun.jna.StringArray;

/**
 * Supports portable daemonization of a Java process. Uses the Sakuma wrapper of
 * LIBC to perform daemonization, so will only work on systems that have LIBC.
 * Works with the {@code commons-cli} command-line-interface argument parsing to
 * define and process command line arguments defining daemonization parameters
 * like a pid file, jvm args, and process output log files.
 * 
 * See {@link #buildOptions(Options)} and
 * {@link #handleDaemonization(CommandLine)} for convenience methods to perform
 * daemonization.
 * 
 * @author bdferris
 */
public class Daemonizer {

  private static final String ARG_DAEMONIZE = "daemonize";

  private static final String ARG_PID_FILE = "pidFile";

  private static final String ARG_JVM_ARGS = "jvmArgs";

  private static final String ARG_ERROR_FILE = "errorFile";

  private static final String ARG_OUTPUT_FILE = "outputFile";

  private static final String ARG_EXE = "exe";

  private static MyLog _log = new MyLog();

  private File _outputFile;

  private File _errorFile;

  private File _pidFile;

  private File _workingDirectory;

  private String _exe;

  private Collection<String> _jvmArgs;

  /**
   * Add common daemonization command line arguments to a {@code commons-cli}
   * {@link Options} collection.
   * 
   * @param options we add command line options to this target options
   *          collection
   * @return the same target options collection
   */
  public static Options buildOptions(Options options) {
    options.addOption(ARG_JVM_ARGS, true, "custom jvm args for the daemon");
    options.addOption(ARG_OUTPUT_FILE, true,
        "stdout output (&2 to redirect to stderr)");
    options.addOption(ARG_ERROR_FILE, true,
        "stderr output (&1 to redirect to stdout)");
    options.addOption(ARG_PID_FILE, true, "pid file");
    options.addOption(ARG_EXE, true, "specify exe path");
    options.addOption(ARG_DAEMONIZE, false, "run as a daemon");

    return options;
  }

  /**
   * Convenience method to handle daemonization of a command line Java program
   * 
   * @param cli parsed command line option values
   * @return true if daemonization was performed
   */
  public static boolean handleDaemonization(CommandLine cli) throws Exception {

    if (cli.hasOption(ARG_DAEMONIZE)) {

      _log.debug("-daemonize option specified");

      Daemonizer daemonizer = new Daemonizer();

      if (cli.hasOption(ARG_OUTPUT_FILE))
        daemonizer.setOutputFile(new File(cli.getOptionValue(ARG_OUTPUT_FILE)));

      if (cli.hasOption(ARG_ERROR_FILE))
        daemonizer.setErrorFile(new File(cli.getOptionValue(ARG_ERROR_FILE)));

      if (cli.hasOption(ARG_PID_FILE))
        daemonizer.setPidFile(new File(cli.getOptionValue(ARG_PID_FILE)));

      if (cli.hasOption(ARG_JVM_ARGS)) {
        String[] jvmArgs = cli.getOptionValue(ARG_JVM_ARGS).split(" ");
        daemonizer.setJvmArgs(Arrays.asList(jvmArgs));
      }

      if (cli.hasOption(ARG_EXE)) {
        daemonizer.setExe(cli.getOptionValue(ARG_EXE));
      }

      daemonizer.daemonize();
      return true;
    }

    return false;
  }

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

  private void setExe(String exe) {
    _exe = exe;
  }

  public void daemonize() throws Exception {

    DaemonImpl d = new DaemonImpl();

    if (d.isDaemonized()) {
      if (_log.isDebugEnabled()) {
        _log.debug("process is already daemonized");
        _log.debug("currentExecutabe=" + Daemon.getCurrentExecutable());
        Properties props = System.getProperties();
        for (Object key : props.keySet()) {
          Object value = props.get(key);
          _log.debug(key + "," + value);
        }
      }

      _log.debug("pre complete");
      LIBC.umask(0027);
      _log.debug("umask complete");

      if (_pidFile == null)
        d.init(null);
      else
        d.init(_pidFile.getAbsolutePath());

      _log.debug("init complete");

    } else {

      _log.debug("forking daemon process");

      JavaVMArguments arguments = JavaVMArguments.current();

      if (_jvmArgs != null)
        arguments.addAll(1, _jvmArgs);

      if (_log.isDebugEnabled()) {
        _log.debug("jvm args:");
        for (int i = 0; i < arguments.size(); i++)
          _log.debug(arguments.get(i));

        _log.debug("currentExecutabe=" + Daemon.getCurrentExecutable());
      }

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

    /**
     * Relaunches the JVM with the given arguments into the daemon.
     */
    public void daemonize(JavaVMArguments args) {
      if (isDaemonized())
        throw new IllegalStateException("Already running as a daemon");

      // let the child process now that it's a daemon
      args.setSystemProperty(Daemon.class.getName(), "daemonized");

      // prepare for a fork
      String exe = getCurrentExecutable();
      if (_exe != null)
        exe = _exe;

      StringArray sa = new StringArray(args.toArray(new String[args.size()]));

      int i = LIBC.fork();
      if (i < 0) {
        LIBC.perror("initial fork failed");
        System.exit(-1);
      }
      if (i == 0) {
        // with fork, we lose all the other critical threads, to exec to Java
        // again
        LIBC.execv(exe, sa);
        System.err.println("exec failed");
        LIBC.perror("initial exec failed");
        System.exit(-1);
      }

      // parent exits
    }
  }

  private static class MyLog {

    public boolean isDebugEnabled() {
      return true;
    }

    public void debug(String string) {
      try {
        PrintWriter out = new PrintWriter(new FileWriter("/tmp/log.out", true));
        out.println(string);
        out.flush();
        out.close();
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }

}
