package io.mapwize.uiapp;

import java.util.ArrayList;
import java.util.List;

import io.mapwize.mapwizeformapbox.model.MapwizeObject;
import io.mapwize.mapwizeformapbox.model.Place;
import io.mapwize.mapwizeformapbox.model.Venue;

public class SearchDataManager {

    List<MapwizeObject> venuesList;
    List<MapwizeObject> mainSearch;
    List<Place> mainFrom;

    public SearchDataManager() {
        venuesList = new ArrayList<>();
        mainSearch = new ArrayList<>();
        mainFrom = new ArrayList<>();
    }

    public List<MapwizeObject> getVenuesList() {
        return venuesList;
    }

    public void setVenuesList(List<MapwizeObject> venuesList) {
        this.venuesList = venuesList;
    }

    public List<MapwizeObject> getMainSearch() {
        return mainSearch;
    }

    public void setMainSearch(List<MapwizeObject> mainSearch) {
        this.mainSearch = mainSearch;
    }

    public List<Place> getMainFrom() {
        return mainFrom;
    }

    public void setMainFrom(List<Place> mainFrom) {
        this.mainFrom = mainFrom;
    }
}
