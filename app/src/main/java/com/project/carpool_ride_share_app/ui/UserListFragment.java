package com.project.carpool_ride_share_app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.clustering.ClusterManager;
import com.project.carpool_ride_share_app.R;
import com.project.carpool_ride_share_app.UserClient;
import com.project.carpool_ride_share_app.adapters.UserRecyclerAdapter;
import com.project.carpool_ride_share_app.models.MarkerCluster;
import com.project.carpool_ride_share_app.models.User;
import com.project.carpool_ride_share_app.models.UserLocation;
import com.project.carpool_ride_share_app.util.MyClusterManagerRenderer;

import java.util.ArrayList;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.project.carpool_ride_share_app.Constants.MAPVIEW_BUNDLE_KEY;

/**
 *  COSC - 341 Carpool Ride Share Application
 *
 *  This is the basic map view for the application. It is a fragment view,
 *  being 50 / 50 map and userlist. All map related code is provided as open source
 *  by Google on GitHub - As this is a very standardized implementation
 *  we have not annotated that code for clarification.
 *
 */

public class UserListFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "UserListFragment";

    //widgets
    private RecyclerView mUserListRecyclerView;
    private MapView mMapView;


    //vars
    private ArrayList<User> mUserList = new ArrayList<>();
    private UserRecyclerAdapter mUserRecyclerAdapter;
    private ArrayList<UserLocation> userLocations = new ArrayList<>();
    private GoogleMap googleMap;
    private LatLngBounds mapBoundary;
    private UserLocation userPos;

    // refactor name ..
    private ClusterManager<MarkerCluster> clusterManager;
    private MyClusterManagerRenderer clusterManagerRenderer;
    private ArrayList<MarkerCluster> clusterMarkers = new ArrayList<>();


    public static UserListFragment newInstance(){
        return new UserListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mUserList = getArguments().getParcelableArrayList(getString(R.string.intent_user_list));
            userLocations = getArguments().getParcelableArrayList(getString(R.string.intent_user_locations));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_user_list, container, false);
        mUserListRecyclerView = view.findViewById(R.id.user_list_recycler_view);
        mMapView = (MapView) view.findViewById(R.id.user_list_map);
        initUserListRecyclerView();
        initGoogleMap(savedInstanceState);

        setUserPos();

        return view;
    }

    private void setCamera(){

        // set bounds .. + 0.1 coordinate from user location in each direction
        double bottomBounds = userPos.getGeoPoint().getLatitude() - 0.1;
        double leftBounds = userPos.getGeoPoint().getLongitude() - 0.1;
        double topBounds = userPos.getGeoPoint().getLatitude() + 0.1;
        double rightBounds = userPos.getGeoPoint().getLongitude() + 0.1;

        mapBoundary = new LatLngBounds(new LatLng(bottomBounds, leftBounds), new LatLng(topBounds, rightBounds));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary, 0));
    }

    private void setUserPos(){
        for(UserLocation userLoc : userLocations){
            if(userLoc.getUser().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                userPos = userLoc;
            }
        }
    }

    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    /**
     * Map is initialized with the latitude and longitude for Kelowna BC
     * User location is enabled as well. Emulator GPS coordinates are always set in AVD.
     * @param map
     */

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(49.882114, -119.477829)).title("Kelowna"));

        // Was not necessary initially for me to perform checks here again, but can't hurt.
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            return;
        }

        map.setMyLocationEnabled(true);
        googleMap = map;
        setCamera();
        addMapMarkers();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void initUserListRecyclerView(){
        mUserRecyclerAdapter = new UserRecyclerAdapter(mUserList);
        mUserListRecyclerView.setAdapter(mUserRecyclerAdapter);
        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    /*
        Loops through all users in chat room and creates a marker for them
     */
    private void addMapMarkers(){
        if(googleMap != null){

            if(clusterManager == null){
                clusterManager = new ClusterManager<MarkerCluster>(getActivity().getApplicationContext(), googleMap);
            }
            if(clusterManagerRenderer == null){
                clusterManagerRenderer = new MyClusterManagerRenderer(
                        getActivity(),
                        googleMap,
                        clusterManager
                );
                clusterManager.setRenderer(clusterManagerRenderer);
            }

            for(UserLocation userLocation: userLocations){

                Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeoPoint().toString());
                try{
                    String snippet = "";
                    if(userLocation.getUser().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                        Log.d(TAG, "MarkerSnippet: " + ((UserClient) getActivity().getApplicationContext()).getUser().getSnippet());
                        snippet = ((UserClient) getActivity().getApplicationContext()).getUser().getSnippet();
                    }
                    else{
                        Log.d(TAG, "MarkerSnippet: " + ((UserClient) getActivity().getApplicationContext()).getUser().getSnippet());
                        snippet = ((UserClient) getActivity().getApplicationContext()).getUser().getSnippet();
                    }

                    int avatar = R.drawable.stock_bg_login; // set the default avatar
                    try{
                        avatar = Integer.parseInt(userLocation.getUser().getAvatar());
                    }catch (NumberFormatException e){
                        Log.d(TAG, "addMapMarkers: no avatar for " + userLocation.getUser().getUsername() + ", setting default.");
                    }
                    MarkerCluster newClusterMarker = new MarkerCluster(
                            new LatLng(userLocation.getGeoPoint().getLatitude(), userLocation.getGeoPoint().getLongitude()),
                            userLocation.getUser().getUsername(),
                            snippet,
                            avatar,
                            userLocation.getUser()
                    );
                    clusterManager.addItem(newClusterMarker);
                    clusterMarkers.add(newClusterMarker);

                }catch (NullPointerException e){
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
                }

            }
            clusterManager.cluster();

            setCamera();
        }
    }


}



















