package com.lieruce.goforlunch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
/*
import static androidx.test.espresso.contrib.DrawerActions.open;
*/
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
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
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // Helper to wait for a view (simple version for this exercise)
    private void waitForView(@IdRes int viewId, long timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < endTime) {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return;
            } catch (Exception | Error ignored) {
            }
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        // Final attempt which will throw the exception if still not found
        onView(withId(viewId)).check(matches(isDisplayed()));
    }

    // Helper to wait for a view to be gone
    private void waitForViewToBeGone(Matcher<View> matcher, long timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < endTime) {
            try {
                onView(matcher).check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
                return;
            } catch (Exception | Error ignored) {
            }
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
    }

    // Helper to click even if partially covered
    private static ViewAction forceClick() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isClickable();
            }

            @Override
            public String getDescription() {
                return "force click";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        };
    }

    @Test
    public void bottomNavigation_shouldSwitchFragments() {
        // 1. Verify Map is shown initially
        waitForView(R.id.map, 5000);

        // 2. Click on Restaurants List tab
        onView(withId(R.id.navigation_restaurants)).perform(forceClick());
        waitForView(R.id.restaurant_recycler_view, 5000);

        // 3. Click on Workmates tab
        onView(withId(R.id.navigation_workmates)).perform(forceClick());
        waitForView(R.id.workmates_recycler_view, 5000);

        // 4. Click back to Map
        onView(withId(R.id.navigation_map)).perform(forceClick());
        waitForView(R.id.map, 5000);
    }

    @Test
    public void navigationDrawer_shouldOpen() {
        // Open Drawer using a custom ViewAction
        onView(withId(R.id.drawer_layout)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(DrawerLayout.class);
            }

            @Override
            public String getDescription() {
                return "open drawer";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((DrawerLayout) view).openDrawer(GravityCompat.START);
                uiController.loopMainThreadUntilIdle();
            }
        });

        // Wait for it to be visible
        waitForView(R.id.nav_view, 5000);
    }
}
