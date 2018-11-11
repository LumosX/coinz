package eu.zerovector.coinz

import android.os.SystemClock
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import eu.zerovector.coinz.Activities.MainMenuActivity
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith




/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MainMenuActivityTests {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("eu.zerovector.coinz", appContext.packageName)
    }

    // This checks whether the tutorials in the GameActivity work
    @Rule @JvmField
    var activityRule = ActivityTestRule(MainMenuActivity::class.java)


    @Test fun testSplashScreen() {
        // Check splash screen is visible
        onView(withId(R.id.layoutSplash)).check(matches(isDisplayed()))
        // Check that the menu is visible after a click
        onView(withId(R.id.layoutSplash)).perform(click())
        onView(withId(R.id.layoutMainMenu)).check(matches(isDisplayed()))
        // Check the splash screen's invisibility after the animation is over
        SystemClock.sleep(1100)
        onView(withId(R.id.layoutSplash)).check(matches(not(isDisplayed())))
    }

    @Test fun testBackgroundVisible() {
        onView(withId(R.id.bgE)).check(matches(isDisplayed()))
    }


    @Test fun testTitleAndSubtitle() {
        onView(withId(R.id.lblTitle)).check(matches(withText(containsString("CLANDESTINE"))))
        onView(withId(R.id.lblTitle)).check(matches(withText(containsString("OPERATION"))))
        onView(withId(R.id.lblTitle)).check(matches(withText(containsString("IMMINENT"))))
        onView(withId(R.id.lblTitle)).check(matches(withText(containsString("NOCTURNAL"))))
        onView(withId(R.id.lblTitle)).check(matches(withText(containsString("ZIGGURAT"))))
        onView(withId(R.id.lblAGameOfSpies)).check(matches(withText(containsString("A game of spies"))))
    }


    @Test fun testRegisterButton() {
        // "NO INSTRUMENTATION REGISTERED! MUST RUN UNDER A REGISTERING INSTRUMENTATION!" OH YEAH? FUCK YOU
        onView(withId(R.id.layoutSplash)).perform(click())
        onView(withId(R.id.btnRegister)).perform(click())
    }
}
