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
/**
 * 
 */
package org.onebusaway.presentation.impl.text;

import org.onebusaway.presentation.services.text.TextModification;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ReplacementTextModification implements TextModification {

  private String _from;

  private String _to;

  private boolean _atWordBoundary = true;

  private Pattern _pattern;

  private boolean _caseSensistive = false;

  public void setFrom(String from) {
    _from = from;
    buildPattern();
  }

  public void setTo(String to) {
    _to = to;
  }
  
  public void setAtWordBoundary(boolean atWordBoundary) {
    _atWordBoundary = atWordBoundary;
    buildPattern();
  }
  
  public void setCaseSensitive(boolean caseSensitive) {
    _caseSensistive = caseSensitive;
    buildPattern();
  }

  public String modify(String input) {
    Matcher matcher = _pattern.matcher(input);
    return matcher.replaceAll(_to);
  }

  private void buildPattern() {
    
    if( _from == null)
      return;
    
    try {
      String from = _from;
      if (_atWordBoundary)
        from = "\\b" + from + "\\b";
      
      int flags = _caseSensistive ? 0 : Pattern.CASE_INSENSITIVE;
      
      _pattern = Pattern.compile(from,flags);
      
    } catch (PatternSyntaxException ex) {
      throw new IllegalArgumentException(
          "invalid replacement regular expression: " + _from, ex);
    }
  }
}