package kr.hs.jung.example

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExampleApplication : Application() {

    companion object {
        private const val TAG = "ExampleApplication"
    }

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    private inner class AppLifecycleObserver : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            // 앱이 포그라운드로 올 때 (앱 실행, 백그라운드에서 복귀)
            // Swift의 scenePhase == .active와 유사
            Log.d(TAG, "App became active (foreground)")
            // TODO: authManager.checkSession() 같은 로직 추가 가능
        }

        override fun onStop(owner: LifecycleOwner) {
            // 앱이 백그라운드로 갈 때
            // Swift의 scenePhase == .background와 유사
            Log.d(TAG, "App went to background")
        }

        override fun onPause(owner: LifecycleOwner) {
            // 앱이 비활성화될 때 (다른 앱으로 전환 중)
            // Swift의 scenePhase == .inactive와 유사
            Log.d(TAG, "App became inactive")
        }

        override fun onResume(owner: LifecycleOwner) {
            // 앱이 활성화될 때
            Log.d(TAG, "App resumed")
        }
    }
}
