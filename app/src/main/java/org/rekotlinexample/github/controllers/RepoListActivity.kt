package org.rekotlinexample.github.controllers

//import io.flutter.embedding.android.FlutterFragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import org.rekotlin.StoreSubscriber
import org.rekotlinexample.github.R
import org.rekotlinexample.github.actions.RepoDetailListAction
import org.rekotlinexample.github.engine
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.repoDetailsChannelMethod
import org.rekotlinexample.github.routes.loginRoute
import org.rekotlinexample.github.routes.repoDetailRoute
import org.rekotlinexample.github.routes.welcomeRoute
import org.rekotlinexample.github.states.RepoListState
import org.rekotlinrouter.Route
import org.rekotlinrouter.SetRouteAction
import org.rekotlinrouter.SetRouteSpecificData




class RepoListActivity : AppCompatActivity(),
        AdapterView.OnItemClickListener,
        StoreSubscriber<RepoListState> //,
       // FlutterEngineProvider
{

    private val mViewProgress: View by lazy {
        this.findViewById<View>(R.id.pb_progress)
    }
    private val mViewForm: View by lazy {
        this.findViewById<View>(R.id.repo_cell)
    }

    var mListOfRepos: List<RepoViewModel>? = null
    private var mRepoListViewAdapter: RepoListAdapter? = null
   // var repoDetailsChannelMethod : MethodChannel? = null

    companion object {
        const val REPO_DETAILS_CHANNEL = "repoInfo/details"
        const val REPO_LIST_CHANNEL = "repoInfo/list"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
    //    FlutterMain.startInitialization(this);
        super.onCreate(savedInstanceState);
//        GeneratedPluginRegistrant.registerWith(this);
        setContentView(R.layout.activity_repo_list)
        createRepoListView()
        mainStore.subscribe(this){
            it.select { it.repoListState }
                    .skipRepeats()
        }
        mainStore.dispatch(RepoDetailListAction())
//        repoDetailsChannelMethod = MethodChannel(engine.dartExecutor, REPO_DETAILS_CHANNEL)
//        repoDetailsChannelMethod?.setMethodCallHandler { call, result ->
//            val args = call.arguments
//
//            print("args are $args")
//            print("methodCall.method is $call.method")
//            when (call.method) {
//                "handleMessageBack" -> {
//                    print("Message from flutter is $result")
//                }
//            }
//        }

    }



    override fun onDestroy() {
        super.onDestroy()
        mainStore.unsubscribe(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val welcomeRoutes = arrayListOf(loginRoute, welcomeRoute)
        val welcomeAction = SetRouteAction(route = welcomeRoutes)
        mainStore.dispatch(welcomeAction)
    }


    fun createRepoListView() {
       val listView: ListView =  findViewById(org.rekotlinexample.github.R.id.repo_cell) as ListView
        mRepoListViewAdapter = RepoListAdapter()
        listView.adapter = mRepoListViewAdapter
        listView.isClickable = true
        listView.onItemClickListener = this
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        startRepoDetailsActivity(position)
    }


// TODO: Pass the actual data
    private fun startRepoDetailsActivity( position: Int) {

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

        startActivity(
                FlutterActivity
                        .withCachedEngine("my_engine_id")
                        .build(this)
        )


        repoDetailsChannelMethod.invokeMethod("dataToDetailFlutterComponent", repoDetailsData)
    }



    fun startRepoDetailActivity(position: Int){

        val routes: Route? = determineRoute()
        val setectedRepo = mListOfRepos?.get(position)
        setectedRepo?.let {
            val actionData =  SetRouteSpecificData(route = routes as Route, data = setectedRepo)
            val action = SetRouteAction(route = routes)
            mainStore.dispatch(actionData)
            mainStore.dispatch(action)
        }

    }

    private fun determineRoute(): Route? {
        val currentRoute: Route = mainStore.state.navigationState.route.clone() as Route
        //val routes = arrayListOf(loginRoute, repoListRoute, repoDetailRoute)
        val routes: Route?
        if (!currentRoute.contains(repoDetailRoute)) {
            routes = currentRoute.plus(repoDetailRoute) as Route
        } else {
            routes = currentRoute
        }
        return routes
    }

    override fun newState(state: RepoListState) {
        if (state.isFetching){
            ViewHelper.showProgress(show = true,
                    view = mViewForm,
                    progressView = mViewProgress,
                    resources = resources)
        }
        if (state.isCompleted) {
            mListOfRepos = state.repoList
            mRepoListViewAdapter?.notifyDataSetChanged()
            ViewHelper.showProgress(show = false,
                    view = mViewForm,
                    progressView = mViewProgress,
                    resources = resources)

        }

    }






    private inner class RepoListAdapter internal constructor() : BaseAdapter() {

        private val layoutInflater: LayoutInflater

        init {
            layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        override fun getCount(): Int {
            return mListOfRepos?.size ?: 0
        }

        override fun getItem(position: Int): Any = mListOfRepos?.get(position) ?: 0

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.repo_cell, null)
                val viewHolder = ViewHolder()
                viewHolder.tvRepoName = convertView?.findViewById(R.id.tv_repo_name) as TextView
                viewHolder.tvRepoFullName = convertView.findViewById(R.id.tv_repo_fullname) as TextView
                viewHolder.tvRepoForksCount = convertView.findViewById(R.id.tv_repo_forks) as TextView
                viewHolder.tvRepoStarsCount = convertView.findViewById(R.id.tv_repo_stars) as TextView
                viewHolder.tvRepolanguage = convertView.findViewById(R.id.tv_repo_language) as TextView
                convertView.tag = viewHolder
            }
            val viewHolder = convertView.tag as ViewHolder
            viewHolder.tvRepoName?.text = mListOfRepos?.get(position)?.repoName
            viewHolder.tvRepoForksCount?.text = mListOfRepos?.get(position)?.forks.toString()
            viewHolder.tvRepoStarsCount?.text = mListOfRepos?.get(position)?.stargazersCount.toString()
            viewHolder.tvRepoFullName?.text = mListOfRepos?.get(position)?.description
            viewHolder.tvRepolanguage?.text = mListOfRepos?.get(position)?.language
            return convertView
        }
    }

    internal inner class ViewHolder {
        var tvRepoName: TextView? = null
        var tvRepoFullName: TextView? = null
        var tvRepoForksCount: TextView? = null
        var tvRepoStarsCount: TextView? = null
        var tvRepolanguage: TextView? = null
    }

}
