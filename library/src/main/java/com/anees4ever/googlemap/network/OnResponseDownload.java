package com.anees4ever.googlemap.network;

import java.io.File;

public interface OnResponseDownload {
    void onResponse(final boolean status, final File savedFile);
    void onProgress(final long readBytes, final long totalBytes);
}
