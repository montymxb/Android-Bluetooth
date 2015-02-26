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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

public class BTCore extends Activity {
    private final BluetoothAdapter mBluetoothAdapter;
    private static BroadcastReceiver mReceiver;
    private static Map<Integer, BluetoothDevice> btDevice;
    private static int discoveryTime, deviceSlot, DISCOVERY_TIME = 120;
    private static boolean btOn, waiting;
    private static Activity activity;
    private static long waitTime;
    private static short btDeviceCount;
    private static UUID btcoreUUID;

    /**
     * Sets up BTCore with default params
     */
    public BTCore() {
        btcoreUUID = UUID.fromString("53462220-c363-11e2-8b8b-0800200c9a66");
        initializeControlVars();
        if(btDevice == null)
            btDevice = new HashMap<>();

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        discoveryTime = DISCOVERY_TIME;
        mReceiver = null;
    }

    /**
     * Sets up BTCore
     * @param ourUUID UUID to register and disover devices by
     */
    public BTCore(UUID ourUUID) {
        btcoreUUID = ourUUID;
        initializeControlVars();
        if(btDevice == null) {
            btDevice = new HashMap<>();
        }

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        discoveryTime = DISCOVERY_TIME;
        mReceiver = null;
    }

    /**
     * Sets up BTCore
     * @param ourUUID UUID to register and discover devices by
     * @param discoverytime Time to spend discovering devices
     */
    public BTCore(UUID ourUUID, int discoverytime) {
        btcoreUUID = ourUUID;
        DISCOVERY_TIME = discoveryTime;
        initializeControlVars();
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(discoverytime <= 0) {
            discoverytime = 1;
        }

        discoveryTime = discoverytime;
        mReceiver = null;
    }

    /**
     * Sets up BTCore
     * @param ourUUID UUID to register and discover devices by
     * @param discoverytime Time to spend discovering devices
     * @param key1 First key for encrypting traffic
     * @param key2 Second key for encrypting traffic
     */
    public BTCore(UUID ourUUID, int discoverytime, String key1, String key2) {
        btcoreUUID = ourUUID;
        DISCOVERY_TIME = discoveryTime;
        initializeControlVars();
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(discoverytime <= 0) {
            discoverytime = 1;
        }

        discoveryTime = discoverytime;
        mReceiver = null;

        //Sets keys
        BTConnectionManager.setKeys(key1, key2);
    }

    /**
     * Sets all symaphores and control vars to their default values
     */
    private static void initializeControlVars() {
        btDeviceCount = 0;
        waitTime = -1L;
        waiting = false;
        btOn = false;
        deviceSlot = 0;
    }

    /**
     * Starts an intent to request that bluetooth is enabled
     * @param act The activity utilized to present the activity from
     * @return
     */
    public int setBTEnable(Activity act) {
        if(activity == null)
            activity = act;

        if(act == null)
            act = activity;

        byte REQUEST_ENABLE_BT = 1;
        if(this.mBluetoothAdapter != null) {
            if(!this.mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
                act.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                waiting = true;
                btOn = false;
                return 0;
            } else {
                waiting = false;
                btOn = true;
                return 1;
            }
        } else {
            waiting = false;
            btOn = false;
            return 2;
        }
    }

