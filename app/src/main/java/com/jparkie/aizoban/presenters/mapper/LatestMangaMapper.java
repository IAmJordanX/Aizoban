package com.jparkie.aizoban.presenters.mapper;

import android.os.Parcelable;
import android.widget.BaseAdapter;

public interface LatestMangaMapper {
    public void registerAdapter(BaseAdapter adapter);

    public Parcelable getPositionState();

    public void setPositionState(Parcelable state);
}
