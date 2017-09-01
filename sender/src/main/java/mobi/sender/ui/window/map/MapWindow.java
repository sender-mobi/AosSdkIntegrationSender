package mobi.sender.ui.window.map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobi.sender.R;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.ui.ChatActivity;

/**
 * Created
 * by vp
 * on 09.12.14.
 */
public class MapWindow {

    public final static int MAP_JUST_SHOW = 0;
    public final static int MAP_WITH_SELECT = 1;
    public final static int MAP_FOR_SEND = 2;
    private static View mapView;
    private static float ZOOM = 15.0f;
    private Activity mAct;
    private OnSelectListener mListener;
    private AlertDialog dialog;
    private GoogleMap mMap;
    private LatLng myPosition;
    private LatLng curPosition;
    private String city;
    private String description;
    private Marker curMarker;
    private String locale;
    private JSONObject jo;
    private double lat;
    private double lon;
    private int whatMapIsShow = -1;
    private boolean i = true;

    public MapWindow(Activity mAct, JSONObject jo, OnSelectListener listener, int whatMapIsShow) {
        this.mAct = mAct;
        this.jo = jo;
        mListener = listener;
        locale = Storage.getInstance(mAct).getLocale();
        this.whatMapIsShow = whatMapIsShow;
    }

    public MapWindow(Activity act, double lat, double lon, int whatMapIsShow) {
        mAct = act;
        locale = Storage.getInstance(mAct).getLocale();
        this.lat = lat;
        this.lon = lon;
        this.whatMapIsShow = whatMapIsShow;
    }

    public MapWindow(ChatActivity act, OnSelectListener onSelectListener, int whatMapIsShow) {
        mAct = act;
        mListener = onSelectListener;
        this.whatMapIsShow = whatMapIsShow;
    }

    private boolean isEmpty(String s) {
        return s != null && s.length() > 0;
    }

