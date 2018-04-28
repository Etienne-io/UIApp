package io.mapwize.uiapp;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.transition.TransitionInflater;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

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
    Scene mAScene;
    Scene mAnotherScene;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Mapbox.getInstance(this, "pk.mapwize");
        setContentView(R.layout.activity_map);
        //mapView = findViewById(R.id.mapboxMap);
        //mapView.onCreate(savedInstanceState);

        initMapUiSceneComponent();

    }

    private void initMapUiSceneComponent() {
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

    private void initSearchSceneComponent() {
        searchEditText = findViewById(R.id.search_input_text);
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.i("Debug", "Focus " + hasFocus);
            }
        });
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.clearFocus();
                searchEditText.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                searchToMapTransition();
            }
        });

        searchResultRecyclerView = findViewById(R.id.search_suggestions_list);
        searchResultAdapter = new SearchResultAdapter(this);
        searchResultRecyclerView.setAdapter(searchResultAdapter);
        List<Object> data = new ArrayList<>();
        data.add(new Object());data.add(new Object());data.add(new Object());data.add(new Object());
        searchResultAdapter.swapData(data);
    }

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
                initSearchSceneComponent();
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
                initMapUiSceneComponent();
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
