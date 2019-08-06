package org.rekotlin.rekotlinrouterexample

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import io.flutter.embedding.android.FlutterEngineProvider
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.view.FlutterMain
import org.rekotlinexample.github.R
import android.content.Intent





class MyFlutterActivity: FragmentActivity()
//        , FlutterEngineProvider
{


    private lateinit var myFlutterFragment: MyFlutterFragment

    private fun getArgsFromIntent(intent: Intent): Array<String>? {
        // Before adding more entries to this list, consider that arbitrary
        // Android applications can generate intents with extra data and that
        // there are many security-sensitive args in the binary.
        val args = ArrayList<String>()
        if (intent.getBooleanExtra("trace-startup", false)) {
            args.add("--trace-startup")
        }
        if (intent.getBooleanExtra("start-paused", false)) {
            args.add("--start-paused")
        }
        if (intent.getBooleanExtra("enable-dart-profiling", false)) {
            args.add("--enable-dart-profiling")
        }
        if (args.isNotEmpty()) {
            val argsArray = arrayOfNulls<String>(args.size)
            return args.toArray(argsArray)
        }
        return null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_flutter)

//        val args = getArgsFromIntent(intent)
//        FlutterMain.ensureInitializationComplete(getApplicationContext(), args);
        myFlutterFragment = MyFlutterFragment()
        supportFragmentManager.beginTransaction().replace(R.id.container, myFlutterFragment)
                .commit()
    }

//    override fun provideFlutterEngine(context: Context): FlutterEngine? {
//
//        return context
//    }
}