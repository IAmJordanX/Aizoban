package com.jparkie.aizoban.views;

import android.content.Context;

public interface SettingsView {
    public void initializeToolbar();

    public void toastClearedFavourite();

    public void toastClearedRecent();

    public void toastClearedImageCache();
    
    public Context getContext();
}
