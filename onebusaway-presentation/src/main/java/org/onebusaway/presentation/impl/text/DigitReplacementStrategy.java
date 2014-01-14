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

public class DigitReplacementStrategy implements TextModification {

    private Pattern p = Pattern.compile("\\b\\d+\\b");

    public String modify(String input) {

        Matcher m = p.matcher(input);
        StringBuffer b = new StringBuffer();
        while (m.find())
            m.appendReplacement(b, getDigitAsString(m.group()));
        m.appendTail(b);
        return b.toString();
    }

    private String getDigitAsString(String input) {

        int value = Integer.parseInt(input);

        if (value >= 1000 || value <= 0)
            throw new IllegalStateException("Can't handle number: " + input);

        if (value % 100 == 0)
            return getDigitAsString(input.charAt(0)) + " hundreth";

        StringBuilder b = new StringBuilder();

        if (input.length() == 3) {
            b.append(getDigitAsString(input.charAt(0)));
            b.append(" hundred and ");
            input = input.substring(1);
        }

        value -= (value / 100) * 100;

        if (input.length() == 2) {
            switch (input.charAt(0)) {
            case '0':
                input = input.substring(1);
                break;
            case '1':
                b.append(getTeensAsOrdinal(input.charAt(1)));
                input = input.substring(2);
                break;
            default:
                b.append(getDigitAsTensString(input.charAt(0))).append(" ");
                input = input.substring(1);
                break;
            }

        }

        if (input.length() == 1) {
            b.append(getDigitAsOrdinal(input.charAt(0)));
        }

        return b.toString();

    }

    private String getDigitAsString(char digit) {
        switch (digit) {
        case '0':
            return "zero";
        case '1':
            return "one";
        case '2':
            return "two";
        case '3':
            return "three";
        case '4':
            return "four";
        case '5':
            return "five";
        case '6':
            return "six";
        case '7':
            return "seven";
        case '8':
            return "eight";
        case '9':
            return "nine";
        default:
            throw new IllegalStateException("uknown digit: " + digit);
        }
    }

    private String getDigitAsTensString(char digit) {
        switch (digit) {
        case '2':
            return "twenty";
        case '3':
            return "thirty";
        case '4':
            return "fourty";
        case '5':
            return "fifty";
        case '6':
            return "sixty";
        case '7':
            return "seventy";
        case '8':
            return "eighty";
        case '9':
            return "ninety";
        default:
            throw new IllegalStateException("uknown digit: " + digit);
        }
    }

    private String getDigitAsOrdinal(char digit) {
        switch (digit) {
        case '0':
            return "zeroth";
        case '1':
            return "first";
        case '2':
            return "second";
        case '3':
            return "third";
        case '4':
            return "fourth";
        case '5':
            return "fifth";
        case '6':
            return "sixth";
        case '7':
            return "seventh";
        case '8':
            return "eighth";
        case '9':
            return "ninth";
        default:
            throw new IllegalStateException("uknown digit: " + digit);
        }
    }

    private String getTeensAsOrdinal(char digit) {
        switch (digit) {
        case '0':
            return "tenth";
        case '1':
            return "eleventh";
        case '2':
            return "twelfth";
        case '3':
            return "thriteenth";
        case '4':
            return "fourteenth";
        case '5':
            return "fifteenth";
        case '6':
            return "sixteenth";
        case '7':
            return "seventeenth";
        case '8':
            return "eighteenth";
        case '9':
            return "ninteenth";
        default:
            throw new IllegalStateException("uknown digit: " + digit);
        }
    }
}