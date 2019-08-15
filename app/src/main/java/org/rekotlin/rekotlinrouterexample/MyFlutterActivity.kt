package org.rekotlin.rekotlinrouterexample

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.flutter.app.FlutterActivity

import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterView
import org.rekotlinexample.github.R


class MyFlutterActivity:
// FlutterActivity()
        FragmentActivity()
//        , FlutterEngineProvider
{


    private lateinit var myFlutterFragment: MyFlutterFragment

    companion object {
        const val CHANNEL = "org.rekotlin.rekotlinrouterexample.basicchannelcommunication"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_flutter)

        val someData = """
            
            {
              "myData": {
                "staggers": 6,
                "forks": 6,
                "languages": [
                  "kotlin",
                  "java"
                ],
                
              }
            }

        """.trimIndent()

        val flutterView = FlutterView(this)

        val channel = MethodChannel(flutterView, CHANNEL)
        channel.invokeMethod("message", someData)

        myFlutterFragment = MyFlutterFragment(someData)
        supportFragmentManager.beginTransaction().replace(R.id.container, myFlutterFragment)
                .commit()

    }

}