    public void show() {
        if (mapView != null) {
            ViewGroup parent = (ViewGroup) mapView.getParent();
            if (parent != null)
                parent.removeView(mapView);
        }
        try {
            mapView = LayoutInflater.from(mAct).inflate(R.layout.ac_map, null);
        } catch (InflateException e) {
            e.printStackTrace();
        }

        ImageView ivPlace = (ImageView) mapView.findViewById(R.id.iv_place);
        View bottom_panel = mapView.findViewById(R.id.bottom_panel);
        ImageView ivBack = (ImageView) mapView.findViewById(R.id.back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        switch (whatMapIsShow) {
            case MAP_JUST_SHOW:
                ivPlace.setVisibility(View.GONE);
                bottom_panel.setVisibility(View.GONE);

                if (ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mAct, "Access to location service is not granted!", Toast.LENGTH_LONG).show();
                    return;
                }

                dialog = new AlertDialog.Builder(mAct).setView(mapView).create();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        GoogleMapMarker.clear();
                        dialog.dismiss();
                    }
                });
                dialog.show();

                ((MapFragment) mAct.getFragmentManager().findFragmentById(R.id.frMap)).getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        LatLng location = new LatLng(lat, lon);

                        mMap = googleMap;
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        mMap.getUiSettings().setZoomControlsEnabled(false);
                        mMap.getUiSettings().setCompassEnabled(false);
                        mMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name)));

                        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lat, lon));
                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

                        mMap.moveCamera(center);
                        mMap.animateCamera(zoom);

                        GoogleMapMarker.zoomCamera(mMap, mAct);
                    }
                });
                break;


            case MAP_WITH_SELECT:
                ivPlace.setVisibility(View.GONE);
                bottom_panel.setVisibility(View.GONE);

                if (ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mAct, "Access to location service is not granted!", Toast.LENGTH_LONG).show();
                    return;
                }

                dialog = new AlertDialog.Builder(mAct).setView(mapView).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        mListener.onCancel();
                    }
                }).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (curPosition != null) {
                            mListener.onSelect((isEmpty(description) ? "" : description) + (isEmpty(city) ? "," + city : ""), curPosition.latitude, curPosition.longitude);
                        }
                    }
                }).create();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        GoogleMapMarker.clear();
                        dialog.dismiss();
                    }
                });
                dialog.show();

                ((MapFragment) mAct.getFragmentManager()
                        .findFragmentById(R.id.frMap)).getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mMap = googleMap;
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        mMap.getUiSettings().setZoomControlsEnabled(false);
                        mMap.getUiSettings().setCompassEnabled(false);
                        if (ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            mMap.setMyLocationEnabled(true);
                        }
                        mMap.clear();
                        curPosition = myPosition = Storage.getInstance(mAct).getLocation();

                        if (myPosition.latitude == 0 && myPosition.longitude == 0) {
                            Toast.makeText(mAct, mAct.getString(R.string.map_determinate_location), Toast.LENGTH_LONG).show();
                            return;
                        }
                        setAndShowMarker();
                        if (jo.has("vars") && jo.has("vars_type") && "MPOINT".equalsIgnoreCase(jo.optString("vars_type"))) {
                            JSONArray arr = jo.optJSONArray("vars");
                            for (int i = 0; i < arr.length(); i++) {
                                new MapPoint(arr.optJSONObject(i)).setToMap(mMap);
                            }
                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    curPosition = marker.getPosition();
                                    description = marker.getTitle();
                                    return false;
                                }
                            });
                            mMap.setOnMapClickListener(
                                    new GoogleMap.OnMapClickListener() {
                                        @Override
                                        public void onMapClick(LatLng latLng) {
                                            curPosition = latLng;

                                            Geocoder geocoder = new Geocoder(mAct, new Locale(locale));
                                            List<Address> addresses;
                                            try {
                                                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                                if (addresses.size() > 0) {
                                                    Address address = addresses.get(0);
                                                    if (address.getMaxAddressLineIndex() > 0) {
                                                        description = address.getAddressLine(0);
                                                        if (address.getMaxAddressLineIndex() > 1) {
                                                            city = address.getAddressLine(1);
                                                        }
                                                    } else {
                                                        description = null;
                                                        city = null;
                                                    }
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                description = null;
                                                city = null;
                                            }
                                        }
                                    }
                            );
                        } else {
                            Geocoder geocoder = new Geocoder(mAct, new Locale(locale));
                            List<Address> addresses;
                            try {
                                addresses = geocoder.getFromLocation(curPosition.latitude, curPosition.longitude, 1);
                                if (addresses.size() > 0) {
                                    Address address = addresses.get(0);
                                    if (address.getMaxAddressLineIndex() > 0) {
                                        description = address.getAddressLine(0);
                                        if (address.getMaxAddressLineIndex() > 1) {
                                            city = address.getAddressLine(1);
                                        }
                                    } else {
                                        description = null;
                                        city = null;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                description = null;
                                city = null;
                            }

                            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                @Override
                                public View getInfoWindow(Marker marker) {
                                    View view = LayoutInflater.from(mAct).inflate(R.layout.marker_info_window, null);
                                    if (description == null || description.length() == 0) {
                                        curMarker.hideInfoWindow();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateInfoWindow(MapParser.loadAddressFromWeb(curPosition, locale));
                                            }
                                        }).start();
                                    } else {
                                        ((TextView) view.findViewById(R.id.tvAddress)).setText(description);
                                    }
                                    return view;
                                }

                                @Override
                                public View getInfoContents(Marker marker) {
                                    return null;
                                }
                            });

                            mMap.setOnMapClickListener(
                                    new GoogleMap.OnMapClickListener() {
                                        @Override
                                        public void onMapClick(LatLng latLng) {
                                            curPosition = latLng;
                                            if (curMarker != null) {
                                                curMarker.remove();
                                            }
                                            curMarker = GoogleMapMarker.addMarker(mMap, new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation)));
                                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                                    .target(curPosition)
                                                    .zoom(16)
                                                    .bearing(0)
                                                    .tilt(30)
                                                    .build();
                                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                            Geocoder geocoder = new Geocoder(mAct, new Locale(locale));
                                            List<Address> addresses;
                                            try {
                                                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                                if (addresses.size() > 0) {
                                                    Address address = addresses.get(0);
                                                    if (address.getMaxAddressLineIndex() > 0) {
                                                        description = address.getAddressLine(0);
                                                        if (address.getMaxAddressLineIndex() > 1) {
                                                            city = address.getAddressLine(1);
                                                        }
                                                    } else {
                                                        description = null;
                                                        city = null;
                                                    }
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                description = null;
                                                city = null;
                                            }
                                            if ((description == null || description.length() == 0)) {
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateInfoWindow(MapParser.loadAddressFromWeb(curPosition, locale));
                                                    }
                                                }).start();
                                            } else {
                                                curMarker.showInfoWindow();
                                            }
                                        }
                                    }
                            );

                        }
                        GoogleMapMarker.zoomCamera(mMap, mAct);
                    }
                });
                break;


            case MAP_FOR_SEND:
                ivPlace.setVisibility(View.VISIBLE);
                bottom_panel.setVisibility(View.VISIBLE);

                if (ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mAct, "Access to location service is not granted!", Toast.LENGTH_LONG).show();
                    return;
                }

                dialog = new AlertDialog.Builder(mAct, R.style.AppTheme).setView(mapView)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                mListener.onCancel();
                            }
                        })
                        .create();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        GoogleMapMarker.clear();
                        dialog.dismiss();
                    }
                });
                ImageView ivCurrentLocation = (ImageView) mapView.findViewById(R.id.current_location);
                ivCurrentLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Location location = mMap.getMyLocation();

                        if (location != null) {
                            LatLng myLocation = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            CameraPosition c = mMap.getCameraPosition();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, c.zoom));
                        }
                    }
                });
                ImageView ivSend = (ImageView) mapView.findViewById(R.id.send);
                ivSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Address> addresses = new ArrayList<>();
                        Geocoder geocoder = new Geocoder(mAct, Locale.getDefault());

                        double latitude = mMap.getCameraPosition().target.latitude;
                        double longitude = mMap.getCameraPosition().target.longitude;
                        Tool.log("map==> latitude: " + latitude + ", longitude: " + longitude);
                        try {
                            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String address = "";
                        if (addresses.size() != 0) {
                            address = addresses.get(0).getAddressLine(0);
                        }
                        mListener.onSelect(address, (float) latitude, (float) longitude);
                        dialog.dismiss();
                    }
                });
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                Point point = new Point();
                mAct.getWindowManager().getDefaultDisplay().getSize(point);
                dialog.getWindow().setLayout(point.x, point.y);
                dialog.show();

                ((MapFragment) mAct.getFragmentManager().findFragmentById(R.id.frMap)).getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mMap = googleMap;

                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);

                        if (ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                            @Override
                            public void onMyLocationChange(Location arg0) {
                                if (i) {
                                    moveMapToCenter((float) arg0.getLatitude(), (float) arg0.getLongitude());
                                    i = false;
                                }
                            }
                        });
                    }
                });
                break;
        }
    }

    private void updateInfoWindow(String address) {
        description = address;
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((description == null || description.length() == 0)) {
                    Toast.makeText(mAct, mAct.getString(R.string.map_disable_location_name), Toast.LENGTH_LONG).show();
                } else {
                    curMarker.showInfoWindow();
                }
            }
        });
    }

    private void setAndShowMarker() {
        if (myPosition.latitude == 0 && myPosition.longitude == 0) {
            Toast.makeText(mAct, mAct.getString(R.string.map_determinate_location), Toast.LENGTH_LONG).show();
            return;
        }
        curPosition = myPosition;
        if (curMarker != null) {
            curMarker.remove();
        }
        curMarker = GoogleMapMarker.addMarker(mMap, new MarkerOptions().position(myPosition).icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation)));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myPosition)
                .zoom(16)
                .bearing(0)
                .tilt(30)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        curMarker.showInfoWindow();
    }

    private Location getCurrentPosition() {
        LocationManager locationManager = (LocationManager) mAct.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(mAct,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mAct, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return locationManager.getLastKnownLocation(provider);
    }

    private void moveMapToCenter(float lat, float lng) {
        LatLng myLocation = new LatLng(lat, lng);
        CameraUpdate c = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(myLocation)
                .zoom(ZOOM)
                .tilt(0)
                .build());
        mMap.moveCamera(c);
    }

    public interface OnSelectListener {
        void onSelect(String address, double lat, double lon);

        void onCancel();
    }

}
