package com.jparkie.aizoban.views;

import android.content.Context;

public interface MainView {
    public void initializeToolbar();

    public void initializeDrawerLayout();

    public void closeDrawerLayout();

    public Context getContext();

    public int getNavigationLayoutId();

    public int getMainLayoutId();
}
