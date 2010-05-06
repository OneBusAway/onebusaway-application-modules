package org.onebusaway.webapp.actions.sms;

import java.util.ArrayList;
import java.util.List;

public class Commands {

  private static List<String> _commands = new ArrayList<String>();

  public static final String HELP = "help";

  public static final String SET_SEARCH_LOCATION = "setSearchLocation";

  public static final String BOOKMARKS = "bookmarks";
  
  public static final String RESET = "reset";

  static {
    addCommand(HELP);
    addCommand(SET_SEARCH_LOCATION);
    addCommand(BOOKMARKS);
    addCommand(RESET);
  }

  private static void addCommand(String command) {
    _commands.add(command);
  }

  public static String getClosestCommand(String command) {

    List<String> matches = new ArrayList<String>(_commands);

    String prefix = "";

    for (int i = 0; i < command.length(); i++) {

      List<String> m = new ArrayList<String>();

      prefix += command.charAt(i);

      for (String c : matches) {
        if (c.startsWith(prefix))
          m.add(c);
      }

      matches = m;

      if (matches.size() == 1)
        return matches.get(0);

      if (matches.isEmpty())
        return null;
    }

    return null;
  }

  public static String getCanonicalCommand(String command) {
    command = command.toLowerCase();
    for (String c : _commands) {
      if (command.equals(c.toLowerCase()))
        return c;
    }
    return null;
  }
}
