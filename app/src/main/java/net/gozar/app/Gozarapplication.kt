package net.gozar.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GozarApplication : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val foreground = MutableStateFlow(false)
    private var startedActivities = 0

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                startedActivities++
                foreground.value = startedActivities > 0
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivities = (startedActivities - 1).coerceAtLeast(0)
                foreground.value = startedActivities > 0
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

        scope.launch {
            val store = withContext(Dispatchers.IO) { ConfigStore.get(applicationContext) }
            val selector = AutoSelector(applicationContext, store)
            combine(store.autoSelect, foreground) { enabled, fg -> enabled && fg }
                .distinctUntilChanged()
                .collect { on ->
                    if (on) selector.start(scope) else selector.stop()
                }
        }
    }
}