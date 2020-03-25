package com.project.carpool_ride_share_app.ui;

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
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.project.carpool_ride_share_app.models.Chatroom;
import com.project.carpool_ride_share_app.Constants;
import com.project.carpool_ride_share_app.models.User;
import com.project.carpool_ride_share_app.models.UserLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.google.android.gms.common.ConnectionResult.*;
import static com.project.carpool_ride_share_app.Constants.ERROR_DIALOG_REQUEST;
import static com.project.carpool_ride_share_app.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.project.carpool_ride_share_app.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

/**
 * -----------------------------------------------------------------
 * Basis is formed by open source tutorial credit to CodingWithMitch.
 * -----------------------------------------------------------------
 * COSC 341 - Car pool Application - Current Main Activity
 * <p>
 * Updated, further developed and annotated by us.
 * <p>
 * Once logged in create / enter / delete chat-rooms.
 * Also verifies that the user has granted the appropriate permissions
 * and has google services enabled.
 * -----------------------------------------------------------------
 */

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        ChatroomRecyclerAdapter.ChatroomRecyclerClickListener {

    // Used in logs
    private static final String TAG = "LandingPage";

    //widgets
    private ProgressBar mProgressBar;

    //vars
    private ArrayList<Chatroom> mChatrooms = new ArrayList<>();
    private Set<String> mChatroomIds = new HashSet<>();
    private ChatroomRecyclerAdapter mChatroomRecyclerAdapter;
    private RecyclerView mChatroomRecyclerView;
    private ListenerRegistration mChatroomEventListener;
    private FirebaseFirestore mDb;
    private UserLocation userLocation;

    // Used to verify permissions were granterd
    private boolean LocationPermissionsGranted = false;
    // Used in finding User location
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve references
        mProgressBar = findViewById(R.id.progressBar);
        mChatroomRecyclerView = findViewById(R.id.chatrooms_recycler_view);

        // Get location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set listeners for the creat and delete chatroom buttons
        findViewById(R.id.fab_create_chatroom).setOnClickListener(this);
        findViewById(R.id.btn_delete_chatroom).setOnClickListener(this);

        // retrieve instance of database
        mDb = FirebaseFirestore.getInstance();

        // initialize the action / tool bar and the recycler view. (Older android feature).
        initSupportActionBar();
        initChatroomRecyclerView();
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

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
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

    /*
     ---------------- Retrieve, create and delete chat room methods ------------------------------
     */

    // On click handlers for the create / delete chat-room
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
        Intent intent = new Intent(MainActivity.this, ChatroomActivity.class);
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
                    Toast.makeText(MainActivity.this, "Enter a chatroom name", Toast.LENGTH_SHORT).show();
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

    // Freeing up resources on activity destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatroomEventListener != null) {
            mChatroomEventListener.remove();
        }
    }

    // Get Chat rooms to display - and Verify user has granted permissions / Last known location
    @Override
    protected void onResume() {
        super.onResume();
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

    // Enter chatroom if clicked
    @Override
    public void onChatroomSelected(int position) {
        navChatroomActivity(mChatrooms.get(position));
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
                    Toast.makeText(MainActivity.this, "Enter a chatroom name", Toast.LENGTH_SHORT).show();
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
    ---------------- Additional methods for initialization / sign out etc ------------------------
     */

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Options in action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {
                signOut();
                return true;
            }
            case R.id.action_profile: {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog() {
        mProgressBar.setVisibility(View.GONE);
    }

}
