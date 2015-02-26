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

import java.util.HashMap;

public class ConnectionManager {
    private HashMap<Integer, String> messages = new HashMap<>();
    private int currentMarker = 0;

    public ConnectionManager() {
    }

    public void addMessage(String data) {
        if(!this.messages.containsValue(data)) {
            this.messages.put(Integer.valueOf(this.messages.size()), data);
        }

    }

    public byte removeMessage(String reply) {
        String tmp = this.retrieveAndAdvance();
        if(tmp == null) {
            return (byte)0;
        } else if(reply.equals(tmp)) {
            this.messages.remove(Integer.valueOf(this.currentMarker - 1));
            this.currentMarker = 0;
            return (byte)2;
        } else {
            return (byte)1;
        }
    }

    public String getReplay() {
        return this.retrieveAndAdvance();
    }

    private String retrieveAndAdvance() {
        if(this.messages.size() > 0) {
            if(this.currentMarker >= this.messages.size()) {
                this.currentMarker = 0;
                return null;
            } else {
                String msg = (String)this.messages.get(Integer.valueOf(this.currentMarker));
                ++this.currentMarker;
                return msg;
            }
        } else {
            return null;
        }
    }
}

