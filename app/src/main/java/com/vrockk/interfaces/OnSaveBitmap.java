package com.vrockk.interfaces;

import android.graphics.Bitmap;


public interface OnSaveBitmap {
    void onBitmapReady(Bitmap saveBitmap);

    void onFailure(Exception e);
}
