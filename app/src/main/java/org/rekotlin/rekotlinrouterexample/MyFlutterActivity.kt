package org.rekotlin.rekotlinrouterexample


import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
// TODO - Check this one
//class MyFlutterActivity : FlutterActivity(), FlutterFragment.FlutterEngineProvider {
//
//    override fun getFlutterEngine(context: Context): FlutterEngine? =
//            (context.applicationContext as MyApplication).engine
//
//}
import org.rekotlinexample.github.engine


class MyFlutterActivity:
//        FlutterFragmentActivity()
        FlutterActivity()
{

    private lateinit var myFlutterFragment: MyFlutterFragment

    companion object {
        const val REPO_DETAILS_CHANNEL = "repoInfo/details"
        const val REPO_LIST_CHANNEL = "repoInfo/list"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_my_flutter)

        val repoDetailsData = """
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

//        val channel = MethodChannel(flutterView, CHANNEL)
//
//        channel.setMethodCallHandler { methodCall, result ->
//            val args = methodCall.arguments
//
//            print("args are $args")
//            print("methodCall.method is $methodCall.method")
//
//            when (methodCall.method){
//                "handleMessageBack" -> {
//                    this.onBackPressed()
//                }
//                else -> {
//
//                }
//            }
//
//        }
//
//        Handler().postDelayed({
//            channel.invokeMethod("dataToDetailFlutterComponent", someData)
//        },500)

        // TODO: check the backdata from flutter  - platform channel will not work
        val repoListChannelMethod = MethodChannel(engine.dartExecutor, REPO_LIST_CHANNEL)
        repoListChannelMethod.setMethodCallHandler { methodCall, result ->
            val args = methodCall.arguments

            print("args are $args")
            print("methodCall.method is $methodCall.method")

            when (methodCall.method){
                "handleMessageBack" -> {
                    this.onBackPressed()
                }
                else -> {

                }
            }
        }



//        Handler().postDelayed({
//            engine.platformChannel.channel.invokeMethod("dataToDetailFlutterComponent", someData)
//        },500)
        val repoDetailsChannelMethod = MethodChannel(engine.dartExecutor, REPO_DETAILS_CHANNEL)
       // Handler().postDelayed({
        repoDetailsChannelMethod.invokeMethod("dataToDetailFlutterComponent", repoDetailsData)
       // },500)
        //myFlutterFragment = MyFlutterFragment(someData)

    }

}