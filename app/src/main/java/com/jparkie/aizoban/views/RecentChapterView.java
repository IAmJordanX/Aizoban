package com.jparkie.aizoban.views;

import android.content.Context;

public interface RecentChapterView {
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
