package kyeh.com.bikelights;


import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class TrackerFragment extends Fragment {

    private static final String TAG = "TrackerFragment";

    private View root;
    private GoogleMap map;
    private ArrayList<LatLng> trackPoints = new ArrayList<LatLng>();
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private GoogleApiClient mGoogleApiClient;
    private Polyline route;

    public TrackerFragment() {}

    public void setGoogleApiClient(GoogleApiClient client) { mGoogleApiClient = client; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (root == null) {
            root = inflater.inflate(R.layout.fragment_tracker, container, false);
            MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            mapFragment.setRetainInstance(true);
            map = mapFragment.getMap();
            map.setMyLocationEnabled(true);

            route = map.addPolyline(new PolylineOptions()
                    .width(3)
                            //.color(_pathColor)
                    .geodesic(true));
            //.zIndex(z));
        }

        return root;
    }


    public void addTrackPoint(LatLng latLng) {
        trackPoints.add(latLng);
        if (route != null) {
            route.setPoints(trackPoints);
        }
    }

    public void marker(LatLng position, final BitmapDescriptor bitmapDescriptor, final String title) {
        if (map == null) return;

        if (position == null) {
            Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (loc == null) {
                return;
            }
            position = new LatLng(loc.getLatitude(), loc.getLongitude());
        }

        Marker marker = map.addMarker(new MarkerOptions()
                .position(position)
                .icon(bitmapDescriptor)
                .title(title));
        markers.add(marker);
    }

    public boolean saveKML(String filename) {
        try {
            File directory = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_dir));

            if (!directory.exists()) {
                directory.mkdirs();
            }

            if (directory.canWrite()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
                String currentDateandTime = sdf.format(new Date());

                int num = 0;
                File kmlFile = new File(directory, filename + currentDateandTime + num + ".kml");

                while (kmlFile.exists()) {
                    num++;
                    kmlFile = new File(directory, filename + currentDateandTime + num + ".kml");
                }

                FileWriter fileWriter = new FileWriter(kmlFile);
                BufferedWriter outWriter = new BufferedWriter(fileWriter);

                String trackCoords = "\n<coordinates>";
                for (int i = 0; i < trackPoints.size(); i++) {
                    trackCoords += trackPoints.get(i).longitude + "," + trackPoints.get(i).latitude + " ";
                }
                trackCoords += "\n</coordinates>";

                // Header
                outWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "\n <kml xmlns=\"http://www.opengis.net/kml/2.2\">" +
                        "\n <Document>" + "\n");

                // Polyline
                outWriter.write("<Placemark>" +
                        "\n<LineString id=\"route\">" +
                        "\n<gx:altitudeOffset>0</gx:altitudeOffset>" +
                        "\n<extrude>0</extrude>" +
                        "\n<tessellate>0</tessellate>" +
                        "\n<altitudeMode>clampToGround</altitudeMode>" +
                        "\n<gx:drawOrder>0</gx:drawOrder>" +
                        trackCoords +
                        "\n</LineString>" +
                        "\n</Placemark>\n");

                // Markers
                for (int i = 0; i < markers.size(); i++) {
                    Marker marker = markers.get(i);
                    LatLng position = marker.getPosition();

                    String iconLink = "http://maps.google.com/mapfiles/kml/paddle/wht-blank.png";
                    if (marker.getTitle().equals(getResources().getString(R.string.turning_left))) {
                        iconLink = "http://maps.google.com/mapfiles/kml/paddle/blu-blank.png";
                    } else if (marker.getTitle().equals(getResources().getString(R.string.turning_right))) {
                        iconLink = "http://maps.google.com/mapfiles/kml/paddle/pink-blank.png";
                    } else if (marker.getTitle().equals(getResources().getString(R.string.turn_end_90))) {
                        iconLink = "http://maps.google.com/mapfiles/kml/paddle/grn-diamond.png";
                    }

                    outWriter.write("<Placemark>" +
                            "\n<name>" + i + " | " + marker.getTitle() + "</name>" +
                            "\n<description> </description>" +
                            "\n<Style id=\"normalPlacemark" + i + "\">" +
                            "\n<IconStyle>" +
                            "\n<Icon>" +
                            "\n<href>" + iconLink + "</href>" +
                            "\n</Icon>" +
                            "\n</IconStyle>" +
                            "\n</Style>" +
                            "\n <Point>" +
                            "\n <coordinates>" + position.longitude + "," + position.latitude + "</coordinates>" +
                            "\n </Point>" +
                            "\n </Placemark>" + "\n");
                }

                outWriter.write("</Document>" +
                        "\n </kml>");
                outWriter.close();

                Log.i(TAG, "Saved map to " + directory + "/" + filename + currentDateandTime + num + ".kml");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save map", e);
        }

        return false;
    }
}
