package com.vrockk.editor;
import android.view.View;


/**
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.1
 * @since 18/01/2017
 * <p>
 * This are the callbacks when any changes happens while editing the photo to make and custimization
 * on client side
 * </p>
 */
public interface OnPhotoEditorListener {


    void onEditTextChangeListener(View rootView, String text, int colorCode , int pos);

    void onAddViewListener(ViewType viewType, int numberOfAddedViews);


    void onRemoveViewListener(ViewType viewType, int numberOfAddedViews);

    void onStartViewChangeListener(ViewType viewType);


    void onStopViewChangeListener(ViewType viewType);
}

