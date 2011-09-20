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
package org.onebusaway.phone.impl;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiOperations;
import org.onebusaway.probablecalls.TextToSpeechFactory;

public class SwiftAndSoxTextToSpeechFactoryImpl implements TextToSpeechFactory {

    private static final String AUDIO_EXTENSION = "gsm";

    private MessageDigest _digester;

    private File _outputDirectory;

    private String _soxPath = "sox";

    private String _swiftPath = "swift";

    private SwiftAndSoxTextToSpeechFactoryImpl() {
        try {
            _digester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void setOutputDirectory(File outputDirectory) {
        _outputDirectory = outputDirectory;
    }

    public void setSoxPath(String soxPath) {
        _soxPath = soxPath;
    }

    public void setSwiftPath(String swiftPath) {
        _swiftPath = swiftPath;
    }

    public char getAudio(AgiOperations opts, String text, String escapeDigits) throws IOException, AgiException {

        String id = getAudioFile(text);
        return opts.streamFile(id, escapeDigits);
    }

    private synchronized String getAudioFile(String text) throws IOException {
        String id = _outputDirectory.getAbsolutePath() + "/" + getHash(text);
        File audioFile = new File(id + "." + AUDIO_EXTENSION);

        if (!audioFile.exists())
            generateAudio(text, audioFile);
        return id;
    }

    private String getHash(String text) {

        _digester.reset();
        byte[] digest = _digester.digest(text.getBytes());

        StringBuffer buffer = new StringBuffer();

        for (byte b : digest) {
            String hex = Integer.toHexString((int) b & 0xff);
            if (hex.length() == 1)
                buffer.append('0');
            buffer.append(hex);
        }

        return buffer.toString();
    }

    private void generateAudio(String text, File audioFile) throws IOException {

        File tmp = getSpokenAudio(text);
        convertAudio(tmp, audioFile);

        tmp.delete();
    }

    private File getSpokenAudio(String text) throws IOException {

        File tmp = File.createTempFile("Audio-", ".wav");
        tmp.deleteOnExit();

        String[] args = { _swiftPath, "-o", tmp.getAbsolutePath(), text };
        Process p = Runtime.getRuntime().exec(args);

        try {
            int retro = p.waitFor();
            if (retro != 0)
                throw new IOException("Error creating audio: exit value=" + retro);
        } catch (InterruptedException ex) {
            throw new IOException("Error creating audio: interrupted before completion");
        }
        return tmp;
    }

    private void convertAudio(File input, File output) throws IOException {
        String[] args = { _soxPath, input.getAbsolutePath(), "-r", "8000", "-c", "1", output.getAbsolutePath() };
        Process p = Runtime.getRuntime().exec(args);
        try {
            int retro = p.waitFor();
            if (retro != 0) {
                StringBuffer sb = new StringBuffer();
                for (String arg : args)
                    sb.append(arg).append(' ');
                throw new IOException("Error converting audio: exit value=" + retro + " cmd=" + sb.toString());
            }
        } catch (InterruptedException e) {
            throw new IOException("Error converting audio: interrupted before completion");
        }
    }
}
