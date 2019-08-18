package org.rekotlin.rekotlinrouterexample


import android.os.Bundle
import android.os.Handler
import io.flutter.app.FlutterFragmentActivity
import io.flutter.plugin.common.MethodChannel


class MyFlutterActivity:
        FlutterFragmentActivity()
{

    private lateinit var myFlutterFragment: MyFlutterFragment

    companion object {
        const val CHANNEL = "repoInfo/details"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_my_flutter)

        val someData = """
  {
  "repoDetail": {
    "stargazers": 222,
    "forks": 33,
    "languages": [
      "kotlin",
      "java"
    ]    
  }
}
        """.trimIndent()

        val channel = MethodChannel(flutterView, CHANNEL)

        Handler().postDelayed({
            channel.invokeMethod("message", someData)
        },500)

        myFlutterFragment = MyFlutterFragment(someData)

    }

}