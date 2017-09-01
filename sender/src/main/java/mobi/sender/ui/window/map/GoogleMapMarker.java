package mobi.sender.ui.window.map;

import android.app.Activity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.tool.Tool;

public class GoogleMapMarker {
    private static List<LatLng> geoPositions = new ArrayList<LatLng>();

    public static Marker addMarker(GoogleMap map, MarkerOptions marker) {
        geoPositions.add(marker.getPosition());
        return map.addMarker(marker);
    }

    public static void clear() {
        geoPositions.clear();
    }

    public static void zoomCamera(final GoogleMap map, final Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (geoPositions.size() == 0) {
                    Tool.log("no geopositions of markers found!!!!");
                    return;
                }
                if (geoPositions.size() == 1) {
                    final CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(geoPositions.get(0))
                            .zoom(16)
                            .bearing(0)
                            .tilt(30)
                            .build();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    });
                    return;
                }
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng position : geoPositions) {
                    builder.include(position);
                }
                final LatLngBounds bounds = builder.build();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    }
                });
            }
        }).start();
    }
}
