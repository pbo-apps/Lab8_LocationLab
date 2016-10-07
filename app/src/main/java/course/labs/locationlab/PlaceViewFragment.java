package course.labs.locationlab;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import static android.os.SystemClock.elapsedRealtimeNanos;

public class PlaceViewFragment extends ListFragment implements LocationListener {
	private static final long FIVE_MINS = 5 * 60 * 1000;
	private static final String TAG = "Lab-Location";

	// False if you don't want to use network access
	public static boolean useNetwork = true;

	private Location mLastLocationReading;
	private PlaceViewAdapter mAdapter;
	private LocationManager mLocationManager;
	private ConnectivityManager mConnectivityManager;
	private boolean mMockLocationOn = false;

	// default minimum time between new readings
	private final long mMinTime = 5000;

	// default minimum distance between old and new readings.
	private final float mMinDistance = 1000.0f;

	// A fake location provider used for testing
	private MockLocationProvider mMockLocationProvider;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up location manager to let us monitor changes in location
		mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		// Set up connectivity manager to track changes in network status
		mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        // Set up the app's user interface. This class is a ListFragment,
        // so it has its own ListView. ListView's adapter should be a PlaceViewAdapter

		ListView placesListView = getListView();

		// DONE - add a footerView to the ListView
		// You can use footer_view.xml to define the footer

		View footerView = getLayoutInflater(savedInstanceState).inflate(R.layout.footer_view, placesListView, false);

		// DONE - footerView must respond to user clicks, handling 3 cases:

		// There is no current location - response is up to you. The best
		// solution is to always disable the footerView until you have a
		// location.

		// There is a current location, but the user has already acquired a
		// PlaceBadge for this location - issue a Toast message with the text -
		// "You already have this location badge."
		// Use the PlaceRecord class' intersects() method to determine whether
		// a PlaceBadge already exists for a given location

		// There is a current location for which the user does not already have
		// a PlaceBadge. In this case download the information needed to make a new
		// PlaceBadge.

		footerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				if (mLastLocationReading == null) {

					// Let user know why they can't yet get the place badge
					Toast.makeText(view.getContext(), R.string.location_not_set, Toast.LENGTH_LONG).show();

				} else if (mAdapter.intersects(mLastLocationReading)) {

					Toast.makeText(view.getContext(), R.string.duplicate_location_string, Toast.LENGTH_LONG).show();

				} else {

					PlaceDownloaderTask downloader = new PlaceDownloaderTask((PlaceViewFragment)
							getFragmentManager().findFragmentById(R.id.fragment_place_view), deviceHasNetwork());
					downloader.execute(mLastLocationReading);

				}

			}

		});

		placesListView.addFooterView(footerView);
		mAdapter = new PlaceViewAdapter(getActivity().getApplicationContext());
		setListAdapter(mAdapter);

	}

	@Override
	public void onResume() {
		super.onResume();

		startMockLocationManager();

		// DONE - Check NETWORK_PROVIDER for an existing location reading.
		Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (isFresh(lastKnownLocation))
            setLastLocationReading(lastKnownLocation);

        // DONE - register to receive location updates from NETWORK_PROVIDER
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mMinTime, mMinDistance, this);
        
	}

    // Function to record our last known location, but only if it's FRESH
    void setLastLocationReading(Location location) {
        if (isFresher(location))
            mLastLocationReading = location;
    }

    @Override
	public void onPause() {

		// DONE - unregister for location updates
		mLocationManager.removeUpdates(this);
        
		shutdownMockLocationManager();
		super.onPause();
	}

	// Callback method used by PlaceDownloaderTask
	public void addNewPlace(PlaceRecord place) {
	
		// DONE - Attempt to add place to the adapter, considering the following cases

		// A PlaceBadge for this location already exists - issue a Toast message
		// with the text - "You already have this location badge." Use the PlaceRecord 
		// class' intersects() method to determine whether a PlaceBadge already exists
		// for a given location. Do not add the PlaceBadge to the adapter
		
		// The place is null - issue a Toast message with the text
		// "PlaceBadge could not be acquired"
		// Do not add the PlaceBadge to the adapter
		
		// The place has no country name - issue a Toast message
		// with the text - "There is no country at this location". 
		// Do not add the PlaceBadge to the adapter
		
		// Otherwise - add the PlaceBadge to the adapter

		if (place == null) {

			Toast.makeText(getActivity().getApplicationContext(), R.string.place_badge_unavailable, Toast.LENGTH_LONG).show();

		} else if (mAdapter.intersects(place.getLocation())) {

			Toast.makeText(getActivity().getApplicationContext(), R.string.duplicate_location_string, Toast.LENGTH_LONG).show();

		} else if (place.getCountryName() == null || place.getCountryName().isEmpty()) {

			Toast.makeText(getActivity().getApplicationContext(), R.string.no_country_string, Toast.LENGTH_LONG).show();

		} else {

			mAdapter.add(place);

		}

	}

	// LocationListener methods
	@Override
	public void onLocationChanged(Location currentLocation) {

		setLastLocationReading(currentLocation);

	}

	@Override
	public void onProviderDisabled(String provider) {
		// not implemented
	}

	@Override
	public void onProviderEnabled(String provider) {
		// not implemented
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// not implemented
	}

	// Commenting out this method as I don't want to use it but it was provided with the lab skeleton code.
	// According to the documentation, currentTimeMillis and getTime are unreliable for measuring elapsed
	// time, as they use UTC time irrespective of time zone. I've therefore written the isFresh method instead.
	// Returns age of location in milliseconds
//	private long ageInMilliseconds(Location location) {
//		return System.currentTimeMillis() - location.getTime();
//	}

	// Helper method to determine if a location reading is fresh or not
	private boolean isFresh(Location location) {
        return location != null &&
                elapsedRealtimeNanos() - location.getElapsedRealtimeNanos() < FIVE_MINS * 1000000;
	}

    // Helper method to determine if a location reading is more recent than our last known location
    // DONE - Update location considering the following cases.
    // 1) If there is no last location, set the last location to the current
    // location.
    // 2) If the current location is older than the last location, ignore
    // the current location
    // 3) If the current location is newer than the last locations, keep the
    // current location.
    private boolean isFresher(Location location) {
        return location != null
                && (mLastLocationReading == null ||
                    location.getElapsedRealtimeNanos() >= mLastLocationReading.getElapsedRealtimeNanos());
    }

	// Helper method to determine whether or not we are currently connected to the internet
    // Currently only uses Wifi, as downloading little flags over mobile data seems frivolous
	private boolean deviceHasNetwork() {
        if (!useNetwork)
            return false;

		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();

		return activeNetwork != null &&
				activeNetwork.isConnectedOrConnecting() &&
                activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete_badges:
			mAdapter.removeAllViews();
			return true;
		case R.id.place_one:
			mMockLocationProvider.pushLocation(37.422, -122.084);
			return true;
		case R.id.place_no_country:
			mMockLocationProvider.pushLocation(0, 0);
			return true;
		case R.id.place_two:
			mMockLocationProvider.pushLocation(38.996667, -76.9275);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void shutdownMockLocationManager() {
		if (mMockLocationOn) {
			mMockLocationProvider.shutdown();
		}
	}

	private void startMockLocationManager() {
		if (!mMockLocationOn) {
			mMockLocationProvider = new MockLocationProvider(
					LocationManager.NETWORK_PROVIDER, getActivity().getApplicationContext());
		}
	}
}
