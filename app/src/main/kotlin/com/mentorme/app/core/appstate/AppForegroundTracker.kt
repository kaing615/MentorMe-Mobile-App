package com.mentorme.app.core.appstate

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppForegroundTracker : Application.ActivityLifecycleCallbacks {
    private val startedCount = AtomicInteger(0)
    private val _isForeground = MutableStateFlow(false)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        if (startedCount.incrementAndGet() > 0) {
            _isForeground.value = true
        }
    }

    override fun onActivityStopped(activity: Activity) {
        val count = startedCount.decrementAndGet()
        if (count <= 0) {
            startedCount.set(0)
            _isForeground.value = false
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
