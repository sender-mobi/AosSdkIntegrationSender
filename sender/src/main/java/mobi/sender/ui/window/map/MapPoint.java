package mobi.sender.ui.window.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import mobi.sender.tool.Tool;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 08.06.15
 * Time: 17:44
 */
public class MapPoint {

    private double lat, lon;
    private String label;

    public MapPoint(JSONObject jo) {
        if (jo.has("lt")) lat = jo.optDouble("lt");
        if (jo.has("lg")) lon = jo.optDouble("lg");
        if (jo.has("t")) label = jo.optString("t");
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getLabel() {
        return label;
    }

    public void setToMap(GoogleMap map) {
        LatLng ll = new LatLng(lat, lon);
        Marker marker = GoogleMapMarker.addMarker(map,
                new MarkerOptions()
                        .position(ll)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title(label)
        );
        Tool.log("marker " + ll + " added");
    }
}
