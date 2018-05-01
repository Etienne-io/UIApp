package io.mapwize.uiapp;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Camera;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.transition.AutoTransition;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.transition.TransitionInflater;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.indoorlocation.core.IndoorLocation;
import io.indoorlocation.core.IndoorLocationProviderListener;
import io.mapwize.mapwizeformapbox.DirectionOptions;
import io.mapwize.mapwizeformapbox.FollowUserMode;
import io.mapwize.mapwizeformapbox.MapOptions;
import io.mapwize.mapwizeformapbox.MapwizePlugin;
import io.mapwize.mapwizeformapbox.UISettings;
import io.mapwize.mapwizeformapbox.api.Api;
import io.mapwize.mapwizeformapbox.api.ApiCallback;
import io.mapwize.mapwizeformapbox.api.ApiFilter;
import io.mapwize.mapwizeformapbox.api.SearchParams;
import io.mapwize.mapwizeformapbox.model.Direction;
import io.mapwize.mapwizeformapbox.model.DirectionPoint;
import io.mapwize.mapwizeformapbox.model.LatLngFloor;
import io.mapwize.mapwizeformapbox.model.MapwizeObject;
import io.mapwize.mapwizeformapbox.model.Place;
import io.mapwize.mapwizeformapbox.model.Translation;
import io.mapwize.mapwizeformapbox.model.Universe;
import io.mapwize.mapwizeformapbox.model.Venue;

public class MapActivity extends AppCompatActivity {

    // Views and Layouts
    private MapView mapView;
    private MapwizePlugin mapwizePlugin;
    private ConstraintLayout settingsLayout;
    private ImageButton settingsButton;
    private ImageButton universesButton;
    private ImageButton languagesButton;
    private ViewGroup uiSceneRoot;
    private TextView searchTextView;
    private EditText searchEditText;
    private ImageButton menuButton;
    private ImageView contentIconImageView;
    private TextView contentTitleTextView;
    private TextView contentSubTitleTextView;
    private TextView contentFloorTextView;
    private RecyclerView searchResultRecyclerView;
    private ImageButton locationButton;
    private TextView directionButton;
    private EditText fromDirectionEditText;
    private EditText toDirectionEditText;
    private ImageView fromDirectionImage;
    private ImageView toDirectionImage;
    private ImageView swapDirectionButton;
    private ImageView accessibleDirectionButton;


    private SearchResultAdapter searchResultAdapter;
    private SearchDataManager searchDataManager;

    // Location provider
    private FusedGpsIndoorLocationProvider locationProvider;
    private boolean locationProviderActivated = false;

