package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.presenters.mapper.ChapterMapper;
import com.jparkie.aizoban.utils.PreferenceUtils;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.ChapterView;
import com.jparkie.aizoban.views.activities.ChapterActivity;
import com.jparkie.aizoban.views.activities.MangaActivity;
import com.jparkie.aizoban.views.adapters.PagesAdapter;
import com.jparkie.aizoban.views.fragments.ChapterHelpFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ChapterPresenterOfflineImpl implements ChapterPresenter {
    public static final String TAG = ChapterPresenterOfflineImpl.class.getSimpleName();

    private static final String REQUEST_PARCELABLE_KEY = TAG + ":" + "RequestParcelableKey";
    private static final String IMAGE_URLS_PARCELABLE_KEY = TAG + ":" + "ImageUrlsParcelableKey";

    private static final String INITIALIZED_PARCELABLE_KEY = TAG + ":" + "InitializedParcelableKey";
    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private ChapterView mChapterView;
    private ChapterMapper mChapterMapper;
    private PagesAdapter mPagesAdapter;

    private RequestWrapper mRequest;
    private DownloadChapter mDownloadChapter;
    private RecentChapter mRecentChapter;
    private ArrayList<String> mImageUrls;

    private boolean mIsRightToLeftDirection;
    private boolean mIsLockOrientation;
    private boolean mIsLockZoom;

    private boolean mInitialized;
    private int mInitialPosition;

    private Subscription mQueryDownloadChapterSubscription;
    private Subscription mQueryRecentChapterSubscription;

    public ChapterPresenterOfflineImpl(ChapterView chapterView, ChapterMapper chapterMapper) {
        mChapterView = chapterView;
        mChapterMapper = chapterMapper;
    }

    @Override
    public void handleInitialArguments(Intent arguments) {
        if (arguments != null) {
            if (arguments.hasExtra(ChapterActivity.REQUEST_ARGUMENT_KEY)) {
                mRequest = arguments.getParcelableExtra(ChapterActivity.REQUEST_ARGUMENT_KEY);

                arguments.removeExtra(ChapterActivity.REQUEST_ARGUMENT_KEY);
            }
            if (arguments.hasExtra(ChapterActivity.POSITION_ARGUMENT_KEY)) {
                mInitialPosition = arguments.getIntExtra(ChapterActivity.POSITION_ARGUMENT_KEY, 0);

                arguments.removeExtra(ChapterActivity.POSITION_ARGUMENT_KEY);
            }
        }
    }

    @Override
    public void initializeViews() {
        mChapterView.initializeToolbar();
        mChapterView.initializeViewPager();
        mChapterView.initializeEmptyRelativeLayout();
        mChapterView.initializeButtons();
    }

    @Override
    public void initializeOptions() {
        mIsRightToLeftDirection = PreferenceUtils.isRightToLeftDirection();
        mIsLockOrientation = PreferenceUtils.isLockOrientation();
        mIsLockZoom = PreferenceUtils.isLockZoom();

        mChapterMapper.applyIsLockOrientation(mIsLockOrientation);
        mChapterMapper.applyIsLockZoom(mIsLockZoom);
    }

    @Override
    public void initializeMenu() {
        mChapterView.setOptionDirectionText(mIsRightToLeftDirection);
        mChapterView.setOptionOrientationText(mIsLockOrientation);
        mChapterView.setOptionZoomText(mIsLockZoom);
    }

    @Override
    public void initializeDataFromUrl(FragmentManager fragmentManager) {
        mPagesAdapter = new PagesAdapter(fragmentManager);
        mPagesAdapter.setIsRightToLeftDirection(mIsRightToLeftDirection);

        mChapterMapper.registerAdapter(mPagesAdapter);

        initializeRecentChapter();

        if (!mInitialized) {
            queryChapterFromUrl();
        } else {
            if (mDownloadChapter != null) {
                mChapterView.setTitleText(mDownloadChapter.getName());
            }
            if (mImageUrls != null && mImageUrls.size() != 0) {
                updateAdapter();

                mChapterView.hideEmptyRelativeLayout();
            }
        }
    }

    @Override
    public void saveState(Bundle outState) {
        if (mRequest != null) {
            outState.putParcelable(REQUEST_PARCELABLE_KEY, mRequest);
        }

        if (mDownloadChapter != null) {
            outState.putParcelable(Chapter.PARCELABLE_KEY, mDownloadChapter);
        }

        if (mImageUrls != null) {
            outState.putStringArrayList(IMAGE_URLS_PARCELABLE_KEY, mImageUrls);
        }

        outState.putBoolean(INITIALIZED_PARCELABLE_KEY, mInitialized);

        outState.putInt(POSITION_PARCELABLE_KEY, mInitialPosition);
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(REQUEST_PARCELABLE_KEY)) {
            mRequest = savedState.getParcelable(REQUEST_PARCELABLE_KEY);

            savedState.remove(REQUEST_PARCELABLE_KEY);
        }
        if (savedState.containsKey(Chapter.PARCELABLE_KEY)) {
            mDownloadChapter = savedState.getParcelable(Chapter.PARCELABLE_KEY);

            savedState.remove(Chapter.PARCELABLE_KEY);
        }
        if (savedState.containsKey(IMAGE_URLS_PARCELABLE_KEY)) {
            mImageUrls = savedState.getStringArrayList(IMAGE_URLS_PARCELABLE_KEY);

            savedState.remove(IMAGE_URLS_PARCELABLE_KEY);
        }
        if (savedState.containsKey(INITIALIZED_PARCELABLE_KEY)) {
            mInitialized = savedState.getBoolean(INITIALIZED_PARCELABLE_KEY, false);

            savedState.remove(INITIALIZED_PARCELABLE_KEY);
        }
        if (savedState.containsKey(POSITION_PARCELABLE_KEY)) {
            mInitialPosition = savedState.getInt(POSITION_PARCELABLE_KEY, 0);

            savedState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    @Override
    public void saveChapterToRecentChapters() {
        try {
            if (mInitialized) {
                if (mRecentChapter == null) {
                    mRecentChapter = DefaultFactory.RecentChapter.constructDefault();
                    mRecentChapter.setSource(mDownloadChapter.getSource());
                    mRecentChapter.setUrl(mDownloadChapter.getUrl());
                    mRecentChapter.setParentUrl(mDownloadChapter.getParentUrl());
                    mRecentChapter.setName(mDownloadChapter.getName());
                    mRecentChapter.setOffline(true);
                }

                mRecentChapter.setThumbnailUrl(mImageUrls.get(getActualPosition()));
                mRecentChapter.setDate(System.currentTimeMillis());
                mRecentChapter.setPageNumber(getActualPosition());

                QueryManager.putObjectToApplicationDatabase(mRecentChapter);
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroyAllSubscriptions() {
        if (mQueryDownloadChapterSubscription != null) {
            mQueryDownloadChapterSubscription.unsubscribe();
            mQueryDownloadChapterSubscription = null;
        }
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        Glide.get(mChapterView.getContext()).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        Glide.get(mChapterView.getContext()).clearMemory();
    }

    @Override
    public void onPageSelected(int position) {
        mChapterView.setSubtitlePositionText(getActualPosition() + 1);

        mChapterMapper.applyViewSettings();
    }

    @Override
    public void onFirstPageOut() {
        if (mDownloadChapter != null) {
            if (mIsRightToLeftDirection) {
                nextChapter();
            } else {
                previousChapter();
            }
        }
    }

    @Override
    public void onLastPageOut() {
        if (mDownloadChapter != null) {
            if (mIsRightToLeftDirection) {
                previousChapter();
            } else {
                nextChapter();
            }
        }
    }

    @Override
    public void onPreviousClick() {
        previousChapter();
    }

    @Override
    public void onNextClick() {
        nextChapter();
    }

    @Override
    public void onOptionParent() {
        if (mDownloadChapter != null) {
            Intent mangaIntent = MangaActivity.constructOfflineMangaActivityIntent(mChapterView.getContext(), new RequestWrapper(mDownloadChapter.getSource(), mDownloadChapter.getParentUrl()));
            mangaIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            mChapterView.finishAndLaunchActivity(mangaIntent, false);
        } else {
            mChapterView.toastNotInitializedError();
        }
    }

    @Override
    public void onOptionRefresh() {
        if (mInitialized) {
            mPagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onOptionDirection() {
        if (mInitialized) {
            mIsRightToLeftDirection = !mIsRightToLeftDirection;

            updateAdapter();

            swapPositions();

            mChapterView.setOptionDirectionText(mIsRightToLeftDirection);

            PreferenceUtils.setDirection(mIsRightToLeftDirection);
        } else {
            mChapterView.toastNotInitializedError();
        }
    }

    @Override
    public void onOptionOrientation() {
        if (mInitialized) {
            mIsLockOrientation = !mIsLockOrientation;

            mChapterMapper.applyIsLockOrientation(mIsLockOrientation);

            mChapterView.setOptionOrientationText(mIsLockOrientation);

            PreferenceUtils.setOrientation(mIsLockOrientation);
        } else {
            mChapterView.toastNotInitializedError();
        }
    }

    @Override
    public void onOptionZoom() {
        if (mInitialized) {
            mIsLockZoom = !mIsLockZoom;

            mChapterMapper.applyIsLockZoom(mIsLockZoom);

            mChapterView.setOptionZoomText(mIsLockZoom);

            PreferenceUtils.setZoom(mIsLockZoom);
        } else {
            mChapterView.toastNotInitializedError();
        }
    }

    @Override
    public void onOptionHelp() {
        if (((FragmentActivity)mChapterView.getContext()).getSupportFragmentManager().findFragmentByTag(ChapterHelpFragment.TAG) == null) {
            ChapterHelpFragment chapterHelpFragment = new ChapterHelpFragment();

            chapterHelpFragment.show(((FragmentActivity) mChapterView.getContext()).getSupportFragmentManager(), ChapterHelpFragment.TAG);
        }
    }

    private void initializeRecentChapter() {
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
        }

        if (mRequest != null) {
            mQueryRecentChapterSubscription = QueryManager
                    .queryRecentChapterFromRequest(mRequest, true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Cursor>() {
                        @Override
                        public void onCompleted() {
                            // Do Nothing.
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(Cursor recentCursor) {
                            if (recentCursor != null && recentCursor.getCount() != 0) {
                                mRecentChapter = QueryManager.toObject(recentCursor, RecentChapter.class);
                            }
                        }
                    });
        }
    }

    private void queryChapterFromUrl() {
        if (mQueryDownloadChapterSubscription != null) {
            mQueryDownloadChapterSubscription.unsubscribe();
            mQueryDownloadChapterSubscription = null;
        }

        if (mRequest != null) {
            mQueryDownloadChapterSubscription = QueryManager
                    .queryDownloadChapterFromRequest(mRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Cursor>() {
                        @Override
                        public void onCompleted() {
                            if (mDownloadChapter != null) {
                                File chapterDirectory = new File(mDownloadChapter.getDirectory());
                                if (chapterDirectory.exists() && chapterDirectory.isDirectory()) {
                                    initializeImageUrls(chapterDirectory.listFiles());

                                    updateAdapter();

                                    initializePosition();

                                    mChapterView.hideEmptyRelativeLayout();

                                    mChapterView.setTitleText(mDownloadChapter.getName());

                                    mChapterView.setSubtitlePositionText(getActualPosition() + 1);

                                    mInitialized = true;
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(Cursor chapterCursor) {
                            if (chapterCursor != null && chapterCursor.getCount() != 0) {
                                mDownloadChapter = QueryManager.toObject(chapterCursor, DownloadChapter.class);
                            }
                        }
                    });
        }
    }

    private void initializeImageUrls(File[] files) {
        mImageUrls = new ArrayList<String>();

        File[] imageFiles = files;
        Arrays.sort(imageFiles, new Comparator<File>() {
            @Override
            public int compare(File leftFile, File rightFile) {
                String leftFileNameAndExtension = leftFile.getName().substring(0, leftFile.getName().indexOf("."));
                String rightFileNameAndExtension = rightFile.getName().substring(0, rightFile.getName().indexOf("."));

                int leftFileNumber = Integer.parseInt(leftFileNameAndExtension);
                int rightFileNumber = Integer.parseInt(rightFileNameAndExtension);

                if (leftFileNumber > rightFileNumber) {
                    return 1;
                } else if (leftFileNumber == rightFileNumber) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        for (File imageFile : imageFiles) {
            mImageUrls.add(imageFile.getPath());
        }
    }

    private void updateAdapter() {
        if (mImageUrls != null) {
            ArrayList<String> imageUrls = new ArrayList<String>(mImageUrls.size());

            if (mIsRightToLeftDirection) {
                for (String imageUrl : mImageUrls) {
                    imageUrls.add(new String(imageUrl));
                }

                Collections.reverse(imageUrls);
            } else {
                imageUrls = mImageUrls;
            }

            if (mPagesAdapter != null) {
                mPagesAdapter.setImageUrls(imageUrls);
                mPagesAdapter.setIsRightToLeftDirection(mIsRightToLeftDirection);
            }
        }
    }

    private void initializePosition() {
        if (mPagesAdapter != null && mPagesAdapter.getCount() != 0) {
            if (mInitialPosition >= 0 && mInitialPosition <= mPagesAdapter.getCount() - 1) {
                int currentPosition = mInitialPosition;

                if (mIsRightToLeftDirection) {
                    currentPosition = mPagesAdapter.getCount() - currentPosition - 1;
                }

                mChapterMapper.setPosition(currentPosition);
            }
        }
    }

    private void swapPositions() {
        if (mPagesAdapter != null && mPagesAdapter.getCount() != 0) {
            int oldPosition = mChapterMapper.getPosition();
            int newPosition = mPagesAdapter.getCount() - oldPosition - 1;

            mChapterMapper.setPosition(newPosition);
        }
    }

    private int getActualPosition() {
        int currentPosition = mChapterMapper.getPosition();

        if (mPagesAdapter != null && mPagesAdapter.getCount() != 0) {
            if (mPagesAdapter.getIsRightToLeftDirection()) {
                currentPosition = mPagesAdapter.getCount() - currentPosition - 1;
            }
        }

        return currentPosition;
    }

    private void nextChapter() {
        if (mDownloadChapter != null) {
            if (mQueryDownloadChapterSubscription != null) {
                mQueryDownloadChapterSubscription.unsubscribe();
                mQueryDownloadChapterSubscription = null;
            }

            mQueryDownloadChapterSubscription = QueryManager
                    .queryChapterFromRequest(new RequestWrapper(mDownloadChapter.getSource(), mDownloadChapter.getUrl()))
                    .flatMap(new Func1<Cursor, Observable<Cursor>>() {
                        @Override
                        public Observable<Cursor> call(Cursor chapterCursor) {
                            Chapter chapter = QueryManager.toObject(chapterCursor, Chapter.class);

                            return QueryManager
                                    .queryAdjacentChapterFromRequestAndNumber(new RequestWrapper(mDownloadChapter.getSource(), chapter.getParentUrl()), chapter.getNumber() + 1);
                        }
                    })
                    .map(new Func1<Cursor, String>() {
                        @Override
                        public String call(Cursor adjacentCursor) {
                            if (adjacentCursor != null && adjacentCursor.getCount() != 0) {
                                Chapter adjacentChapter = QueryManager.toObject(adjacentCursor, Chapter.class);

                                if (adjacentChapter != null) {
                                    return adjacentChapter.getUrl();
                                }
                            }

                            return null;
                        }
                    })
                    .flatMap(new Func1<String, Observable<Cursor>>() {
                        @Override
                        public Observable<Cursor> call(String adjacentChapterUrl) {
                            return QueryManager
                                    .queryDownloadChapterFromRequest(new RequestWrapper(mDownloadChapter.getSource(), adjacentChapterUrl));
                        }
                    })
                    .map(new Func1<Cursor, String>() {
                        @Override
                        public String call(Cursor downloadChapterCursor) {
                            DownloadChapter downloadChapter = QueryManager.toObject(downloadChapterCursor, DownloadChapter.class);
                            if (downloadChapter != null) {
                                return downloadChapter.getUrl();
                            }

                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {
                            // Do Nothing.
                        }

                        @Override
                        public void onError(Throwable e) {
                            mChapterView.toastNoNextChapter();

                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(String adjacentChapterUrl) {
                            if (adjacentChapterUrl != null) {
                                Intent adjacentChapterIntent = ChapterActivity.constructOfflineChapterActivityIntent(mChapterView.getContext(), new RequestWrapper(mDownloadChapter.getSource(), adjacentChapterUrl), 0);

                                mChapterView.finishAndLaunchActivity(adjacentChapterIntent, true);
                            } else {
                                mChapterView.toastNoNextChapter();
                            }
                        }
                    });
        } else {
            mChapterView.toastNoNextChapter();
        }
    }

    private void previousChapter() {
        if (mDownloadChapter != null) {
            if (mQueryDownloadChapterSubscription != null) {
                mQueryDownloadChapterSubscription.unsubscribe();
                mQueryDownloadChapterSubscription = null;
            }

            mQueryDownloadChapterSubscription = QueryManager
                    .queryChapterFromRequest(new RequestWrapper(mDownloadChapter.getSource(), mDownloadChapter.getUrl()))
                    .flatMap(new Func1<Cursor, Observable<Cursor>>() {
                        @Override
                        public Observable<Cursor> call(Cursor chapterCursor) {
                            Chapter chapter = QueryManager.toObject(chapterCursor, Chapter.class);

                            return QueryManager
                                    .queryAdjacentChapterFromRequestAndNumber(new RequestWrapper(mDownloadChapter.getSource(), chapter.getParentUrl()), chapter.getNumber() - 1);
                        }
                    })
                    .map(new Func1<Cursor, String>() {
                        @Override
                        public String call(Cursor adjacentCursor) {
                            if (adjacentCursor != null && adjacentCursor.getCount() != 0) {
                                Chapter adjacentChapter = QueryManager.toObject(adjacentCursor, Chapter.class);

                                if (adjacentChapter != null) {
                                    return adjacentChapter.getUrl();
                                }
                            }

                            return null;
                        }
                    })
                    .flatMap(new Func1<String, Observable<Cursor>>() {
                        @Override
                        public Observable<Cursor> call(String adjacentChapterUrl) {
                            return QueryManager
                                    .queryDownloadChapterFromRequest(new RequestWrapper(mDownloadChapter.getSource(), adjacentChapterUrl));
                        }
                    })
                    .map(new Func1<Cursor, String>() {
                        @Override
                        public String call(Cursor downloadChapterCursor) {
                            DownloadChapter downloadChapter = QueryManager.toObject(downloadChapterCursor, DownloadChapter.class);
                            if (downloadChapter != null) {
                                return downloadChapter.getUrl();
                            }

                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {
                            // Do Nothing.
                        }

                        @Override
                        public void onError(Throwable e) {
                            mChapterView.toastNoPreviousChapter();

                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(String adjacentChapterUrl) {
                            if (adjacentChapterUrl != null) {
                                Intent adjacentChapterIntent = ChapterActivity.constructOfflineChapterActivityIntent(mChapterView.getContext(), new RequestWrapper(mDownloadChapter.getSource(), adjacentChapterUrl), 0);

                                mChapterView.finishAndLaunchActivity(adjacentChapterIntent, true);
                            } else {
                                mChapterView.toastNoPreviousChapter();
                            }
                        }
                    });
        } else {
            mChapterView.toastNoPreviousChapter();
        }
    }
}
