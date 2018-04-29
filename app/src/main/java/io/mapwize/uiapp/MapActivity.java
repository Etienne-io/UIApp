package io.mapwize.uiapp;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.transition.TransitionInflater;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapbox.mapboxsdk.maps.MapView;

import java.util.List;

import io.mapwize.mapwizeformapbox.api.Api;
import io.mapwize.mapwizeformapbox.api.ApiCallback;
import io.mapwize.mapwizeformapbox.api.SearchParams;
import io.mapwize.mapwizeformapbox.model.MapwizeObject;
import io.mapwize.mapwizeformapbox.model.Translation;
import io.mapwize.mapwizeformapbox.model.Venue;

public class MapActivity extends AppCompatActivity implements SearchResultAdapter.OnItemClickListener{

    private MapView mapView;
    private ConstraintLayout settingsLayout;
    private ImageButton settingsButton;
    private ViewGroup uiSceneRoot;
    private TextView searchTextView;
    private EditText searchEditText;
    private ImageButton menuButton;
    private Transition mapToSearchTransition;
    private Transition searchToMapTransition;
    private RecyclerView searchResultRecyclerView;
    private SearchResultAdapter searchResultAdapter;

    private SearchDataManager searchDataManager;
    Scene mAScene;
    Scene mAnotherScene;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Mapbox.getInstance(this, "pk.mapwize");
        setContentView(R.layout.activity_map);
        //mapView = findViewById(R.id.mapboxMap);
        //mapView.onCreate(savedInstanceState);

        setupMapUiSceneComponent();
        initSearchDataManager();
    }

    private void initSearchDataManager() {
        searchDataManager = new SearchDataManager();
        SearchParams params = new SearchParams.Builder()
                .setObjectClass(new String[]{"venue"})
                .build();
        Api.search(params, new ApiCallback<List<MapwizeObject>>() {
            @Override
            public void onSuccess(List<MapwizeObject> mapwizeObjects) {
                Log.i("Debug", "Ended");
                searchDataManager.setVenuesList(mapwizeObjects);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.i("Debug", "Ended " + throwable.getMessage());
            }
        });
    }

    /*
    Map Scene
     */
    private void setupMapUiSceneComponent() {
        settingsLayout = findViewById(R.id.settingsLayout);
        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSettingsClick(!settingExpanded);
            }
        });

        searchTextView = findViewById(R.id.search_text_view);
        searchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapToSearchTransition();
            }
        });

        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Debug", "Menu button click");
            }
        });
    }


    /*
    Search Scene
     */
    private void setupSearchSceneComponent() {
        searchEditText = findViewById(R.id.search_input_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    searchResultAdapter.swapData(searchDataManager.getVenuesList());
                } else {
                    SearchParams params = new SearchParams.Builder()
                            .setObjectClass(new String[]{"venue"})
                            .setQuery(s.toString())
                            .build();
                    Api.search(params, new ApiCallback<List<MapwizeObject>>() {
                        @Override
                        public void onSuccess(final List<MapwizeObject> mapwizeObjects) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    searchResultAdapter.swapData(mapwizeObjects);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                        }
                    });
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchToMapTransition();
            }
        });

        searchResultRecyclerView = findViewById(R.id.search_suggestions_list);
        searchResultAdapter = new SearchResultAdapter(this);
        searchResultRecyclerView.setAdapter(searchResultAdapter);
        searchResultAdapter.swapData(searchDataManager.getVenuesList());
        searchResultAdapter.setListener(this);
    }

    @Override
    public void onItemClick(Object item) {
        if (item instanceof Venue) {
            Venue venue = (Venue) item;
            Log.i("Debug", "Venue : " + venue.getName());
            searchToMapTransition();
        }
    }

    /*
    Transitions
     */
    private void mapToSearchTransition() {
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        mAScene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_map_scene, this);
        mAnotherScene =
                Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_search_scene, this);
        mapToSearchTransition = TransitionInflater.from(this).inflateTransition(R.transition.map_to_search_transition);
        mapToSearchTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                setupSearchSceneComponent();
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionPause(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionResume(@NonNull Transition transition) {

            }
        });
        TransitionManager.go(mAnotherScene, mapToSearchTransition);
    }

    private void searchToMapTransition() {
        searchEditText.clearFocus();
        searchEditText.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        uiSceneRoot = findViewById(R.id.ui_scene_root);
        mAScene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_map_scene, this);
        mAnotherScene =
                Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_search_scene, this);
        searchToMapTransition = TransitionInflater.from(this).inflateTransition(R.transition.map_to_search_transition);
        searchToMapTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                setupMapUiSceneComponent();
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionPause(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionResume(@NonNull Transition transition) {

            }
        });
        TransitionManager.go(mAScene, searchToMapTransition);
    }


    private boolean settingExpanded = false;
    private void onSettingsClick(boolean expand) {
        int from, to;
        if (expand) {
            from = convertDpToPixel(40, this);
            to = convertDpToPixel(120, this);
        } else {
            from = convertDpToPixel(120, this);
            to = convertDpToPixel(40, this);
        }
        settingExpanded = expand;
        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(from, to)
                .setDuration(300);

        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                settingsLayout.getLayoutParams().width = value.intValue();
                settingsLayout.requestLayout();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(slideAnimator);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int)(dp * (metrics.densityDpi / 160f));
    }



    /*
    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    */

}
