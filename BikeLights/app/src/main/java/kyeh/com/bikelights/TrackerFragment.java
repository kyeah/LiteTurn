package kyeh.com.bikelights;


import android.app.Fragment;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class TrackerFragment extends Fragment {

    private GoogleMap map;
    private LocationManager mLocationManager;
    private ArrayList<LatLng> trackPoints = new ArrayList<LatLng>();

    public TrackerFragment() {

    }

    public void setLocationManager(LocationManager lm) { mLocationManager = lm; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tracker, container, false);
        MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mapFragment.setRetainInstance(true);

        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);

        Polyline route = map.addPolyline(new PolylineOptions()
                .width(3)
                //.color(_pathColor)
                .geodesic(true));
                //.zIndex(z));

        route.setPoints(trackPoints);
        return rootView;
    }


    public void addTrackPoint(LatLng latLng) {
        trackPoints.add(latLng);
    }

    public void marker(LatLng position, final BitmapDescriptor bitmapDescriptor, final String title) {
        if (position == null) {
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    map.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(bitmapDescriptor)
                    .title(title));
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            }, null);
        } else {
            map.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(bitmapDescriptor)
                    .title(title));
        }
    }
}
