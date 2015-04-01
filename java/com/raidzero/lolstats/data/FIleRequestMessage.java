package com.raidzero.lolstats.data;

import android.os.Message;

import com.raidzero.lolstats.global.Common;

/**
 * Created by posborn on 4/1/15.
 */
public class FileRequestMessage {
    public Common.FILE_MESSAGE_TYPE msgType;
    public Object obj;

    public FileRequestMessage(Common.FILE_MESSAGE_TYPE msgType) {
        this.msgType = msgType;
    }
}