    // Direction
    private MapwizeObject fromDirectionPoint;
    private MapwizeObject toDirectionPoint;
    private boolean isAccessible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.mapwize");
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapboxMap);
        mapView.onCreate(savedInstanceState);

        MapOptions mapOptions = new MapOptions.Builder()
                .language("en")
                .build();
        UISettings uiSettings = new UISettings.Builder(this)
                .showUserPositionControl(false)
                .showFloorControl(false)
                .mapwizeCompassEnabled(false)
                .logoEnabled(false)
                .build();
        mapwizePlugin = new MapwizePlugin(mapView, mapOptions, uiSettings);

        initSettingsLayout();
        setupMapwizeListeners();
        setupMapUiSceneComponent();
        initSearchDataManager();
        initLocationManager();
    }

    private void initSearchDataManager() {
        searchDataManager = new SearchDataManager();
        SearchParams params = new SearchParams.Builder()
                .setObjectClass(new String[]{"venue"})
                .build();
        Api.search(params, new ApiCallback<List<MapwizeObject>>() {
            @Override
            public void onSuccess(List<MapwizeObject> mapwizeObjects) {
                searchDataManager.setVenuesList(mapwizeObjects);
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    /*
    Map Scene
     */
    private void setupMapUiSceneComponent() {

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

        locationButton = findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapwizePlugin.getFollowUserMode() == FollowUserMode.NONE) {
                    mapwizePlugin.setFollowUserMode(FollowUserMode.FOLLOW_USER);
                }
                else if (mapwizePlugin.getFollowUserMode() == FollowUserMode.FOLLOW_USER) {
                    mapwizePlugin.setFollowUserMode(FollowUserMode.FOLLOW_USER_AND_HEADING);
                }
                else {
                    mapwizePlugin.setFollowUserMode(FollowUserMode.FOLLOW_USER);
                }
            }
        });
        if (mapwizePlugin.getFollowUserMode() == FollowUserMode.NONE) {
            locationButton.setImageResource(R.drawable.ic_my_location_black_24dp);
            locationButton.setColorFilter(Color.BLACK);
        }
        else if (mapwizePlugin.getFollowUserMode() == FollowUserMode.FOLLOW_USER) {
            locationButton.setImageResource(R.drawable.ic_my_location_black_24dp);
            locationButton.setColorFilter(Color.argb(255, 197, 21, 134));
        }
        else {
            locationButton.setImageResource(R.drawable.ic_explore_black_24dp);
            locationButton.setColorFilter(Color.argb(255, 197, 21, 134));
        }

        mapwizePlugin.setOnPlaceClickListener(new MapwizePlugin.OnPlaceClickListener() {
            @Override
            public boolean onPlaceClick(Place place) {
                selectContent(place);
                return true;
            }
        });

        mapwizePlugin.setOnMapClickListener(new MapwizePlugin.OnMapClickListener() {
            @Override
            public void onMapClick(LatLngFloor latLngFloor) {
                if (selectedContent != null) {
                    unselectContent();
                }
            }
        });
    }


    /*
    Search Scene
     */
    private void setupSearchSceneComponent() {
        searchEditText = findViewById(R.id.search_input_text);

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

        if (mapwizePlugin.getVenue() == null) {
            initOutOfVenueSearch();
        }
        else {
            initInVenueSearch();
        }
    }

    private void initOutOfVenueSearch() {
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
        searchResultAdapter.swapData(searchDataManager.getVenuesList());
        searchResultAdapter.setListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object item) {
                Venue venue = (Venue) item;
                mapwizePlugin.centerOnVenue(venue);
                searchToMapTransition();
            }
        });
    }

    private void initInVenueSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    searchResultAdapter.swapData(searchDataManager.getMainSearch());
                } else {
                    SearchParams params = new SearchParams.Builder()
                            .setObjectClass(new String[]{"place", "placeList"})
                            .setVenueId(mapwizePlugin.getVenue().getId())
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
        searchResultAdapter.swapData(searchDataManager.getMainSearch());
        searchResultAdapter.setListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object item) {
                if (item instanceof Place) {
                    Place place = (Place) item;
                    centerOnPlace(place);
                    searchToSelectContentTransition(place);
                }
            }
        });
    }

    /*
    Transitions
     */
    private void mapToSearchTransition() {
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_search_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.map_to_search_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                uiSceneRoot.setBackgroundColor(Color.argb(255, 238, 238, 238));
                setupSearchSceneComponent();
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {

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
        TransitionManager.go(scene, transition);
    }

    private void searchToMapTransition() {
        searchEditText.clearFocus();
        searchEditText.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_map_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.map_to_search_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                setupMapUiSceneComponent();
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                uiSceneRoot.setBackgroundColor(Color.TRANSPARENT);
                if (mapwizePlugin.getVenue() != null) {
                    settingsLayout.setVisibility(View.VISIBLE);
                }
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
        TransitionManager.go(scene, transition);
    }

    private void mapToSelectContentTransition(final MapwizeObject mapwizeObject) {
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_content_selected_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.map_to_select_content_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                contentIconImageView = findViewById(R.id.content_icon);
                contentTitleTextView = findViewById(R.id.content_title);
                contentSubTitleTextView = findViewById(R.id.content_subtitle);
                contentFloorTextView = findViewById(R.id.content_floor);
                setupSelectContentView(mapwizeObject);
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {

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
        TransitionManager.go(scene, transition);
    }

    private void searchToSelectContentTransition(final MapwizeObject mapwizeObject) {
        searchEditText.clearFocus();
        searchEditText.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_content_selected_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.search_to_select_content_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                uiSceneRoot.setBackgroundColor(Color.TRANSPARENT);
                contentIconImageView = findViewById(R.id.content_icon);
                contentTitleTextView = findViewById(R.id.content_title);
                contentSubTitleTextView = findViewById(R.id.content_subtitle);
                contentFloorTextView = findViewById(R.id.content_floor);

                setupSelectContentView(mapwizeObject);
                selectedContent = mapwizeObject;
                if (mapwizeObject instanceof Place) {
                    Place place = (Place) mapwizeObject;
                    mapwizePlugin.addMarker(place);
                }
                mapwizePlugin.setOnPlaceClickListener(new MapwizePlugin.OnPlaceClickListener() {
                    @Override
                    public boolean onPlaceClick(Place place) {
                        selectContent(place);
                        return true;
                    }
                });

                mapwizePlugin.setOnMapClickListener(new MapwizePlugin.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLngFloor latLngFloor) {
                        if (selectedContent != null) {
                            unselectContent();
                        }
                    }
                });
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
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
        TransitionManager.go(scene, transition);
    }

    private void selectContentToMapTransition() {
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_map_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.select_content_to_map_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                setupMapUiSceneComponent();
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {

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
        TransitionManager.go(scene, transition);
    }

    private void selectContentToDirectionTransition() {
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_direction_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.select_content_to_direction_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                setupDirectionSceneComponents();
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                if (fromDirectionPoint == null) {
                    directionToSearchDirectionTransition(true);
                }
                else if (toDirectionPoint == null) {
                    directionToSearchDirectionTransition(false);
                }
                else {
                    startDirection(fromDirectionPoint, toDirectionPoint, true);
                }
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
        TransitionManager.go(scene, transition);
    }

    private void setupDirectionSceneComponents() {
        fromDirectionEditText = findViewById(R.id.from_input_text);
        toDirectionEditText = findViewById(R.id.to_input_text);
        fromDirectionEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                directionToSearchDirectionTransition(true);
            }
        });
        toDirectionEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                directionToSearchDirectionTransition(false);
            }
        });

        toDirectionEditText.setText(toDirectionPoint.getTranslation("en").getTitle());
        if (fromDirectionPoint != null) {
            fromDirectionEditText.setText(fromDirectionPoint.getTranslation("en").getTitle());
        }

        directionButton = findViewById(R.id.direction_button);
        directionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapwizePlugin.removeDirection();
                if (toDirectionPoint != null) {
                    selectedContent = toDirectionPoint;
                    directionToSelectContentTransition();
                }
                else {
                    directionToMapTransition();
                }
            }
        });

        swapDirectionButton = findViewById(R.id.swap_icon);
        swapDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapwizeObject tmp = fromDirectionPoint;
                fromDirectionPoint = toDirectionPoint;
                toDirectionPoint = tmp;
                if (fromDirectionPoint != null) {
                    fromDirectionEditText.setText(fromDirectionPoint.getTranslation("en").getTitle());
                }
                else {
                    fromDirectionEditText.setText("");
                }
                if (toDirectionPoint != null) {
                    toDirectionEditText.setText(toDirectionPoint.getTranslation("en").getTitle());
                }
                else {
                    toDirectionEditText.setText("");
                }
                if (fromDirectionPoint != null && toDirectionPoint != null) {
                    startDirection(fromDirectionPoint, toDirectionPoint, isAccessible);
                }
            }
        });

        accessibleDirectionButton = findViewById(R.id.accessibility_icon);
        if (isAccessible) {
            accessibleDirectionButton.setColorFilter(Color.argb(255, 197, 21, 134));
        }
        else {
            accessibleDirectionButton.setColorFilter(Color.TRANSPARENT);
        }
        accessibleDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAccessible = !isAccessible;
                if (isAccessible) {
                    accessibleDirectionButton.setColorFilter(Color.argb(255, 197, 21, 134));
                }
                else {
                    accessibleDirectionButton.setColorFilter(Color.TRANSPARENT);
                }
                if (fromDirectionPoint != null && toDirectionPoint != null) {
                    startDirection(fromDirectionPoint, toDirectionPoint, isAccessible);
                }
            }
        });

        mapwizePlugin.setOnPlaceClickListener(null);

        mapwizePlugin.setOnMapClickListener(null);
    }

    private void directionToSearchDirectionTransition(final boolean isFrom) {
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_search_direction_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.direction_to_search_direction_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                uiSceneRoot.setBackgroundColor(Color.argb(255, 238, 238, 238));
                setupDirectionSearchSceneComponent(isFrom);

            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {

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
        TransitionManager.go(scene, transition);
    }

    private void searchDirectionToDirectionTransition() {
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_direction_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.direction_to_search_direction_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                setupDirectionSceneComponents();
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                uiSceneRoot.setBackgroundColor(Color.TRANSPARENT);
                if (fromDirectionPoint != null && toDirectionPoint != null) {
                    startDirection(fromDirectionPoint, toDirectionPoint, isAccessible);
                }
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
        TransitionManager.go(scene, transition);
    }

    private void directionToMapTransition() {
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_map_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.select_content_to_direction_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                setupMapUiSceneComponent();
                fromDirectionPoint = null;
                toDirectionPoint = null;
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {

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
        TransitionManager.go(scene, transition);
    }

    private void directionToSelectContentTransition() {
        uiSceneRoot = findViewById(R.id.ui_scene_root);
        Scene scene = Scene.getSceneForLayout(uiSceneRoot, R.layout.activity_content_selected_scene, this);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.select_content_to_direction_transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                contentIconImageView = findViewById(R.id.content_icon);
                contentTitleTextView = findViewById(R.id.content_title);
                contentSubTitleTextView = findViewById(R.id.content_subtitle);
                contentFloorTextView = findViewById(R.id.content_floor);
                //setupSelectContentView(selectedContent);
                if (selectedContent != null) {
                    selectContent(selectedContent);
                }
                fromDirectionPoint = null;
                toDirectionPoint = null;
                mapwizePlugin.setOnPlaceClickListener(new MapwizePlugin.OnPlaceClickListener() {
                    @Override
                    public boolean onPlaceClick(Place place) {
                        selectContent(place);
                        return true;
                    }
                });

                mapwizePlugin.setOnMapClickListener(new MapwizePlugin.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLngFloor latLngFloor) {
                        if (selectedContent != null) {
                            unselectContent();
                        }
                    }
                });
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {

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
        TransitionManager.go(scene, transition);
    }

    private void setupDirectionSearchSceneComponent(boolean isFrom) {
        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDirectionToDirectionTransition();
            }
        });

        if (isFrom) {
            initFromSearch();
        }
        else {
            initToSearch();
        }
    }

    private void initFromSearch() {
        fromDirectionEditText = findViewById(R.id.from_input_text);
        fromDirectionEditText.requestFocus();
        fromDirectionEditText.setText("");
        fromDirectionImage = findViewById(R.id.from_icon);
        fromDirectionImage.setColorFilter(Color.argb(255, 197, 21, 134));
        toDirectionEditText = findViewById(R.id.to_input_text);
        toDirectionEditText.setClickable(false);
        if (toDirectionPoint != null) {
            toDirectionEditText.setText(toDirectionPoint.getTranslation("en").getTitle());
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(fromDirectionEditText, InputMethodManager.SHOW_IMPLICIT);

        searchResultRecyclerView = findViewById(R.id.search_suggestions_list);
        searchResultAdapter = new SearchResultAdapter(this);
        searchResultRecyclerView.setAdapter(searchResultAdapter);
        fromDirectionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    searchResultAdapter.swapData(searchDataManager.getMainFrom());
                } else {
                    SearchParams params = new SearchParams.Builder()
                            .setObjectClass(new String[]{"place"})
                            .setQuery(s.toString())
                            .setVenueId(mapwizePlugin.getVenue().getId())
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
        searchResultAdapter.swapData(searchDataManager.getMainFrom());
        searchResultAdapter.setListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object item) {
                fromDirectionPoint = (MapwizeObject) item;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(fromDirectionEditText.getWindowToken(), 0);
                searchDirectionToDirectionTransition();
            }
        });
    }

    private void initToSearch() {
        toDirectionEditText = findViewById(R.id.to_input_text);
        toDirectionEditText.requestFocus();
        toDirectionEditText.setText("");
        toDirectionImage = findViewById(R.id.to_icon);
        toDirectionImage.setColorFilter(Color.argb(255, 197, 21, 134));
        fromDirectionEditText = findViewById(R.id.from_input_text);
        fromDirectionEditText.setClickable(false);
        if (fromDirectionPoint != null) {
            fromDirectionEditText.setText(fromDirectionPoint.getTranslation("en").getTitle());
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(toDirectionEditText, InputMethodManager.SHOW_IMPLICIT);

        searchResultRecyclerView = findViewById(R.id.search_suggestions_list);
        searchResultAdapter = new SearchResultAdapter(this);
        searchResultRecyclerView.setAdapter(searchResultAdapter);
        toDirectionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    searchResultAdapter.swapData(searchDataManager.getMainSearch());
                } else {
                    SearchParams params = new SearchParams.Builder()
                            .setObjectClass(new String[]{"place", "placeList"})
                            .setQuery(s.toString())
                            .setVenueId(mapwizePlugin.getVenue().getId())
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
        searchResultAdapter.swapData(searchDataManager.getMainSearch());
        searchResultAdapter.setListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object item) {
                toDirectionPoint = (MapwizeObject) item;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(toDirectionEditText.getWindowToken(), 0);
                searchDirectionToDirectionTransition();
            }
        });
    }

    private void startDirection(MapwizeObject from, MapwizeObject to, boolean isAccessible) {
        mapwizePlugin.removeMarkers();
        DirectionPoint f = (DirectionPoint) from;
        DirectionPoint t = (DirectionPoint) to;
        Api.getDirection(f, t, isAccessible, new ApiCallback<Direction>() {
            @Override
            public void onSuccess(Direction direction) {
                DirectionOptions.Builder optsBuilder = new DirectionOptions.Builder();
                mapwizePlugin.setDirection(direction, optsBuilder.build());
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }

    private boolean settingExpanded = false;
    private void onSettingsClick(boolean expand) {
        int from, to;
        if (expand) {
            from = convertDpToPixel(40, this);
            to = convertDpToPixel(124, this);
        } else {
            from = convertDpToPixel(124, this);
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



    private MapwizeObject selectedContent = null;
    private void selectContent(MapwizeObject mapwizeObject) {
        mapwizePlugin.removeMarkers();
        if (selectedContent == null) {
            mapToSelectContentTransition(mapwizeObject);
        }
        else {
            setupSelectContentView(mapwizeObject);
        }
        selectedContent = mapwizeObject;

        if (mapwizeObject instanceof Place) {
            Place place = (Place) mapwizeObject;
            mapwizePlugin.addMarker(place);
        }
    }

    private void unselectContent() {
        selectedContent = null;
        selectContentToMapTransition();
        mapwizePlugin.removeMarkers();
    }

    private void setupSelectContentView(MapwizeObject mapwizeObject) {
        if (mapwizeObject instanceof Place) {
            Place place = (Place) mapwizeObject;
            String title = place.getTranslation("en").getTitle();
            String subtitle = place.getTranslation("en").getSubtitle();
            if (title.length() > 0) {
                contentTitleTextView.setText(title);
                contentTitleTextView.setVisibility(View.VISIBLE);
            } else {
                contentTitleTextView.setVisibility(View.GONE);
            }
            if (subtitle.length() > 0) {
                contentSubTitleTextView.setText(subtitle);
                contentSubTitleTextView.setVisibility(View.VISIBLE);
            } else {
                contentSubTitleTextView.setVisibility(View.GONE);
            }
            Picasso.get().load(place.getIcon()).into(contentIconImageView);
            NumberFormat nf = new DecimalFormat("###.###");
            contentFloorTextView.setText(String.format("Floor %1$s", nf.format(place.getFloor())));
        }

        directionButton = findViewById(R.id.direction_button);
        directionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDirectionPoint = selectedContent;
                selectContentToDirectionTransition();
            }
        });
    }

    /*
    Settings button
     */
    private void initSettingsLayout() {
        settingsLayout = findViewById(R.id.settings_bar_layout);
        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSettingsClick(!settingExpanded);
            }
        });
        universesButton = findViewById(R.id.universesButton);
        universesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSettingsClick(!settingExpanded);
                displayUniversesSelector();
            }
        });
        languagesButton = findViewById(R.id.languagesButton);
        languagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSettingsClick(!settingExpanded);
                displayLanguagesSelector();
            }
        });
    }

    private void displayUniversesSelector() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.universes_alert, null);

        RecyclerView universesList = dialogView.findViewById(R.id.universes_list);
        UniversesAdapter universesAdapter = new UniversesAdapter(this);
        universesList.setAdapter(universesAdapter);
        universesAdapter.swapData(mapwizePlugin.getVenue().getUniverses());
        universesAdapter.setListener(new UniversesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Universe item) {
                mapwizePlugin.setUniverseForVenue(item, mapwizePlugin.getVenue());
                alertDialog.dismiss();
            }
        });
        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    AlertDialog alertDialog;
    private void displayLanguagesSelector() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.languages_alert, null);

        RecyclerView languagesList = dialogView.findViewById(R.id.languages_list);
        LanguagesAdapter languagesAdapter = new LanguagesAdapter(this);
        languagesList.setAdapter(languagesAdapter);
        languagesAdapter.swapData(mapwizePlugin.getVenue().getSupportedLanguages());
        languagesAdapter.setListener(new LanguagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Locale item) {
                mapwizePlugin.setLanguageForVenue(item.getLanguage(), mapwizePlugin.getVenue());
                alertDialog.dismiss();
            }
        });
        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /*
    Mapwize listener
     */
    private void setupMapwizeListeners() {
        mapwizePlugin.setOnDidLoadListener(new MapwizePlugin.OnDidLoadListener() {
            @Override
            public void didLoad(MapwizePlugin mapwizePlugin) {
                mapwizePlugin.setLocationProvider(locationProvider);
            }
        });

        mapwizePlugin.setOnVenueEnterListener(new MapwizePlugin.OnVenueEnterListener() {
            @Override
            public void onVenueEnter(Venue venue) {
                settingsLayout.setAlpha(0.0f);
                settingsLayout.setVisibility(View.VISIBLE);
                settingsLayout.animate().alpha(1.0f).setDuration(300).setListener(null);
            }

            @Override
            public void willEnterInVenue(Venue venue) {

                searchDataManager.setMainSearch(new ArrayList<MapwizeObject>());
                searchDataManager.setMainFrom(new ArrayList<Place>());
                Api.getMainSearchesForVenue(venue.getId(), new ApiCallback<List<MapwizeObject>>() {
                    @Override
                    public void onSuccess(List<MapwizeObject> mapwizeObjects) {
                        searchDataManager.setMainSearch(mapwizeObjects);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                });
                Api.getMainFromsForVenue(venue.getId(), new ApiCallback<List<Place>>() {
                    @Override
                    public void onSuccess(List<Place> mapwizeObjects) {
                        searchDataManager.setMainFrom(mapwizeObjects);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                });
            }
        });

        mapwizePlugin.setOnVenueExitListener(new MapwizePlugin.OnVenueExitListener() {
            @Override
            public void onVenueExit(Venue venue) {

                if (selectedContent != null) {
                    unselectContent();
                }

                settingsLayout.animate().alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        settingsLayout.setVisibility(View.GONE);
                    }
                });
            }
        });

        mapwizePlugin.setOnVenueClickListener(new MapwizePlugin.OnVenueClickListener() {
            @Override
            public boolean onVenueClick(Venue venue) {
                mapwizePlugin.centerOnVenue(venue);
                return false;
            }
        });

        mapwizePlugin.setOnFollowUserModeChangeListener(new MapwizePlugin.OnFollowUserModeChange() {
            @Override
            public void followUserModeChange(int i) {
                if (i == FollowUserMode.NONE) {
                    locationButton.setImageResource(R.drawable.ic_my_location_black_24dp);
                    locationButton.setColorFilter(Color.BLACK);
                }
                else if (i == FollowUserMode.FOLLOW_USER) {
                    locationButton.setImageResource(R.drawable.ic_my_location_black_24dp);
                    locationButton.setColorFilter(Color.argb(255, 197, 21, 134));
                }
                else {
                    locationButton.setImageResource(R.drawable.ic_explore_black_24dp);
                    locationButton.setColorFilter(Color.argb(255, 197, 21, 134));
                }
            }
        });
    }

    /*
    Camera method
     */
    private void centerOnPlace(final Place place) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                CameraPosition cp = new CameraPosition.Builder().target(place.getCenter()).zoom(18).build();
                mapboxMap.setCameraPosition(cp);
                if (!mapwizePlugin.getFloor().equals(place.getFloor())) {
                    mapwizePlugin.setFloor(place.getFloor());
                }
            }
        });

    }

    /*
    Location Manager
     */
    private void initLocationManager() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            setupLocationProvider();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupLocationProvider();
                }
            }
        }
    }

    private void setupLocationProvider() {
        locationProvider = new FusedGpsIndoorLocationProvider(MapActivity.this);
        locationProvider.start();
        locationProvider.addListener(new IndoorLocationProviderListener() {
            @Override
            public void onProviderStarted() {

            }

            @Override
            public void onProviderStopped() {

            }

            @Override
            public void onProviderError(Error error) {

            }

            @Override
            public void onIndoorLocationChange(IndoorLocation indoorLocation) {
                if (!locationProviderActivated) {
                    locationProviderActivated = true;
                    final CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(indoorLocation.getLatitude(), indoorLocation.getLongitude()), 16);
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapboxMap mapboxMap) {
                            mapboxMap.animateCamera(cu, 3000);
                        }
                    });
                    locationButton.setImageResource(R.drawable.ic_my_location_black_24dp);
                    locationButton.setColorFilter(Color.BLACK);
                }
            }
        });
    }

    /*
    Lifecycle
     */
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
        if (mapwizePlugin != null) {
            mapwizePlugin.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (mapwizePlugin != null) {
            mapwizePlugin.onPause();
        }
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


    /*
    Helper
     */
    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int)(dp * (metrics.densityDpi / 160f));
    }
}
