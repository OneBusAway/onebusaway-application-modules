package org.onebusaway.presentation.tags;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.struts2.components.ContextBean;

import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class DateComponent extends ContextBean {

  private static final Logger LOG = LoggerFactory.getLogger(DateComponent.class);

  private String _format;

  private String _value;

  private String _timeZone;

  public DateComponent(ValueStack stack) {
    super(stack);
  }

  public void setFormat(String format) {
    _format = format;
  }

  public void setValue(String value) {
    _value = value;
  }

  public void setTimeZone(String timeZone) {
    _timeZone = timeZone;
  }

  @Override
  public boolean usesBody() {
    return false;
  }

  @Override
  public boolean end(Writer writer, String body) {

    String format = getFormat();
    
    if( format == null) {
      LOG.debug("no \"format\" property specified for oba:date component");
      return super.end(writer, "");
    }
    
    if (_value == null)
      _value = "top";

    Object obj = findValue(_value);
    
    if( obj instanceof Long) 
      obj = new Date((Long) obj);
    else if( obj instanceof String) {
      String v = (String) obj;
      if( v.equals("now"))
        obj = new Date();
    }
    
    if (obj instanceof Date) {

      try {

        Date date = (Date) obj;
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        TimeZone timeZone = getTimeZone();
        sdf.setTimeZone(timeZone);

        String name = sdf.format(date);
        writer.write(name);

      } catch (Exception e) {
        LOG.error("Could not write out Text tag", e);
      }
    }

    return super.end(writer, "");
  }

  private String getFormat() {
    if( _format == null)
      return null;
    return findStringIfAltSyntax(_format);
  }

  private TimeZone getTimeZone() {

    TimeZone tz = null;

    if (_timeZone != null)
      tz = getTimeZoneForExpression(_timeZone);

    if (tz == null)
      tz = getTimeZoneForExpression("timeZone");

    if (tz == null)
      tz = TimeZone.getDefault();

    return tz;
  }

  private TimeZone getTimeZoneForExpression(String expression) {

    Object timeZone = findValue(expression);

    if (timeZone instanceof TimeZone) {
      return (TimeZone) timeZone;
    } else if (timeZone instanceof String) {
      TimeZone tz = TimeZone.getTimeZone((String) timeZone);
      if (tz != null)
        return tz;
    }

    return null;
  }
}
