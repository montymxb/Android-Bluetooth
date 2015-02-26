/**
 The MIT License (MIT)
 Copyright (c) 2015 Benjamin Wilson Friedman
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package com.uphouseworks.uphw_bluetooth_lib;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BTConnectionManager extends Thread {
    private static BluetoothSocket btSocket;
    private static InputStream btInStream;
    private static OutputStream btOutStream;
    private static boolean canRun = false, isServer;
    private static Set<String> received;
    private static ConnectionManager connectionManager;
    private static short clock120;
    private static StringBuilder sb;
    private static byte pingCount;
    private static String llave1 = "j*%d";
    private static String llave2 = "8Kn$";

    public BTConnectionManager(BluetoothSocket socket, boolean isTheServer) {
        cancel();
        isServer = isTheServer;
        sb = new StringBuilder();
        pingCount = 0;
        clock120 = 0;
        connectionManager = new ConnectionManager();
        received = new HashSet<>();
        btSocket = socket;
        canRun = true;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        btInStream = tmpIn;
        btOutStream = tmpOut;
    }

    public static boolean getConnectedState() {
        return canRun;
    }

    private synchronized void manageConnection() {
        byte[] buffer = new byte[70];

        while(canRun) {
            try {
                int bytes1 = btInStream.read(buffer);
                storeData(bytes1, buffer);
            } catch (IOException ioe) {
                System.out.println("Manage connection io exception, cancelling");
                cancel();
            }
        }

    }

    /**
     * Alias for manageConnection to be performed asynchronously
     */
    public void run() {
        this.manageConnection();
    }

    /**
     * Updates the connection state, basic management:
     * Gets messages that need to be replayed and writes them back into the stream
     * Sends a 'ping' to the other player to maintain connection state
     */
    public static void updateState() {
        if(canRun) {
            ++clock120;
            if(clock120 >= 120) {
                String nm;
                while((nm = connectionManager.getReplay()) != null) {
                    internalWrite(nm.getBytes());
                }

                clock120 = 0;
                ++pingCount;
                if(pingCount < 3) {
                    if(isServer) {
                        internalWrite("^^^".getBytes());
                    }
                } else {
                    cancel();
                }
            }
        }

    }

    /**
     * Requests to add a message to the que
     * @param msg -Message to send
     * @param tag -Relevant message tag
     */
    public static void writeData(String msg, String tag) {
        if(msg != null && tag != null) {
            String tmp = tag + msg;
            byte[] newBytes = tmp.getBytes();
            if(connectionManager != null) {
                connectionManager.addMessage(tmp);
            }
            internalWrite(newBytes);
        }
    }

    /**
     * Writes data to the actual stream, given as a byte array
     * @param bytes -Data to write out to stream
     */
    private static void internalWrite(byte[] bytes) {
        String tmp = new String(bytes);
        if(sb == null) {
            sb = new StringBuilder();
        }

        sb.append("@#$");
        sb.append(tmp);
        sb.append("@#$``");
        byte[] newBytes = sb.toString().getBytes();

        int lc1 = 0, lmax1 = llave1.length();
        int lc2 = 0, lmax2 = llave2.length();

        for(short ne = 0; ne < newBytes.length; ++ne) {
            byte tp = (byte)(newBytes[ne] ^ llave1.charAt(lc1++));
            newBytes[ne] = (byte)(tp ^ llave2.charAt(lc2++));
            if(lc1 == lmax1-1)
                lc1 = 0;
            if(lc2 == lmax2-1)
                lc2 = 0;
        }

        try {
            btOutStream.write(newBytes);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }// catch (NullPointerException npe) {
        //    npe.printStackTrace();
        //}

        sb.delete(0, sb.length());
    }

    /**
     * Retrieves data from the input stream and decodes it. Decoded messages will be placed into a queue with their decoded tag as well, allowing individual components to fetch data by a given tag. This allows individual components within an application to utilize the IOStream independently of each other.
     * @param byteCount -Length of data received
     * @param bBuff -Byte array containing data
     */
    private static void storeData(int byteCount, byte[] bBuff) {
        byte[] tmpBuff = new byte[byteCount];

        int lc1 = 0, lmax1 = llave1.length();
        int lc2 = 0, lmax2 = llave2.length();

        for(short str = 0; str < byteCount; ++str) {
            tmpBuff[str] = bBuff[str];
            byte strArray = (byte)(llave2.charAt(lc2++) ^ tmpBuff[str]);
            tmpBuff[str] = (byte)(llave1.charAt(lc1++) ^ strArray);

            if(lc1 == lmax1-1)
                lc1 = 0;
            if(lc2 == lmax2-1)
                lc2 = 0;
        }

        String strData = new String(tmpBuff);
        String[] messages = strData.split("``");

        for(String tmpStr: messages) {
            if(tmpStr.length() > 6 && tmpStr.substring(0, 3).equals("@#$") && tmpStr.substring(tmpStr.length() - 3, tmpStr.length()).equals("@#$")) {
                tmpStr = tmpStr.substring(3, tmpStr.length() - 3);
                if(tmpStr.equals("^^^")) {
                    pingCount = 0;
                    if(!isServer) {
                        internalWrite("^^^".getBytes());
                    }
                } else {
                    boolean canAdd = true;
                    if(tmpStr.length() > 2 && tmpStr.substring(0, 2).equals("%%")) {
                        canAdd = false;
                        String testThis = tmpStr.substring(2, tmpStr.length());

                        byte e;
                        while((e = connectionManager.removeMessage(testThis)) != 0 && e != 2) { //fast forwards through connection manager to remove all applicable messages
                        }
                    }

                    if(canAdd) {
                        internalWrite(("%%" + tmpStr).getBytes());

                        try {
                            received.add(tmpStr);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    /**
     * Checks the message queue to see if any messages have been received with a given tag. Returns the message if found and removes it from the queue, otherwise returns null;
     * @param tag Tag to search messages by
     * @return Message or Null
     */
    public static String readData(String tag) {
        try {
            String e = null;
            Iterator i$ = received.iterator();

            while(i$.hasNext()) {
                String item = (String)i$.next();
                if(item.length() > tag.length() && item.substring(0, tag.length()).equals(tag)) {
                    e = item.substring(tag.length(), item.length());
                    received.remove(item);
                    break;
                }
            }

            return e;
        } catch (NullPointerException npe) {
            return null;
        }
    }

    /**
     * Flushes all messages in the queue using a given tag
     * @param tag Tag to flush messages by
     */
    public static void clearDataByTag(String tag) {
        try {
            Iterator e = received.iterator();

            while(e.hasNext()) {
                String item = (String)e.next();
                if(item.length() > tag.length() && item.substring(0, tag.length()).equals(tag)) {
                    received.remove(item);
                }
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

    }

    /**
     * Sets the keys used for encrypting the stream
     * @param k1 Key1
     * @param k2 Key2
     */
    public static void setKeys(String k1, String k2) {
        if(k1 != null && k2 != null) {
            llave1 = k1;
            llave2 = k2;
        }
    }

    /**
     * Returns whether or not this user's device is acting as the server
     * @return true/false is server
     */
    public static boolean isServer() {
        return isServer;
    }

    /**
     * Closes IOStreams and socket
     */
    public static void cancel() {
        canRun = false;
        if(btOutStream != null) {
            try {
                btOutStream.close();
                btOutStream = null;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        if(btInStream != null) {
            try {
                btInStream.close();
                btInStream = null;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        if(btSocket != null) {
            try {
                btSocket.close();
                btSocket = null;
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }

        connectionManager = null;
        received = null;
        sb = null;
    }
}
