package com.bikeonet.android.dslrbrowser;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bikeonet.android.dslrbrowser.content.PhotoItem;
import com.bikeonet.android.dslrbrowser.content.PhotoList;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PhotoItem} and makes a call to the
 * specified {@link PhotoListFragment.OnPhotoListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PhotoRecyclerViewAdapter extends RecyclerView.Adapter<PhotoRecyclerViewAdapter.ViewHolder> {

    public static boolean isSelectionMode = false;

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
        holder.checkBox.setText(holder.mItem.getTitle());
        holder.checkBox.setChecked(holder.mItem.isSelected());

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
        public final CheckBox checkBox;
        public PhotoItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mThumbnailView = view.findViewById(R.id.thumbnailView);
            checkBox = view.findViewById(R.id.checkBox);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("com.bikeonet.checkboxli", "checkbox "+(((CheckBox) v).isChecked()?"checked":"unchecked")+" for item "+mItem.toString());
                    PhotoList.selectItem(mItem, ((CheckBox) v).isChecked());
                }
            });

            if (isSelectionMode) {
                checkBox.setVisibility(View.VISIBLE);
            }
            else {
                checkBox.setVisibility(View.GONE);
            }
        }

    }
}
