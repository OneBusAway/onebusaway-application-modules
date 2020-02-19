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
package org.onebusaway.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Convenience methods for serializing objects to and from streams and files.
 * 
 * @author bdferris
 */
public class ObjectSerializationLibrary {

  public static void main(String[] args) throws IOException,
      ClassNotFoundException {
    for (int i = 0; i < args.length; i++)
      System.out.println((String) readObject(new File(args[i])));
  }

  /**
   * Convenience method to open a specified file as an
   * {@link ObjectOutputStream}
   * 
   * @param file the target output file
   * @return an ObjectOutputStream linked to the target file
   * @throws IOException
   */
  public static ObjectOutputStream getFileAsObjectOutputStream(File file)
      throws IOException {
    OutputStream out = IOLibrary.getFileAsOutputStream(file);
    return new ObjectOutputStream(new BufferedOutputStream(out));
  }

  /**
   * Convenience method to open a specified file as an {@link ObjectInputStream}
   * 
   * @param file the target input file
   * @return an ObjectInputStream linked to the target file
   * @throws IOException
   */
  public static ObjectInputStream getFileAsObjectInputStream(File file)
      throws IOException {
    InputStream in = IOLibrary.getFileAsInputStream(file);
    return new ObjectInputStream(new BufferedInputStream(in));
  }

  /**
   * Serialize an object to a file by opening an {@link ObjectOutputStream}
   * linked to the file and writing the object to the stream.
   * 
   * @param file the target output file
   * @param o the object to serialize
   * @throws IOException
   */
  public static void writeObject(File file, Object o) throws IOException {
    ObjectOutputStream oos = getFileAsObjectOutputStream(file);
    oos.writeObject(o);
    oos.close();
  }

  /**
   * Read an object from a file by opening the file as an
   * {@link ObjectInputStream} and reading a single object from the stream.
   * 
   * @param file the input file
   * @return an object deserialized from the file
   * @throws IOException
   * @throws ClassNotFoundException
   */
  @SuppressWarnings("unchecked")
  public static <T> T readObject(File file) throws IOException,
      ClassNotFoundException {
    ObjectInputStream ois = getFileAsObjectInputStream(file);
    Object o = ois.readObject();
    ois.close();
    return (T) o;
  }

  /**
   * Convenience method to serialize the string representation of an object, as
   * returned by the {@link #toString()} method, to a file.
   * 
   * @param file the target output file
   * @param o the object to serialize
   * @throws IOException
   */
  public static void printObject(File file, Object o) throws IOException {
    PrintStream out = new PrintStream(new BufferedOutputStream(
        new FileOutputStream(file)));
    out.println(o);
    out.close();
  }

}
