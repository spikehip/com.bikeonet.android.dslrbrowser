package com.bikeonet.android.dslrbrowser;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bikeonet.android.dslrbrowser.content.DummyContent.DummyItem;
import com.bikeonet.android.dslrbrowser.content.PhotoItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link PhotoListFragment.OnPhotoListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PhotoRecyclerViewAdapter extends RecyclerView.Adapter<PhotoRecyclerViewAdapter.ViewHolder> {

    private final List<PhotoItem> mValues;
    private final PhotoListFragment.OnPhotoListFragmentInteractionListener mListener;

    public PhotoRecyclerViewAdapter(List<PhotoItem> items, PhotoListFragment.OnPhotoListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        //holder.mIdView.setText(mValues.get(position).id);
        if (mValues.get(position).getThumbnail() != null ) {
            holder.mThumbnailView.setImageBitmap(mValues.get(position).getThumbnail());
        }

        //holder.mProgressView.setText("0 %");

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPhotoListFragmentInteraction(holder.mItem);
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
        public final ImageView mThumbnailView;
        //public final TextView mProgressView;
        public PhotoItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mThumbnailView = (ImageView) view.findViewById(R.id.thumbnailView);
            //mProgressView = (TextView) view.findViewById(R.id.progressView);
        }

//        @Override
//        public String toString() {
//            return super.toString() + " '" + mProgressView.getText() + "'";
//        }
    }
}
