package com.jparkie.aizoban.views;

import android.content.Context;

public interface LatestMangaView {
    public void initializeToolbar();

    public void initializeSwipeRefreshLayout();

    public void initializeGridView();

    public void initializeEmptyRelativeLayout();

    public void hideEmptyRelativeLayout();

    public void showRefreshing();

    public void hideRefreshing();

    public void scrollToTop();

    public void toastLatestError();

    public Context getContext();
}
