/*
 * Copyright 2008 Brian Ferris
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
package org.onebusaway.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

public class ObjectSerializationLibrary {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        for (int i = 0; i < args.length; i++)
            System.out.println(readObject(new File(args[i])));
    }

    public static ObjectOutputStream getFileAsObjectOutputStream(File file) throws IOException {
        return new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
    }

    public static ObjectInputStream getFileAsObjectInputStream(File file) throws IOException {
        return new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
    }

    public static void writeObject(File file, Object o) throws IOException {
        ObjectOutputStream oos = getFileAsObjectOutputStream(file);
        oos.writeObject(o);
        oos.close();
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObject(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = getFileAsObjectInputStream(file);
        Object o = ois.readObject();
        ois.close();
        return (T) o;
    }
    
    public static void printObject(File file, Object o) throws IOException {
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
        out.println(o);
        out.close();
    }

}
