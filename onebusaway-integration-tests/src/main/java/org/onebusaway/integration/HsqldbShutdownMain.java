package org.onebusaway.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class HsqldbShutdownMain {
  public static void main(String[] args) throws ClassNotFoundException,
      SQLException {
    if (args.length != 1) {
      System.err.println("usage: jdbc_connection_string");
      System.exit(-1);
    }
    Class.forName("org.hsqldb.jdbc.JDBCDriver");
    Connection c = DriverManager.getConnection(args[0], "SA", "");
    Statement statement = c.createStatement();
    statement.execute("SHUTDOWN");
    statement.close();
    c.close();
  }
}
