package xun.loc.feature;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import xun.loc.feature.db.entity.Location;

public class TrackOverlay {

    private Polyline polyline;

    public TrackOverlay(AMap aMap) {
        this(aMap, new PolylineOptions()
                .setCustomTexture(BitmapDescriptorFactory.fromAsset("tracelinetexture.png"))
                .width(30));
    }

    public TrackOverlay(AMap aMap, PolylineOptions options) {
        polyline = aMap.addPolyline(options);
    }

    public TrackOverlay addLocation(Location location) {
        List<LatLng> points = new ArrayList<>(polyline.getPoints());
        points.add(cnvLocation(location));
        polyline.setPoints(points);
        return this;
    }

    public TrackOverlay setLocations(List<Location> locations) {
        List<LatLng> points = new ArrayList<>(locations.size());
        for (Location location : locations) {
            points.add(cnvLocation(location));
        }
        polyline.setPoints(points);
        return this;
    }

    public void remove() {
        polyline.remove();
    }

    public LatLngBounds bounds() {
        List<LatLng> points = polyline.getPoints();
        if (points == null || points.size() < 2) return null;
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        return builder.build();
    }

    static LatLng cnvLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude(), false);
    }
}
