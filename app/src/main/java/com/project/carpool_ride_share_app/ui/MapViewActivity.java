package com.project.carpool_ride_share_app.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.carpool_ride_share_app.R;
import com.project.carpool_ride_share_app.UserClient;
import com.project.carpool_ride_share_app.adapters.ChatroomRecyclerAdapter;
import com.project.carpool_ride_share_app.adapters.UserRecyclerAdapter;
import com.project.carpool_ride_share_app.models.Chatroom;
import com.project.carpool_ride_share_app.models.MarkerCluster;
import com.project.carpool_ride_share_app.models.User;
import com.project.carpool_ride_share_app.models.UserLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.google.android.gms.common.ConnectionResult.SUCCESS;
import static com.project.carpool_ride_share_app.Constants.ERROR_DIALOG_REQUEST;
import static com.project.carpool_ride_share_app.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.project.carpool_ride_share_app.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener,
        ChatroomRecyclerAdapter.ChatroomRecyclerClickListener {
    private MapView mMapView;

    private static final String TAG = "LandingPage";


    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private ArrayList<Chatroom> mChatrooms = new ArrayList<>();
    private Set<String> mChatroomIds = new HashSet<>();
    private ChatroomRecyclerAdapter mChatroomRecyclerAdapter;
    private RecyclerView mChatroomRecyclerView;
    private ListenerRegistration mChatroomEventListener;
    private FirebaseFirestore mDb;
    private UserLocation userLocation;
    private GoogleMap googleMap;
    private ProgressBar mProgressBar;

    //vars
    private ArrayList<User> mUserList = new ArrayList<>();
    private UserRecyclerAdapter mUserRecyclerAdapter;
    private ArrayList<UserLocation> userLocations = new ArrayList<>();
    private LatLngBounds mapBoundary;
    private UserLocation userPos;


    // Used to verify permissions were granterd
    private boolean LocationPermissionsGranted = false;
    // Used in finding User location
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.user_list_map);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        mProgressBar = findViewById(R.id.progressBar);
        mChatroomRecyclerView = findViewById(R.id.chatrooms_recycler_view);

        // Get location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);





        // Wip
        //retrieveUserLocation();

        // Set listeners for the creat and delete chatroom buttons
        findViewById(R.id.fab_create_chatroom).setOnClickListener(this);
        findViewById(R.id.btn_delete_chatroom).setOnClickListener(this);

        // retrieve instance of database
        mDb = FirebaseFirestore.getInstance();

        // initialize the action / tool bar and the recycler view. (Older android feature).
        initSupportActionBar();
        initChatroomRecyclerView();


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.fab_create_chatroom: {
                newChatroomDialog();
            }
            break;

            case R.id.btn_delete_chatroom: {
                newDeleteChatroomDialog();
            }
            break;
        }
    }

    // Green bar up top ..
    private void initSupportActionBar() {
        setTitle("Chatrooms");
    }

    // Firebase handles the sign out, redirect to login activity
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Recycler view initialization
    private void initChatroomRecyclerView() {
        mChatroomRecyclerAdapter = new ChatroomRecyclerAdapter(mChatrooms, this);
        mChatroomRecyclerView.setAdapter(mChatroomRecyclerAdapter);
        mChatroomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // Retrieve chat rooms stored in db
    private void getChatrooms() {

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);

        // Collection is like a relation - though firebase is noSQL
        CollectionReference chatroomsCollection = mDb.collection(getString(R.string.collection_chatrooms));

        mChatroomEventListener = chatroomsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "onEvent: called.");

                // Early return for exceptions during query
                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {

                    // For each result returned create an Chatroom object
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Chatroom chatroom = doc.toObject(Chatroom.class);

                        // Add to the arraylists up top.
                        if (!mChatroomIds.contains(chatroom.getChatroom_id())) {
                            mChatroomIds.add(chatroom.getChatroom_id());
                            mChatrooms.add(chatroom);
                        }
                    }
                    Log.d(TAG, "onEvent: number of chatrooms: " + mChatrooms.size());

                    // Notify recycler view new data may be displayed
                    mChatroomRecyclerAdapter.notifyDataSetChanged();
                }

            }
        });
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
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        getChatrooms();
        getUserDetails();
        if (checkMapServices()) {
            if (LocationPermissionsGranted) {
                getChatrooms();
            } else {
                getLocationPermission();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(49.882114, -119.477829)).title("Kelowna"));

        // Was not necessary initially for me to perform checks here again, but can't hurt.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        map.setMyLocationEnabled(true);
        googleMap = map;


        // Currently Broken -- wip
        //setCamera();

        // Gets the coordinates of where the user clicks. Currently only outputs the coords to sout only.
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                System.out.println("Map clicked [" + point.latitude + " / " + point.longitude + "]");
            }
        });

    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        if (mChatroomEventListener != null) {
            mChatroomEventListener.remove();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    // Self explanatory - Chat room object is in models directory. Standard java object.
    private void buildNewChatroom(String chatroomName) {

        // Create object and set title
        final Chatroom chatroom = new Chatroom();
        chatroom.setTitle(chatroomName);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        mDb.setFirestoreSettings(settings);

        // Creat new document reference in db
        DocumentReference newChatroomRef = mDb.collection(getString(R.string.collection_chatrooms)).document();

        // Retrieve the newly created id - see Firebase for detail
        chatroom.setChatroom_id(newChatroomRef.getId());

        // Set the value in db - check if successful
        newChatroomRef.set(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideDialog();

                // if successful create navigate to the newly created chatroom
                if (task.isSuccessful()) {
                    navChatroomActivity(chatroom);
                } else {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Move into newly created Chat room
    private void navChatroomActivity(Chatroom chatroom) {
        Intent intent = new Intent(MapViewActivity.this, ChatroomActivity.class);
        intent.putExtra(getString(R.string.intent_chatroom), chatroom);
        startActivity(intent);
    }

    // Prompt for creating a new chat room when button is pressed
    private void newChatroomDialog() {

        // Title of alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a chatroom name");

        // Set the input type to EditText view
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Create the confirm and cancel buttons with appropriate logic / toast
        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().equals("")) {
                    buildNewChatroom(input.getText().toString());
                } else {
                    Toast.makeText(MapViewActivity.this, "Enter a chatroom name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onChatroomSelected(final int position) {

        // Title of alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Destination or Message :");

        // Set the input type for snippet / message on map
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Create the confirm and cancel buttons with appropriate logic / toast
        builder.setPositiveButton("DRIVER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((UserClient) getApplicationContext()).getUser().setRole(getString(R.string.driver));

                String snippet = input.getText().toString();
                Log.d(TAG, "onSnippetRead: " + snippet);
                ((UserClient) getApplicationContext()).getUser().setSnippet(snippet);

                Log.d(TAG, "onRoleSet: " + ((UserClient) getApplicationContext()).getUser().getRole());
                Log.d(TAG, "onRoleSet: " + ((UserClient) getApplicationContext()).getUser().getSnippet());

                navChatroomActivity(mChatrooms.get(position));
            }
        });
        builder.setNegativeButton("PASSENGER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((UserClient) getApplicationContext()).getUser().setRole(getString(R.string.passenger));

                String snippet = input.getText().toString();
                Log.d(TAG, "onSnippetRead" + snippet);
                ((UserClient) getApplicationContext()).getUser().setSnippet(snippet);

                ((UserClient) getApplicationContext()).getUser().setSnippet(input.getText().toString());

                Log.d(TAG, "onRoleSet: " + ((UserClient) getApplicationContext()).getUser().getRole());
                Log.d(TAG, "onRoleSet: " + ((UserClient) getApplicationContext()).getUser().getSnippet());

                navChatroomActivity(mChatrooms.get(position));
            }
        });

        builder.show();

    }


    // TODO  Write a method that checks if a rooms name is in use and then inform the user when trying to create it

    // I wrote these to delete the chat rooms - They are basically the same in function as the add methods above.
    private void newDeleteChatroomDialog() {

        // Alert title
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a chatroom name to be removed");

        // Set input type
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Create confirm and cancel buttons
        builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().equals("")) {
                    deleteChatroom(input.getText().toString());
                } else {
                    Toast.makeText(MapViewActivity.this, "Enter a chatroom name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void deleteChatroom(String chatroomName) {

        // Get collection of all chatrooms - the string resource is kinda pointless here but
        CollectionReference chatroomsCollection = mDb
                .collection(getString(R.string.collection_chatrooms));

        //  Unnecessary object but whatever
        final Chatroom delete = new Chatroom();
        delete.setTitle(chatroomName);

        // Query Firebase
        mChatroomEventListener = chatroomsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                // Early return if exception is thrown
                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {

                    // for each doc returned create a chatroom object
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Chatroom chatroom = doc.toObject(Chatroom.class);

                        // If title of chat room to be deleted same as current iterations chat room
                        if (delete.getTitle().equalsIgnoreCase(chatroom.getTitle())) {

                            // delete that sucker
                            doc.getReference().delete();

                            // Remove from arraylists - probably redundant
                            mChatroomIds.remove(chatroom.getChatroom_id());
                            mChatrooms.remove(chatroom);
                        }
                    }

                    Log.d(TAG, "onEvent: number of chatrooms: " + mChatrooms.size());
                    mChatroomRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });
        navBackToMainActivity();
    }

    private void navBackToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    /*
     --------------------- Methods for retrieving current user location ----------------------------
     */

    // Step 1 - Create the UserLocation Object - See models directory
    private void getUserDetails() {
        if (userLocation == null) {
            // Create new object
            userLocation = new UserLocation();

            // Query database for a document of this particular authorized user (firebaseauth)
            DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            // Use reference to query Firebase
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "OnComplete: Retrieved the user details");

                        // Create User Object and fill in the blanks for our UserLocation
                        User user = task.getResult().toObject(User.class);
                        userLocation.setUser(user);

                        // Set User Client - see class for details
                        ((UserClient) getApplicationContext()).setUser(user);
                        getLastKnownLocation();
                    }
                }
            });
        } else {
            getLastKnownLocation();
        }
    }


    // Step 2 - Method actually used to retrieve last location - includes permission check
    private void getLastKnownLocation() {

        // Was not necessary initially for me to perform checks again, but can't hurt.
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Using our Fused location object set listener and see if it was successful
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {

                    // Use GeoPoint object to retrieve location
                    Location loc = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());

                    // For debugging purposes
                    Log.e(TAG, "onComplete: " + geoPoint.getLatitude());
                    Log.e(TAG, "onComplete: " + geoPoint.getLongitude());

                    userLocation.setGeoPoint(geoPoint);
                    // Due to the @ServerTimestamp - a null argument timestamps it
                    userLocation.setTimestamp(null);
                    saveUserLocation();
                }
            }
        });
    }

    // Step 3 -  Self explanatory
    private void saveUserLocation() {
        if (userLocation != null) {

            // Get reference to document in db
            DocumentReference locationRef = mDb.collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            // add userlocation to db - log if successful
            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Successfully Inserted Location into db.");
                    }
                }
            });
        }
    }

    /*
    ------------------------------- Permission and GPS checks --------------------------------------
     */

    // Verify Google services / gps are enabled -- see below functions.
    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    // Verify / prompt user to grant GPS permissions
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    // Actually verifies the user has location and maps enabled
    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    // Whenever we successfully retrieve permissions - get chat rooms and last known location.
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationPermissionsGranted = true;
            getChatrooms();
            getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // Check google services and APIs / key is enabled.
    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapViewActivity.this);

        if (available == SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapViewActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        LocationPermissionsGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationPermissionsGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (LocationPermissionsGranted) {
                    getChatrooms();
                    getUserDetails();
                } else {
                    getLocationPermission();
                }
            }
        }

    }

    // Sets the camera view on the map component to center on user location
    private void setCamera() {

        retrieveUserLocation();
        // set bounds .. + 0.1 coordinate from user location in each direction
        double bottomBounds = userPos.getGeoPoint().getLatitude() - 0.1;
        double leftBounds = userPos.getGeoPoint().getLongitude() - 0.1;
        double topBounds = userPos.getGeoPoint().getLatitude() + 0.1;
        double rightBounds = userPos.getGeoPoint().getLongitude() + 0.1;

        mapBoundary = new LatLngBounds(new LatLng(bottomBounds, leftBounds), new LatLng(topBounds, rightBounds));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary, 0));
    }


    private void retrieveUserLocation() {

        try {
            final DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            Log.d(TAG, "1");

            userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    Log.d(TAG, "2");

                    if (task.isSuccessful()) {

                        Log.d(TAG, "3");
                        userPos = task.getResult().toObject(UserLocation.class);
                    }
                }
            });
        } catch (IllegalStateException e) {
            Log.e(TAG, "retrieveUserLocation: Error. Ending query." + e.getMessage());
        }
    }


}


