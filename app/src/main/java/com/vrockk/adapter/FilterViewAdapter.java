package com.vrockk.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.vrockk.custom_view.PhotoFilter;
import com.vrockk.interfaces.FilterListener;
import com.vrockk.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FilterViewAdapter extends RecyclerView.Adapter<FilterViewAdapter.ViewHolder> {

    private FilterListener mFilterListener;
    private List<Pair<String, PhotoFilter>> mPairList = new ArrayList<>();

    public FilterViewAdapter(FilterListener filterListener) {
        mFilterListener = filterListener;
        setupFilters();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_filter_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<String, PhotoFilter> filterPair = mPairList.get(position);
        Bitmap fromAsset = getBitmapFromAsset(holder.itemView.getContext(), filterPair.first);
        holder.mImageFilterView.setImageBitmap(fromAsset);
        holder.mTxtFilterName.setText(filterPair.second.name().replace("_", " "));
    }

    @Override
    public int getItemCount() {
        return mPairList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageFilterView;
        TextView mTxtFilterName;

        ViewHolder(View itemView) {
            super(itemView);
            mImageFilterView = itemView.findViewById(R.id.imgFilterView);
            mTxtFilterName = itemView.findViewById(R.id.txtFilterName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilterListener.onFilterSelected(getLayoutPosition(),mPairList.get(getLayoutPosition()).second);
                }
            });
        }
    }

    private Bitmap getBitmapFromAsset(Context context, String strName) {
        AssetManager assetManager = context.getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
            return BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupFilters() {
        mPairList.add(new Pair<>("filters/original.jpg", com.vrockk.custom_view.PhotoFilter.NONE));
        mPairList.add(new Pair<>("filters/brightness.png", com.vrockk.custom_view.PhotoFilter.BRIGHTNESS));
        mPairList.add(new Pair<>("filters/contrast.png", com.vrockk.custom_view.PhotoFilter.CONTRAST));
        mPairList.add(new Pair<>("filters/documentary.png", com.vrockk.custom_view.PhotoFilter.DOCUMENTARY));
        mPairList.add(new Pair<>("filters/fill_light.png", com.vrockk.custom_view.PhotoFilter.FILL_LIGHT));
        mPairList.add(new Pair<>("filters/gray_scale.png", com.vrockk.custom_view.PhotoFilter.GRAY_SCALE));
        mPairList.add(new Pair<>("filters/negative.png", com.vrockk.custom_view.PhotoFilter.NEGATIVE));
        mPairList.add(new Pair<>("filters/posterize.png", com.vrockk.custom_view.PhotoFilter.POSTERIZE));
        mPairList.add(new Pair<>("filters/saturate.png", com.vrockk.custom_view.PhotoFilter.SATURATE));
        mPairList.add(new Pair<>("filters/sepia.png", com.vrockk.custom_view.PhotoFilter.SEPIA));
        mPairList.add(new Pair<>("filters/sharpen.png", com.vrockk.custom_view.PhotoFilter.SHARPEN));
        mPairList.add(new Pair<>("filters/temprature.png", com.vrockk.custom_view.PhotoFilter.TEMPERATURE));
        mPairList.add(new Pair<>("filters/tint.png", com.vrockk.custom_view.PhotoFilter.TINT));
        mPairList.add(new Pair<>("filters/vignette.png", com.vrockk.custom_view.PhotoFilter.VIGNETTE));
        //mPairList.add(new Pair<>("filters/b_n_w.png", PhotoFilter.BLACK_WHITE));
    }
}

