package com.raidzero.lolstats.interfaces;

import com.raidzero.lolstats.data.RequestCommand;

/**
 * Created by raidzero on 4/6/15.
 */
public interface RequestCommandListener {
    void onProcessStart(RequestCommand command);
    void onProcessComplete(RequestCommand command);
}
