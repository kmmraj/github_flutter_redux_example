package org.rekotlin.rekotlinrouterexample


import android.app.PendingIntent.getActivity
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

        channel.setMethodCallHandler { methodCall, result ->
//            val args = methodCall.arguments
//
//            print("args are $args")
//            print("methodCall.method is $methodCall.method")

            when (methodCall.method){
                "handleMessageBack" -> {
                    this.onBackPressed()
                }
                else -> {

                }
            }

        }

        Handler().postDelayed({
            channel.invokeMethod("dataToDetailFlutterComponent", someData)
        },500)

        myFlutterFragment = MyFlutterFragment(someData)

    }

}