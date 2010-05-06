package org.onebusaway.webapp.gwt.common.control;

public class StringEditDistance {

  /**
   * See http://en.wikipedia.org/wiki/Levenshtein_distance
   * 
   * @param s
   * @param t
   * @return
   */
  public static int getEditDistance(String s, String t) {

    int m = s.length();
    int n = t.length();
    int[][] d = new int[m + 1][n + 1];

    for (int i = 0; i <= m; i++)
      d[i][0] = i;
    for (int j = 0; j <= n; j++)
      d[0][j] = j;

    for (int i = 1; i <= m; i++) {
      for (int j = 1; j <= n; j++) {
        int cost = s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1;
        d[i][j] = Math.min(d[i - 1][j] + 1, Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + cost));
      }
    }

    return d[m][n];
  }
}
