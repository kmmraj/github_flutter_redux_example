package org.rekotlinexample.github

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.squareup.leakcanary.LeakCanary
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.systemchannels.KeyEventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterMain
import org.rekotlinexample.github.actions.LoggedInDataSaveAction
import org.rekotlinexample.github.middleware.gitHubMiddleware
import org.rekotlinexample.github.reducers.appReducer
import org.rekotlinexample.github.routes.RootRoutable

import org.rekotlinexample.github.apirequests.PreferenceApiService
import org.rekotlinexample.github.states.AuthenticationState
import org.rekotlinexample.github.states.GitHubAppState
import org.rekotlinexample.github.states.LoggedInState
import org.rekotlinexample.github.states.RepoListState
import org.rekotlinrouter.NavigationState
import org.rekotlinrouter.Router
import org.rekotlin.Store
import org.rekotlinexample.github.controllers.RepoListActivity

/**
 * Created by Mohanraj Karatadipalayam on 15/10/17.
 */

var mainStore = Store(state = null,
        reducer = ::appReducer,
        middleware = arrayListOf(gitHubMiddleware))

private var mInstance: AppController? = null
var router: Router<GitHubAppState>? = null
lateinit var engine: FlutterEngine
lateinit var repoDetailsChannelMethod : MethodChannel

class AppController : Application() {


    val TAG = "AppController"

    //Creating sharedpreferences object
    //We will store the user data in sharedpreferences
    private val sharedPreference: SharedPreferences by lazy {
        PreferenceApiService.getSharedPreferenceByName(context = applicationContext,
                sharedPreferenceKey = PreferenceApiService.GITHUB_PREFS_NAME)
    }


    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        mInstance = this
        instance = this
        val loginState = getLogedInState()

        val authenticationState = AuthenticationState(loggedInState = loginState.loginStatus,
                userName = loginState.userName)
        val state = GitHubAppState(navigationState = NavigationState(),
                authenticationState = authenticationState,
                repoListState = RepoListState())
        mainStore = Store(state = state,
//                reducer = ::loginReducer,
                reducer = ::appReducer,
                middleware = arrayListOf(gitHubMiddleware),
                automaticallySkipRepeats = true)
        router = Router(store = mainStore,
                rootRoutable = RootRoutable(context = applicationContext),
                stateTransform = { subscription ->
                    subscription.select { stateType ->
                        stateType.navigationState
                    }
                })

//        FlutterMain.startInitialization(applicationContext)
//        FlutterMain.ensureInitializationComplete(applicationContext, arrayOf<String>())
//        engine = FlutterEngine(this)
////        val entryPoint = DartExecutor.DartEntrypoint(this.assets,
////                FlutterMain.findAppBundlePath(this).toString(), "main")
//
//
//        val entryPoint =  DartExecutor.DartEntrypoint(
//                FlutterMain.findAppBundlePath(), "main")
//        engine.dartExecutor.executeDartEntrypoint(entryPoint)


        // Instantiate a FlutterEngine.
        engine = FlutterEngine(this)

        // Start executing Dart code to pre-warm the FlutterEngine.
        engine.dartExecutor.executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        )


        // Cache the FlutterEngine to be used by FlutterActivity.
        FlutterEngineCache
                .getInstance()
                .put("my_engine_id", engine)

        repoDetailsChannelMethod = MethodChannel(engine.dartExecutor, RepoListActivity.REPO_DETAILS_CHANNEL)

//        repoDetailsChannelMethod.setMethodCallHandler { call, result ->
//            val args = call.arguments
//
//            Log.d(TAG,"args are $args")
//            Log.d(TAG,"methodCall.method is $call.method")
//            when (call.method) {
//                "handleMessageBack" -> {
//                    Log.d(TAG,"Message from flutter is $result")
//
//                }
//            }
//        }

    }



    fun persistUserDetails(email: String,token:String) {
        var editor = sharedPreference.edit()
        editor.putString(PreferenceApiService.GITHUB_PREFS_NAME, email)
        editor.putString(PreferenceApiService.GITHUB_PREFS_KEY_TOKEN, token)
        editor.putBoolean(PreferenceApiService.GITHUB_PREFS_KEY_LOGINSTATUS, true)
        editor.apply()
    }

    //This method will clear the sharedpreference
    //It will be called on logout
    fun clearUserDetails() {
        val editor = sharedPreference.edit()
        editor.clear()
        editor.apply()
    }

    fun getLogedInState(): LoggedInDataSaveAction {
        val token = PreferenceApiService.getPreference(applicationContext, PreferenceApiService.GITHUB_PREFS_KEY_TOKEN)
//        var loginState: LoggedInState = LoggedInState.notLoggedIn
//        if (mToken != null){
//            loginState = LoggedInState.loggedIn
//        }
        val userName: String? = PreferenceApiService.getPreference(applicationContext, PreferenceApiService.GITHUB_PREFS_KEY_USERNAME)

        val loginStateString = PreferenceApiService.getPreference(applicationContext, PreferenceApiService.GITHUB_PREFS_KEY_LOGINSTATUS)

        var loginState = LoggedInState.notLoggedIn
        if (loginStateString === LoggedInState.loggedIn.name){
            loginState = LoggedInState.loggedIn
        }
        val savedLoginDataAction = LoggedInDataSaveAction(userName = userName?:"",
                token = token?:"",
                loginStatus = loginState)
        return savedLoginDataAction
    }

    companion object {

        //Getting tag it will be used for displaying log and it is optional
        val TAG = AppController::class.java.simpleName


        //Creating class object
        //Public static method to get the instance of this class
        @get:Synchronized var instance: AppController? = null
            private set

        fun getAppController(context: Context): AppController {
            if (instance == null) {
                //Create instance
                instance = AppController()
            }

            return instance as AppController
        }
    }


}
