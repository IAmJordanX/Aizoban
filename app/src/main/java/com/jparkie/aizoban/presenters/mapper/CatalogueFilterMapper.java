package com.jparkie.aizoban.presenters.mapper;

import android.os.Parcelable;
import android.widget.BaseAdapter;

import java.util.List;

public interface CatalogueFilterMapper {
    public void registerAdapter(BaseAdapter adapter);

    public List<String> getSelectedGenres();

    public void setSelectedGenres(List<String> selectedGenres);

    public String getSelectedStatus();

    public void setSelectedStatus(String selectedStatus);

    public String getSelectedOrderBy();

    public void setSelectedOrderBy(String selectedOrderBy);

    public Parcelable getPositionState();

    public void setPositionState(Parcelable state);
}
