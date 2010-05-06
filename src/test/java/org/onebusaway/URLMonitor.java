package org.onebusaway;

import java.net.HttpURLConnection;
import java.net.URL;

public class URLMonitor {

  public static void main(String[] args) {

    if (args.length < 2) {
      System.err.println("usge: url action [args to action]");
      System.exit(-1);
    }

    long lastModified = -1;

    String[] cmd = new String[args.length - 1];
    for (int i = 0; i < cmd.length; i++)
      cmd[i] = args[i + 1];

    while (true) {

      try {
        System.out.println("trying " + args[0]);
        URL u = new URL(args[0]);
        HttpURLConnection http = (HttpURLConnection) u.openConnection();
        http.setRequestMethod("HEAD");
        long time = http.getLastModified();
        if (time > lastModified) {
          System.err.println("modification!");
          lastModified = time;
          Runtime runtime = Runtime.getRuntime();
          Process exec = runtime.exec(cmd);
          if (exec.waitFor() != 0)
            System.err.println("  error executing command");
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      try {
        Thread.sleep(60 * 1000);
      } catch (InterruptedException e) {
        System.exit(-1);
      }
    }
  }

}
