package course.labs.locationlab.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import course.labs.locationlab.MainActivity;
import course.labs.locationlab.PlaceViewFragment;
import course.labs.locationlab.R;

public class TestTwoValidLocations extends
		ActivityInstrumentationTestCase2<MainActivity> {
	private Solo solo;

	public TestTwoValidLocations() {
		super(MainActivity.class);
	}

	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
		PlaceViewFragment.useNetwork = false;
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testRun() {

		int delay = 2000;
		int longDelay = 5000;

		// Wait for activity: 'course.labs.locationlab.PlaceViewFragment'
		solo.waitForActivity(MainActivity.class,
				2000);

		// Click on action bar item
		solo.clickOnActionBarItem(course.labs.locationlab.R.id.place_one);

		solo.sleep(delay);

		// Click on Get New Place		
		assertTrue("Footer view isn't visible",
				solo.waitForView(course.labs.locationlab.R.id.footer, 0, delay));
		solo.clickOnView(solo.getView(course.labs.locationlab.R.id.footer));

		solo.sleep(delay);

		// Assert that PlaceBadge is shown
		assertTrue("PlaceBadge is not shown!", solo.waitForText(
				solo.getString(R.string.the_greenhouse_string), 1, longDelay));

		// Click on action bar item
		solo.clickOnActionBarItem(course.labs.locationlab.R.id.place_two);

		solo.sleep(delay);

		// Click on Get New Place
		solo.clickOnView(solo.getView(course.labs.locationlab.R.id.footer));

		solo.sleep(delay);

		// Assert that PlaceBadge is shown
		assertTrue("PlaceBadge is not shown!", solo.waitForText(
				solo.getString(R.string.berwyn_string), 1, longDelay));

	}
}
