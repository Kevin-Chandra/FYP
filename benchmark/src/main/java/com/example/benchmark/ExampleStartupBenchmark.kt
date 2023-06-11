package com.example.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This is an example startup benchmark.
 *
 * It navigates to the device's home screen, and launches the default activity.
 *
 * Before running this benchmark:
 * 1) switch your app's active build variant in the Studio (affects Studio runs only)
 * 2) add `<profileable android:shell="true" />` to your app's manifest, within the `<application>` tag
 *
 * Run this benchmark from Studio to see startup measurements, and captured system traces
 * for investigating your app's performance.
 */
@RunWith(AndroidJUnit4::class)
class ExampleStartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()


//    @Test
//    fun benchmarkNone() = startup(CompilationMode.None())
//    @Test
//    fun benchmarkPartial() = startup(CompilationMode.Partial())

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "com.example.fyp",
        metrics = listOf(StartupTimingMetric(),FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
//        compilationMode = mode
    ) {
        orderingSystem()
    }

}

fun MacrobenchmarkScope.orderingSystem(){
    device.waitForIdle()
    device.wait(Until.hasObject(By.textContains("Ordering System")),5000)
    device.findObject(By.textContains ("Ordering System")).click()

    device.wait(Until.hasObject(By.textContains("Ongoing Order")),5000)

    device.findObject(By.textContains("Ongoing Order")).click()
    device.waitForIdle()

    device.findObject(By.textContains("History")).click()
    device.waitForIdle()

    device.pressBack()
    device.waitForIdle()

    scrolling()
    categoryScrolling()

}

fun MacrobenchmarkScope.scrolling(){

    device.wait(Until.hasObject(By.res("product_list")), 5000)
    val list = device.findObject(By.res("product_list"))

    list.setGestureMargin(device.displayWidth/5)

    list.fling(Direction.DOWN)
    list.fling(Direction.UP)

    device.waitForIdle()
}

fun MacrobenchmarkScope.categoryScrolling(){
    device.wait(Until.hasObject(By.res("category_list")), 5000)
    val list = device.findObject(By.res("category_list"))

    list.setGestureMargin(device.displayHeight/5)
    list.fling(Direction.LEFT)
    list.fling(Direction.RIGHT)
}
