package mobi.sender.ui.window.map;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import mobi.sender.tool.Tool;

/**
 * Created by user on 24.04.15.
 */
public class MapParser {

    public static String loadAddressFromWeb(LatLng curPosition, String locale) {
        String description = null;
        try {
            String s = Tool.httpGet("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + curPosition.latitude + ","
                    + curPosition.longitude + "&sensor=true" + "&language=" + locale);
            JSONObject jsonObj = new JSONObject(s);
            String Status = jsonObj.getString("status");
            if (Status.equalsIgnoreCase("OK")) {
                JSONArray Results = jsonObj.getJSONArray("results");
                JSONObject zero = Results.getJSONObject(0);
                JSONArray address_components = zero.getJSONArray("address_components");

                for (int i = 0; i < address_components.length(); i++) {
                    JSONObject zero2 = address_components.getJSONObject(i);
                    String long_name = zero2.getString("long_name");
                    JSONArray mtypes = zero2.getJSONArray("types");
                    String Type = mtypes.getString(0);

                    if (TextUtils.isEmpty(long_name) == false || !long_name.equals(null) || long_name.length() > 0 || long_name != "") {
                        if (Type.equalsIgnoreCase("street_number")) {
                            description = long_name;
                        } else if (Type.equalsIgnoreCase("route")) {
                            description = long_name + ((description != null && description.length() > 0) ? " " + description : description);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return description;
    }

}
