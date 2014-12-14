package com.jparkie.aizoban.views;

import android.content.Context;
import android.content.Intent;

public interface ChapterView {
    public void initializeToolbar();

    public void initializeViewPager();

    public void initializeEmptyRelativeLayout();

    public void initializeButtons();

    public void hideEmptyRelativeLayout();

    public int getDisplayWidth();

    public int getDisplayHeight();

    public void setTitleText(String title);

    public void setSubtitleProgressText(int imageUrlsCount);

    public void setSubtitlePositionText(int position);

    public void setOptionDirectionText(boolean isRightToLeftDirection);

    public void setOptionOrientationText(boolean isLockOrientation);

    public void setOptionZoomText(boolean isLockZoom);

    public void toastNotInitializedError();

    public void toastChapterError();

    public void toastNoPreviousChapter();

    public void toastNoNextChapter();

    public void finishAndLaunchActivity(Intent launchIntent, boolean isFadeTransition);

    public Context getContext();
}
