/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.common.web.gwt.resources;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Aggregates and minifies CSS stylesheets. A CssResource represents a regular
 * CSS file with GWT-specific at-rules.
 * 
 * Currently-supported rules:
 * 
 * <ul>
 * <li>{@code @def NAME literal-value; .myClass {background: NAME;}} Define a
 * static constant.
 * <li>{@code @eval NAME Java-expression; .myClass {background: NAME;}} Define
 * a constant based on a Java expression.
 * <li>{@code @if [!]property (list of values) {ruleBlock}} Include or exclude
 * CSS rules based on the value of a deferred-binding property.
 * <li>{@code @if Java-expression {ruleBlock}} Include or exclude CSS rules
 * based on a boolean Java expression.
 * <li>{@code @sprite className siblingImageResource;} Return a {@link Sprite}
 * to access the style.
 * <li>{@code @url NAME siblingDataResource; .myClass {background: NAME repeat-x;}}
 * Use a DataResource to generate a <code>url('...'}</code> value.
 * </ul>
 */
public interface CssResource extends ResourcePrototype {
  /**
   * The original CSS class name specified in the resource.
   */
  @Documented
  @Target(ElementType.METHOD)
  @interface ClassName {
    String value();
  }

  /**
   * Override the prefix used for obfuscated CSS class names within a bundle
   * type. This annotation must be applied to the enclosing
   * ImmutableResourceBundle because the bundle itself defines the scope in
   * which the obfuscation of CSS class identifiers is applied.
   * <p>
   * The default algorithm is designed to be safe, but will not produce the
   * shortest possible CSS class identifiers. The developer should choose a
   * prefix that is known to not conflict with external CSS class names. An
   * application written by FooBar Inc. might choose to use a prefix
   * <code>FB</code>.
   */
  @Target(ElementType.TYPE)
  @interface ClassPrefix {
    String value();
  }

  String getText();
}
