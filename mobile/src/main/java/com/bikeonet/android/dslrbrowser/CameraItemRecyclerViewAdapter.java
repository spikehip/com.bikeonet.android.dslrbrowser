package com.bikeonet.android.dslrbrowser;

import android.graphics.drawable.Icon;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bikeonet.android.dslrbrowser.content.CameraItem;
import com.bikeonet.android.dslrbrowser.content.DummyContent.DummyItem;

import java.io.IOException;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link CameraItemFragment.OnCameraListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CameraItemRecyclerViewAdapter extends RecyclerView.Adapter<CameraItemRecyclerViewAdapter.ViewHolder> {

    private final List<CameraItem> mValues;
    private final CameraItemFragment.OnCameraListFragmentInteractionListener mListener;

    public CameraItemRecyclerViewAdapter(List<CameraItem> items, CameraItemFragment.OnCameraListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.camera_fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getName());
        holder.mContentView.setText(mValues.get(position).getDescription());
        if (mValues.get(position).getLargeIcon() != null ) {
            holder.deviceIcon.setImageBitmap(mValues.get(position).getLargeIcon());
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onCameraListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final ImageView deviceIcon;
        public CameraItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            deviceIcon = (ImageView) view.findViewById(R.id.deviceicon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