    /**
     * Start discovering available bluetooth devices and caching them locally
     * @return Number of devices discovered
     */
    public int setBTDiscovery() {
        btDevice.clear();
        deviceSlot = 0;
        if(btOn && !waiting) {
            Intent discoverableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
            discoverableIntent.putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", discoveryTime);
            activity.startActivity(discoverableIntent);
            Set pairedDevices = this.mBluetoothAdapter.getBondedDevices();
            if(pairedDevices != null && pairedDevices.size() > 0) {
                Iterator discoveryActive = pairedDevices.iterator();

                while(discoveryActive.hasNext()) {
                    BluetoothDevice device = (BluetoothDevice)discoveryActive.next();
                    if(device.getUuids() != null) {
                        ParcelUuid[] arr$ = device.getUuids();
                        for(ParcelUuid pu: arr$) {
                            if(pu.getUuid().equals(btcoreUUID)) {
                                btDevice.put(deviceSlot, device);
                                ++deviceSlot;
                                break;
                            }
                        }
                    }
                }
            }

            boolean var10 = this.mBluetoothAdapter.startDiscovery();
            waitTime = System.currentTimeMillis();
            if(var10) {
                mReceiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if("android.bluetooth.device.action.FOUND".equals(action)) {
                            BluetoothDevice device = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                            if(device != null) {
                                boolean duplicate = false;
                                Iterator i$ = BTCore.btDevice.entrySet().iterator();

                                while(i$.hasNext()) {
                                    Entry item = (Entry)i$.next();
                                    BluetoothDevice daD = (BluetoothDevice)item.getValue();
                                    if(daD == null) {
                                        break;
                                    }

                                    if(daD.equals(device)) {
                                        duplicate = true;
                                        break;
                                    }
                                }

                                if(!duplicate && device.getName() != null) {
                                    btDevice.put(deviceSlot, device);
                                    //todo what in the hell was this???>>BTCore.access$108();
                                }
                            }
                        }

                    }
                };
                activity.registerReceiver(mReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
            }
        }
        return deviceSlot;
    }

    /**
     * Updates the state of the connection
     * @param autoStartClient Boolean indicating whether or not the client should automatically fire up
     */
    public void updateState(boolean autoStartClient) {
        if(System.currentTimeMillis() - waitTime > 10000L && waitTime != -1L) {
            waitTime = -1L;
            if(autoStartClient) {
                this.startClient();
            }
        }

        BTConnectionManager.updateState();
    }

    /**
     * Handles callback for turning on bluetooth
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1 && !btOn) {
            btOn = true;
            waiting = false;
            this.setBTDiscovery();
        }

    }

    /**
     * Returns whether or not bluetooth is on
     * @return
     */
    public static boolean getBluetoothOn() {
        return btOn;
    }

    /**
     * Unregisters broadcast receiver for finding bluetooth devices
     */
    public void unregisterBroadcastReceiver() {
        if(mReceiver != null) {
            try {
                activity.unregisterReceiver(mReceiver);
                mReceiver = null;
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
            }
        }

    }

    /**
     * Starts up a client instance using the default bluetooth adapter, device, and UUID
     */
    public void startClient() {
        BTConnectThread btct = new BTConnectThread(this.mBluetoothAdapter, btDevice, btcoreUUID);
        btct.start();
    }

    /**
     * Returns the last bluetooth device added to the stack
     * @return last bluetooth device added
     */
    public BluetoothDevice retrieveFoundDevice() {
        if(btDeviceCount < btDevice.size()) {
            ++btDeviceCount;
            if(btDevice.get(btDeviceCount - 1) == null) {
                btDeviceCount = 0;
                return null;
            } else {
                return btDevice.get(btDeviceCount - 1);
            }
        } else {
            btDeviceCount = 0;
            return null;
        }
    }

    /**
     * Returns number of bluetooth devices found
     * @return # devices found
     */
    public int retrieveFoundDeviceCount() {
        return btDevice.size();
    }

    /**
     * Fetches a bluetooth device from a given index
     * @param index index
     * @return Bluetooth device at index
     */
    public BluetoothDevice retrieveDeviceWithIndex(int index) {
        if(index > btDevice.size() - 1)
            index = btDevice.size() - 1;
        else if(index < 0)
            index = 0;
        return btDevice.get(index);
    }

    /**
     * Starts up a bluetooth client instance with a given bluetooth device
     * @param btd BluetoothDevice passed on to start client
     */
    public void startClient(BluetoothDevice btd) {
        new BTConnectThread(this.mBluetoothAdapter, btd, btcoreUUID).start();
    }

    /**
     * Starts up a bluetooth server instance
     */
    public void startServer() {
        new BTAcceptThread(this.mBluetoothAdapter, btcoreUUID).start();
    }

    /**
     * Gets our device name
     * @return device name
     */
    public String getOurName() {
        return (this.mBluetoothAdapter != null)?this.mBluetoothAdapter.getName():null;
    }

    /**
     * Sets our device's bluetooth name
     * @param newName New device name
     */
    public void setOurName(String newName) {
        if(this.mBluetoothAdapter != null)
            this.mBluetoothAdapter.setName(newName);
    }

    /**
     * Shuts down all bluetooth activity related to the application asap, no mercy here this is a kill call
     */
    public void stopBluetooth() {
        if(this.mBluetoothAdapter != null)
            this.mBluetoothAdapter.cancelDiscovery();
        BTConnectThread.cancel();
        BTAcceptThread.cancel();
        BTConnectionManager.cancel();
    }
}
