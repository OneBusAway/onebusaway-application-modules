package org.onebusaway.users.impl.authentication;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public class UtilityLibrary {

  public static void writeFormPostRedirect(HttpServletResponse httpResponse,
      String formAction, Map<?, ?> parameters) throws IOException {

    httpResponse.setContentType("text/html");
    httpResponse.setStatus(HttpServletResponse.SC_OK);

    PrintWriter writer = httpResponse.getWriter();

    writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
    writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
    writer.println("<head><title>OpenID HTML FORM Redirection</title></head>");
    writer.println("<body>");
    writer.println("<form name=\"openid-form-redirection\" action=\""
        + formAction + "\" method=\"post\" accept-charset=\"utf-8\">");
    
    for (Map.Entry<?, ?> entry : parameters.entrySet())
      writer.println("<input type=\"hidden\" name=\"" + entry.getKey()
          + "\" value=\"" + entry.getValue() + "\"/>");

    writer.println("<button type=\"submit\">Continue...</button>");
    writer.println("</form>");
    writer.println("<script type=\"text/javascript\">");
    writer.println("  window.onload = function() { document.forms['openid-form-redirection'].submit(); }");
    writer.println("</script>");
    writer.println("</body>");
    writer.println("</html>");
  }
}
