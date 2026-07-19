package com.lieruce.goforlunch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NavigationFlowTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private void waitForView(@IdRes int viewId, long timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < endTime) {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return;
            } catch (Exception | Error ignored) {}
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        onView(withId(viewId)).check(matches(isDisplayed()));
    }

    private static ViewAction forceClick() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return ViewMatchers.isClickable(); }
            @Override public String getDescription() { return "force click"; }
            @Override public void perform(UiController uiController, View view) { view.performClick(); }
        };
    }

    @Test
    public void clickingRestaurantInList_shouldOpenDetails() {
        // 1. Navigate to Restaurants tab
        onView(withId(R.id.navigation_restaurants)).perform(forceClick());
        waitForView(R.id.restaurant_recycler_view, 5000);

        // 2. Click on a specific restaurant name (from Mock data)
        onView(withText("Le Petit Gourmet")).perform(click());

        // 3. Verify Detail screen views are shown
        waitForView(R.id.detail_restaurant_name, 5000);
        onView(withId(R.id.detail_restaurant_name)).check(matches(withText("Le Petit Gourmet")));
        onView(withId(R.id.detail_restaurant_address)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_select_fab)).check(matches(isDisplayed()));
    }
}
