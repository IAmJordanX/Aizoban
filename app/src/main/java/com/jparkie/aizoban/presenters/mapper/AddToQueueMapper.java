package com.jparkie.aizoban.presenters.mapper;

import android.os.Parcelable;
import android.util.SparseBooleanArray;
import android.widget.BaseAdapter;

public interface AddToQueueMapper {
    public void registerAdapter(BaseAdapter adapter);

    public int getCheckedItemCount();

    public SparseBooleanArray getCheckedItemPositions();

    public Parcelable getPositionState();

    public void setPositionState(Parcelable state);
}
