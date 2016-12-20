/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.presentation.tags;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.struts2.components.ContextBean;
import org.onebusaway.util.SystemTime;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class DateComponent extends ContextBean {

  private static final Logger LOG = LoggerFactory.getLogger(DateComponent.class);

  private String _format;

  private String _dateStyle;

  private String _timeStyle;

  private String _value;

  private String _timeZone;

  public DateComponent(ValueStack stack) {
    super(stack);
  }

  public void setFormat(String format) {
    _format = format;
  }

  public void setDateStyle(String dateStyle) {
    _dateStyle = dateStyle;
  }

  public void setTimeStyle(String timeStyle) {
    _timeStyle = timeStyle;
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
    String dateStyle = getDateStyle();
    String timeStyle = getTimeStyle();

    if (format == null && dateStyle == null && timeStyle == null) {
      LOG.debug("no \"format\", \"dateStyle\", or \"timeStyle\" property specified for oba:date component");
      return super.end(writer, "");
    }

    if (_value == null)
      _value = "top";

    Object obj = findValue(_value);

    if (obj instanceof Long)
      obj = new Date((Long) obj);
    else if (obj instanceof String) {
      String v = (String) obj;
      if (v.equals("now"))
        obj = new Date(SystemTime.currentTimeMillis());
    }

    if (obj instanceof Date) {

      try {

        Date date = (Date) obj;
        DateFormat sdf = getFormat(format, dateStyle, timeStyle);

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

  private DateFormat getFormat(String format, String dateStyle, String timeStyle) {

    Locale locale = ActionContext.getContext().getLocale();

    if (format != null)
      return new SimpleDateFormat(format, locale);
    if (dateStyle != null && timeStyle != null)
      return DateFormat.getDateTimeInstance(getStyleAsValue(dateStyle),
          getStyleAsValue(timeStyle), locale);
    if (dateStyle != null)
      return DateFormat.getDateInstance(getStyleAsValue(dateStyle), locale);
    if (timeStyle != null)
      return DateFormat.getTimeInstance(getStyleAsValue(timeStyle), locale);
    throw new IllegalStateException(
        "no format, dateStyle, or timeStyle specified");
  }

  private int getStyleAsValue(String style) {
    style = style.toLowerCase();
    if ("medium".equals(style))
      return DateFormat.MEDIUM;
    if ("long".equals(style))
      return DateFormat.LONG;
    return DateFormat.SHORT;
  }

  private String getFormat() {
    if (_format == null)
      return null;
    return findStringIfAltSyntax(_format);
  }

  private String getDateStyle() {
    if (_dateStyle == null)
      return null;
    return findStringIfAltSyntax(_dateStyle);
  }

  private String getTimeStyle() {
    if (_timeStyle == null)
      return null;
    return findStringIfAltSyntax(_timeStyle);
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
