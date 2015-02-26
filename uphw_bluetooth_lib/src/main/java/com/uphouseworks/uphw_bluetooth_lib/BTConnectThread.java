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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import com.uphouseworks.uphw_bluetooth_lib.BTAcceptThread;
import com.uphouseworks.uphw_bluetooth_lib.BTConnectionManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BTConnectThread extends Thread {
    private static BluetoothSocket btSocket;
    private BluetoothAdapter bluetoothAdapter;
    private Map<Integer, BluetoothDevice> btDeviceList;
    private static byte working;
    private static boolean connected;
    private static UUID ourUUID;

    public BTConnectThread(BluetoothAdapter ba, Map<Integer, BluetoothDevice> btd, UUID inId) {
        cancel();
        this.bluetoothAdapter = ba;
        this.btDeviceList = btd;
        ourUUID = inId;
    }

    public BTConnectThread(BluetoothAdapter ba, BluetoothDevice btd, UUID inId) {
        cancel();
        this.bluetoothAdapter = ba;
        this.btDeviceList = new HashMap<>();
        this.btDeviceList.put(Integer.valueOf(0), btd);
        ourUUID = inId;
    }

    //todo on the first run the app never seems to be able to find anyone...i wonder why that is..
    public void run() {
        working = 1;
        this.bluetoothAdapter.cancelDiscovery();

        while(this.bluetoothAdapter.isDiscovering()) {
            //todo this empty spinner is not really a good idea...
        }

        for(int x = 0; x < this.btDeviceList.size(); ++x) {
            if(this.btDeviceList.get(Integer.valueOf(x)) != null) {
                try {
                    btSocket = ((BluetoothDevice)this.btDeviceList.get(Integer.valueOf(x))).createRfcommSocketToServiceRecord(ourUUID);
                } catch (IOException var5) {

                }

                try {
                    btSocket.connect();
                } catch (Exception var6) {
                    try {
                        btSocket.close();
                    } catch (Exception var4) {

                    }

                    if(x == this.btDeviceList.size() - 1) {
                        working = 0;
                    }
                    continue;
                }

                if(!BTAcceptThread.getConnection()) {
                    connected = true;
                    BTConnectionManager btcm = new BTConnectionManager(btSocket, false);
                    btcm.start();
                    working = 0;
                    break;
                }

                cancel();
            } else if(this.btDeviceList.get(Integer.valueOf(x)) == null) {
                working = 0;
                break;
            }
        }

    }

    public static boolean getConnected() {
        return connected;
    }

    public static void cancel() {
        connected = false;
        if(btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException var1) {

            }
        }

        btSocket = null;
    }

    public static byte getWorkingState() {
        return working;
    }
}
