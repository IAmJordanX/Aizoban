package com.jparkie.aizoban.views;

import android.content.Context;

public interface MangaView {
    public void initializeToolbar();

    public void initializeSwipeRefreshLayout();

    public void initializeListView();

    public void initializeDeletionListView();

    public void initializeEmptyRelativeLayout();

    public void initializeFavouriteButton(boolean isFavourite);

    public void hideEmptyRelativeLayout();

    public void showListViewIfHidden();

    public void showChapterStatusError();

    public void hideChapterStatusError();

    public void showRefreshing();

    public void hideRefreshing();

    public void setTitle(String title);

    public void setName(String name);

    public void setDescription(String description);

    public void setAuthor(String author);

    public void setArtist(String artist);

    public void setGenre(String genre);

    public void setIsCompleted(boolean isCompleted);

    public void setThumbnail(String url);

    public void setFavouriteButton(boolean isFavourite);

    public int getHeaderViewsCount();

    public void scrollToTop();

    public void selectAll();

    public void clear();

    public void toastMangaError();

    public Context getContext();
}
