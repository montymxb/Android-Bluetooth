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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.util.UUID;

public class BTAcceptThread extends Thread {
    private static BluetoothServerSocket btServerSocket;
    private static boolean connected;

    /**
     * Takes a bluetooth adapter and a given UUID
     * @param ba -Bluetooth Adapter object
     * @param inId -UUID for application to use
     */
    public BTAcceptThread(BluetoothAdapter ba, UUID inId) {
        cancel();
        BluetoothServerSocket tempServerSocket = null;

        try {
            tempServerSocket = ba.listenUsingRfcommWithServiceRecord("UphouseworksBT", inId);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        btServerSocket = tempServerSocket;
    }

    /**
     * Handles accepting a connection
     */
    public void run() {
        BluetoothSocket socket = null;
        if(btServerSocket != null) {
            try {
                socket = btServerSocket.accept();
                btServerSocket.close();
            } catch (IOException ioe) {
                try {
                    btServerSocket.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }

            if(socket != null && !BTConnectionManager.getConnectedState()) {
                connected = true;
                new BTConnectionManager(socket, true).start();
            } else {
                cancel();
            }
        }

    }

    /**
     * Returns whether the socket is currently connected
     * @return -connected state as a boolean
     */
    public static boolean getConnection() {
        return connected;
    }

    /**
     * Disconnects the socket
     */
    public static void cancel() {
        connected = false;
        if(btServerSocket != null) {
            try {
                btServerSocket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }
}

