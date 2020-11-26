package com.vrockk.editor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.annotation.UiThread;

import com.vrockk.R;
import com.vrockk.custom_view.PhotoEditorView;
import com.vrockk.interfaces.OnSaveBitmap;
import com.vrockk.utils.SaveSettings;
import com.vrockk.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotoEditor implements  MultiTouchListener.OnMultiTouchListener{

    private static final String TAG = "PhotoEditor";
    private final LayoutInflater mLayoutInflater;
    private Context context;
    private PhotoEditorView parentView;
    private ImageView imageView;
    private View deleteView;
    private List<View> addedViews;
    private List<View> redoViews;
    private OnPhotoEditorListener mOnPhotoEditorListener;
    private boolean isTextPinchZoomable;
    private Typeface mDefaultTextTypeface;
    private Typeface mDefaultEmojiTypeface;


    private PhotoEditor(Builder builder) {
        this.context = builder.context;
        this.parentView = builder.parentView;
        this.imageView = builder.imageView;
        this.deleteView = builder.deleteView;
        this.isTextPinchZoomable = builder.isTextPinchZoomable;
        this.mDefaultTextTypeface = builder.textTypeface;
        this.mDefaultEmojiTypeface = builder.emojiTypeface;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addedViews = new ArrayList<>();
        redoViews = new ArrayList<>();
    }

    @Override
    public void onEditTextClickListener(String text, int colorCode) {

    }

    @Override
    public void onRemoveViewListener(View removedView) {
        viewUndo(removedView, (ViewType) removedView.getTag());
    }

    private void viewUndo(View removedView, ViewType viewType) {
        if (addedViews.size() > 0) {
            if (addedViews.contains(removedView)) {
                parentView.removeView(removedView);
                addedViews.remove(removedView);
                redoViews.add(removedView);
                if (mOnPhotoEditorListener != null) {
                    mOnPhotoEditorListener.onRemoveViewListener(viewType, addedViews.size());
                }
            }
        }
    }

    private View getLayout(final ViewType viewType) {
        View rootView = null;
        switch (viewType) {
            case TEXT:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null);
                TextView txtText = rootView.findViewById(R.id.tvPhotoEditorText);
                if (txtText != null && mDefaultTextTypeface != null) {
                    txtText.setGravity(Gravity.CENTER);
                    if (mDefaultEmojiTypeface != null) {
                        txtText.setTypeface(mDefaultTextTypeface);
                    }
                }
                break;
            case IMAGE:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_image, null);
                break;
            case EMOJI:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null);
                TextView txtTextEmoji = rootView.findViewById(R.id.tvPhotoEditorText);
                if (txtTextEmoji != null) {
                    if (mDefaultEmojiTypeface != null) {
                        txtTextEmoji.setTypeface(mDefaultEmojiTypeface);
                    }
                    txtTextEmoji.setGravity(Gravity.CENTER);
                    txtTextEmoji.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                break;
        }

        if (rootView != null) {
            //We are setting tag as ViewType to identify what type of the view it is
            //when we remove the view from stack i.e onRemoveViewListener(ViewType viewType, int numberOfAddedViews);
            rootView.setTag(viewType);
//            final ImageView imgClose = rootView.findViewById(R.id.imgPhotoEditorClose);
//            final View finalRootView = rootView;
//            if (imgClose != null) {
//                imgClose.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        viewUndo(finalRootView, viewType);
//                    }
//                });
//            }
        }
        return rootView;
    }
    @NonNull
    private MultiTouchListener getMultiTouchListener() {
        MultiTouchListener multiTouchListener = new MultiTouchListener(
                deleteView,
                parentView,
                this.imageView,
                isTextPinchZoomable,
                mOnPhotoEditorListener);

        //multiTouchListener.setOnMultiTouchListener(this);

        return multiTouchListener;
    }

    public void setOnPhotoEditorListener(@NonNull OnPhotoEditorListener onPhotoEditorListener) {
        this.mOnPhotoEditorListener = onPhotoEditorListener;
    }
    public void addImage(Bitmap desiredImage) {
        final View imageRootView = getLayout(ViewType.IMAGE);
        final ImageView imageView = imageRootView.findViewById(R.id.imgPhotoEditorImage);
//        final FrameLayout frmBorder = imageRootView.findViewById(R.id.frmBorder);
//        final ImageView imgClose = imageRootView.findViewById(R.id.imgPhotoEditorClose);

        imageView.setImageBitmap(desiredImage);

        MultiTouchListener multiTouchListener = getMultiTouchListener();
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {
//                boolean isBackgroundVisible = frmBorder.getTag() != null && (boolean) frmBorder.getTag();
//                frmBorder.setBackgroundResource(isBackgroundVisible ? 0 : R.drawable.rounded_border_tv);
//                imgClose.setVisibility(isBackgroundVisible ? View.GONE : View.VISIBLE);
//                frmBorder.setTag(!isBackgroundVisible);
            }

            @Override
            public void onLongClick() {

            }
        });

        multiTouchListener.setOnMultiTouchListener(this);

        imageRootView.setOnTouchListener(multiTouchListener);

        addViewToParent(imageRootView, ViewType.IMAGE);

    }
    private void addViewToParent(View rootView, ViewType viewType) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        parentView.addView(rootView, params);
        addedViews.add(rootView);
        if (mOnPhotoEditorListener != null)
            mOnPhotoEditorListener.onAddViewListener(viewType, addedViews.size());
    }
    private static String convertEmoji(String emoji) {
        String returnedEmoji;
        try {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            returnedEmoji = new String(Character.toChars(convertEmojiToInt));
        } catch (NumberFormatException e) {
            returnedEmoji = "";
        }
        return returnedEmoji;
    }



    public static class Builder {

        private Context context;
        private PhotoEditorView parentView;
        private ImageView imageView;
        private View deleteView;
       // private BrushDrawingView brushDrawingView;
        private Typeface textTypeface;
        private Typeface emojiTypeface;
        //By Default pinch zoom on text is enabled
        private boolean isTextPinchZoomable = true;

        /**
         * Building a PhotoEditor which requires a Context and PhotoEditorView
         * which we have setup in our xml layout
         *
         * @param context         context
         * @param photoEditorView {@link PhotoEditorView}
         */
        public Builder(Context context, PhotoEditorView photoEditorView) {
            this.context = context;
            parentView = photoEditorView;
            imageView = photoEditorView.getSource();
          //  brushDrawingView = photoEditorView.getBrushDrawingView();
        }

        public Builder setDeleteView(View deleteView) {
            this.deleteView = deleteView;
            return this;
        }


        public Builder setDefaultTextTypeface(Typeface textTypeface) {
            this.textTypeface = textTypeface;
            return this;
        }


        public Builder setDefaultEmojiTypeface(Typeface emojiTypeface) {
            this.emojiTypeface = emojiTypeface;
            return this;
        }


        public Builder setPinchTextScalable(boolean isTextPinchZoomable) {
            this.isTextPinchZoomable = isTextPinchZoomable;
            return this;
        }


        public PhotoEditor build() {
            return new PhotoEditor(this);
        }
    }

    @UiThread
    public void clearHelperBox() {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View childAt = parentView.getChildAt(i);
//            FrameLayout frmBorder = childAt.findViewById(R.id.frmBorder);
//            if (frmBorder != null) {
//                frmBorder.setBackgroundResource(0);
//            }
//            ImageView imgClose = childAt.findViewById(R.id.imgPhotoEditorClose);
//            if (imgClose != null) {
//                imgClose.setVisibility(View.GONE);
//            }
        }
    }


    public static ArrayList<String> getEmojis(Context context) {
        ArrayList<String> convertedEmojiList = new ArrayList<>();
        String[] emojiList = context.getResources().getStringArray(R.array.photo_editor_emoji);
        for (String emojiUnicode : emojiList) {
            convertedEmojiList.add(convertEmoji(emojiUnicode));
        }
        return convertedEmojiList;
    }
    public interface OnSaveListener {

        void onSuccess(@NonNull String imagePath);

        void onFailure(@NonNull Exception exception);
    }


    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveAsFile(@NonNull final String imagePath, @NonNull final OnSaveListener onSaveListener) {
        saveAsFile(imagePath, new SaveSettings.Builder().build(), onSaveListener);
    }

    public void saveAsBitmap(@NonNull final SaveSettings saveSettings,
                             @NonNull final OnSaveBitmap onSaveBitmap) {
        parentView.saveFilter(new OnSaveBitmap() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                new AsyncTask<String, String, Bitmap>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        clearHelperBox();
                        parentView.setDrawingCacheEnabled(false);
                    }

                    @Override
                    protected Bitmap doInBackground(String... strings) {
                        if (parentView != null) {
                            parentView.setDrawingCacheEnabled(true);
                            return saveSettings.isTransparencyEnabled() ?
                                    Utils.removeTransparency(parentView.getDrawingCache())
                                    : parentView.getDrawingCache();
                        } else {
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        if (bitmap != null) {
                            if (saveSettings.isClearViewsEnabled()) clearAllViews();
                            onSaveBitmap.onBitmapReady(bitmap);
                        } else {
                            onSaveBitmap.onFailure(new Exception("Failed to load the bitmap"));
                        }
                    }

                }.execute();
            }

            @Override
            public void onFailure(Exception e) {
                onSaveBitmap.onFailure(e);
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveAsFile(@NonNull final String imagePath,
                           @NonNull final SaveSettings saveSettings,
                           @NonNull final OnSaveListener onSaveListener) {
        Log.d(TAG, "Image Path: " + imagePath);
        parentView.saveFilter(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                new AsyncTask<String, String, Exception>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        clearHelperBox();
                        parentView.setDrawingCacheEnabled(false);
                    }

                    @SuppressLint("MissingPermission")
                    @Override
                    protected Exception doInBackground(String... strings) {
                        // Create a media file name
                        File file = new File(imagePath);
                        try {
                            FileOutputStream out = new FileOutputStream(file, false);
                            if (parentView != null) {
                                parentView.setDrawingCacheEnabled(true);
                                Bitmap drawingCache = saveSettings.isTransparencyEnabled()
                                        ? Utils.removeTransparency(parentView.getDrawingCache())
                                        : parentView.getDrawingCache();
                                drawingCache.compress(saveSettings.getCompressFormat(), saveSettings.getCompressQuality(), out);
                            }
                            out.flush();
                            out.close();
                            Log.d(TAG, "Filed Saved Successfully");
                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Failed to save File");
                            return e;
                        }
                    }

                    @Override
                    protected void onPostExecute(Exception e) {
                        super.onPostExecute(e);
                        if (e == null) {
                            //Clear all views if its enabled in save settings
                            if (saveSettings.isClearViewsEnabled()) clearAllViews();
                            onSaveListener.onSuccess(imagePath);
                        } else {
                            onSaveListener.onFailure(e);
                        }
                    }

                }.execute();
            }

            @Override
            public void onFailure(Exception e) {
                onSaveListener.onFailure(e);
            }
        });
    }

    public void clearAllViews() {
        for (int i = 0; i < addedViews.size(); i++) {
            parentView.removeView(addedViews.get(i));
        }
        addedViews.clear();
        redoViews.clear();
    }
}
