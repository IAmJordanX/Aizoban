package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.CataloguePresenter;
import com.jparkie.aizoban.presenters.CataloguePresenterImpl;
import com.jparkie.aizoban.presenters.mapper.CatalogueMapper;
import com.jparkie.aizoban.views.CatalogueView;

public class CatalogueFragment extends Fragment implements CatalogueView, CatalogueMapper {
    public static final String TAG = CatalogueFragment.class.getSimpleName();

    private CataloguePresenter mCataloguePresenter;

    private GridView mGridView;
    private RelativeLayout mEmptyRelativeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mCataloguePresenter = new CataloguePresenterImpl(this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View catalogueView = inflater.inflate(R.layout.fragment_catalogue, container, false);

        mGridView = (GridView) catalogueView.findViewById(R.id.gridView);
        mEmptyRelativeLayout = (RelativeLayout) catalogueView.findViewById(R.id.emptyRelativeLayout);

        return catalogueView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCataloguePresenter.restoreState(savedInstanceState);
        }

        mCataloguePresenter.initializeViews();

        mCataloguePresenter.initializeSearch();

        mCataloguePresenter.initializeDataFromPreferenceSource();
    }

    @Override
    public void onStart() {
        super.onStart();

        mCataloguePresenter.registerForEvents();
    }

    @Override
    public void onStop() {
        mCataloguePresenter.unregisterForEvents();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCataloguePresenter.destroyAllSubscriptions();
        mCataloguePresenter.releaseAllResources();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mCataloguePresenter.saveState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.catalogue, menu);
        final SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                InputMethodManager searchKeyboard = (InputMethodManager) AizobanApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                searchKeyboard.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mCataloguePresenter.onQueryTextChange(query);

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                mCataloguePresenter.onOptionFilter();
                return true;
            case R.id.action_to_top:
                mCataloguePresenter.onOptionToTop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // CatalogueView:

    @Override
    public void initializeToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_catalogue);
        }
    }

    @Override
    public void initializeGridView() {
        if (mGridView != null) {
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mCataloguePresenter.onMangaClick(position);
                }
            });
        }
    }

    @Override
    public void initializeEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_photo_library_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_catalogue);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.catalogue_instructions);
        }
    }

    @Override
    public void hideEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            mEmptyRelativeLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            mEmptyRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void scrollToTop() {
        if (mGridView != null) {
            mGridView.smoothScrollToPosition(0);
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // CatalogueMapper:

    @Override
    public void registerAdapter(BaseAdapter adapter) {
        if (mGridView != null) {
            mGridView.setAdapter(adapter);
        }
    }

    @Override
    public Parcelable getPositionState() {
        if (mGridView != null) {
            return mGridView.onSaveInstanceState();
        } else {
            return null;
        }
    }

    @Override
    public void setPositionState(Parcelable state) {
        if (mGridView != null) {
            mGridView.onRestoreInstanceState(state);
        }
    }
}
