package com.jparkie.aizoban.views;

import android.content.Context;

public interface CatalogueView {
    public void initializeToolbar();

    public void initializeGridView();

    public void initializeEmptyRelativeLayout();

    public void hideEmptyRelativeLayout();

    public void showEmptyRelativeLayout();

    public void scrollToTop();

    public Context getContext();
}
