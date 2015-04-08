package com.raidzero.lolstats.data;

import com.raidzero.lolstats.interfaces.RequestCommandListener;

import java.io.File;

/**
 * Created by raidzero on 4/6/15.
 */
public class RequestCommand {

    public enum RequestType {
        FILE,
        REST
    }

    public String requestUrl;
    public RequestCommandListener listener;
    public int requestId;
    public RequestType requestType;
    public String restResponse;

    public File destDir;
}
