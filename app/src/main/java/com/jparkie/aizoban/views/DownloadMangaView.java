package com.jparkie.aizoban.views;

import android.content.Context;

public interface DownloadMangaView {
    public void initializeToolbar();

    public void initializeListView();

    public void initializeEmptyRelativeLayout();

    public void hideEmptyRelativeLayout();

    public void showEmptyRelativeLayout();

    public void scrollToTop();

    public Context getContext();
}
