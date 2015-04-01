package com.raidzero.lolstats.interfaces;

import android.net.Uri;

/**
 * Created by posborn on 4/1/15.
 */
public interface FileRequestListener {
    void onFileDownloadStart(int requestCode);
    void onFileComplete(int requestCode, Uri fileUri);
}
