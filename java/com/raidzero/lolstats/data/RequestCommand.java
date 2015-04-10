package com.raidzero.lolstats.data;

import com.raidzero.lolstats.interfaces.RequestCommandListener;

import java.io.File;

/**
 * Created by raidzero on 4/6/15.
 */
public class RequestCommand {
    public String requestUrl;
    public RequestCommandListener listener;
    public int requestId;
    public String restResponse;
}
