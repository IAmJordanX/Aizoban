package com.jparkie.aizoban.views;

import android.content.Context;

public interface FavouriteMangaView {
    public void initializeToolbar();

    public void initializeListView();

    public void initializeEmptyRelativeLayout();

    public void hideEmptyRelativeLayout();

    public void showEmptyRelativeLayout();

    public void scrollToTop();

    public void selectAll();

    public void clear();

    public Context getContext();
}
