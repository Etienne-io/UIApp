package io.mapwize.uiapp;

import java.util.List;

import io.mapwize.mapwizeformapbox.model.MapwizeObject;
import io.mapwize.mapwizeformapbox.model.Place;
import io.mapwize.mapwizeformapbox.model.Venue;

public class SearchDataManager {

    List<MapwizeObject> venuesList;
    List<MapwizeObject> mainSearch;
    List<MapwizeObject> mainFrom;

    public SearchDataManager() {
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

    public List<MapwizeObject> getMainFrom() {
        return mainFrom;
    }

    public void setMainFrom(List<MapwizeObject> mainFrom) {
        this.mainFrom = mainFrom;
    }
}
