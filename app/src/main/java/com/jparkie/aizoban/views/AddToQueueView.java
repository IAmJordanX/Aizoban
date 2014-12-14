package com.jparkie.aizoban.views;

import android.content.Context;

public interface AddToQueueView {
    public void initializeEmptyRelativeLayout();

    public void hideEmptyRelativeLayout();

    public void showEmptyRelativeLayout();

    public void overrideToggleButton();

    public void selectAllItems();

    public void deselectAllItems();

    public Context getContext();
}
