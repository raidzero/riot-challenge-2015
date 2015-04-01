package com.raidzero.lolstats.interfaces;

import com.raidzero.lolstats.global.Common;

/**
 * Created by posborn on 3/31/15.
 */
public interface RestRequestListener {
    public void onRestRequestComplete(Common.REQUEST_TYPE requestType, int requestCode, String jsonData);
}
