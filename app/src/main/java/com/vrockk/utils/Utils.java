package com.vrockk.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Utils {
    private Context mContext;

    public Utils(Context context) {
        this.mContext = context;

    }

    public static boolean isValidEmail(String email) {
        // onClick of button perform this simplest code.
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (email.matches(emailPattern)) {
            return true;
        } else {
            return false;
        }
    }

    public static String toJson(JsonElement jsonElement) {
        return new Gson().toJson(jsonElement);
    }

    public static DimensionData getScaledDimension(DimensionData imgSize, DimensionData
            boundary) {

        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        double new_width = original_width;
        double new_height = original_height;

        Log.d("video_width >> ", original_width + " video_height >> " + original_height);
        Log.d("display_width >> ", bound_width + " display_height >> " + bound_height);

        /*if (original_width > original_height) {

        } else {

        }*/


        //scale width to fit
        new_width = bound_width;
        //scale height to maintain aspect ratio
        new_height = (new_width * original_height) / original_width;

//        new_height = ((double)original_width / (double) bound_width) * bound_height;


        // first check if we need to scale width
      /*  if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }*/
        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }


        return new DimensionData((int) new_width, (int) new_height);
    }

    public static Bitmap createBitmapFromGLSurface(GLSurfaceView glSurfaceView, GL10 gl) throws OutOfMemoryError {
        int x = 0, y = 0;
        int w = glSurfaceView.getWidth();
        int h = glSurfaceView.getHeight();
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }
        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    static public Bitmap removeTransparency(Bitmap source) {
        int firstX = 0, firstY = 0;
        int lastX = source.getWidth();
        int lastY = source.getHeight();
        int[] pixels = new int[source.getWidth() * source.getHeight()];
        source.getPixels(pixels, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());
        loop:
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstX = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = firstX; x < source.getHeight(); x++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstY = y;
                    break loop;
                }
            }
        }
        loop:
        for (int x = source.getWidth() - 1; x >= firstX; x--) {
            for (int y = source.getHeight() - 1; y >= firstY; y--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastX = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = source.getHeight() - 1; y >= firstY; y--) {
            for (int x = source.getWidth() - 1; x >= firstX; x--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastY = y;
                    break loop;
                }
            }
        }
        return Bitmap.createBitmap(source, firstX, firstY, lastX - firstX, lastY - firstY);
    }

    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
}