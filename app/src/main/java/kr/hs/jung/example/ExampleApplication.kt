package kr.hs.jung.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kr.hs.jung.example.data.remote.PersistentCookieJar
import kr.hs.jung.example.util.logger.AppLogger

/**
 * 앱 Application 클래스
 *
 * 앱 전역 초기화와 생명주기 관리를 담당합니다.
 *
 * 주요 역할:
 * - Hilt 의존성 주입 초기화
 * - CookieJar 초기화 (암호화 저장소 → 메모리)
 * - Activity 생명주기 로깅
 * - 앱 포그라운드/백그라운드 상태 감지
 */
@HiltAndroidApp
class ExampleApplication : Application() {

    // 앱 전체 스코프 (앱 종료 시까지 유지)
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CookieJarEntryPoint {
        fun cookieJar(): PersistentCookieJar
    }

    override fun onCreate() {
        super.onCreate()

        // CookieJar 초기화 (비동기, 메인 스레드 블로킹 없음)
        initializeCookieJar()

        registerActivityLifecycleCallbacks(ActivityLifecycleCallback())
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    /**
     * PersistentCookieJar를 초기화합니다.
     * 암호화 저장소에서 쿠키를 메모리로 로드합니다.
     */
    private fun initializeCookieJar() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    this@ExampleApplication,
                    CookieJarEntryPoint::class.java
                )
                entryPoint.cookieJar().initialize()
                AppLogger.d("App", "CookieJar initialized successfully")
            } catch (e: Exception) {
                AppLogger.e("App", "Failed to initialize CookieJar: ${e.message}", e)
            }
        }
    }

    /**
     * Activity 생명주기 콜백
     *
     * 개별 Activity의 생명주기를 추적할 때 사용
     */
    private inner class ActivityLifecycleCallback : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            AppLogger.d("App", "Activity created: ${activity::class.java.simpleName}")
        }

        override fun onActivityStarted(activity: Activity) {
            AppLogger.d("App", "Activity started: ${activity::class.java.simpleName}")
        }

        override fun onActivityResumed(activity: Activity) {
            AppLogger.d("App", "Activity resumed: ${activity::class.java.simpleName}")
        }

        override fun onActivityPaused(activity: Activity) {
            AppLogger.d("App", "Activity paused: ${activity::class.java.simpleName}")
        }

        override fun onActivityStopped(activity: Activity) {
            AppLogger.d("App", "Activity stopped: ${activity::class.java.simpleName}")
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            AppLogger.d("App", "Activity saveInstanceState: ${activity::class.java.simpleName}")
        }

        override fun onActivityDestroyed(activity: Activity) {
            AppLogger.d("App", "Activity destroyed: ${activity::class.java.simpleName}")
        }
    }

    /**
     * 앱 전체 생명주기 옵저버 (ProcessLifecycleOwner)
     *
     * 앱이 포그라운드/백그라운드로 전환될 때 감지
     * - onStart: 앱이 포그라운드로 진입
     * - onStop: 앱이 백그라운드로 진입
     */
    private inner class AppLifecycleObserver : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            AppLogger.d("App", "App lifecycle: onCreate")
        }

        override fun onStart(owner: LifecycleOwner) {
            AppLogger.d("App", "App lifecycle: onStart (foreground)")
        }

        override fun onResume(owner: LifecycleOwner) {
            AppLogger.d("App", "App lifecycle: onResume")
        }

        override fun onPause(owner: LifecycleOwner) {
            AppLogger.d("App", "App lifecycle: onPause")
        }

        override fun onStop(owner: LifecycleOwner) {
            AppLogger.d("App", "App lifecycle: onStop (background)")
        }

        override fun onDestroy(owner: LifecycleOwner) {
            AppLogger.d("App", "App lifecycle: onDestroy")
        }
    }
}
