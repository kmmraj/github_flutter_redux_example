package org.rekotlinexample.github.middleware

import org.rekotlinexample.github.AppController
import org.rekotlinexample.github.actions.*
import org.rekotlinexample.github.asyntasks.RepoListTask
import org.rekotlinexample.github.asyntasks.UserLoginTask
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.apirequests.PreferenceApiService
import org.rekotlinexample.github.apirequests.PreferenceApiService.GITHUB_PREFS_KEY_LOGINSTATUS
import org.rekotlinexample.github.apirequests.PreferenceApiService.GITHUB_PREFS_KEY_TOKEN
import org.rekotlinexample.github.apirequests.PreferenceApiService.GITHUB_PREFS_KEY_USERNAME
import org.rekotlinexample.github.states.GitHubAppState
import org.rekotlinexample.github.states.LoggedInState
import tw.geothings.rekotlin.DispatchFunction
import tw.geothings.rekotlin.Middleware

/**
* Created by Mohanraj Karatadipalayam on 17/10/17.
*/

interface LoginTaskListenerInterface {
    fun onFinished(result: LoginCompletedAction)
}

class LoginTaskListenerMiddleware : LoginTaskListenerInterface {
    override fun onFinished(result: LoginCompletedAction){

        if (result.loginStatus == LoggedInState.loggedIn ) {
            result.token?.let {
                mainStore.dispatch(result)
                mainStore.dispatch(LoggedInDataSaveAction(userName = result.userName,
                        token = result.token as String, loginStatus = LoggedInState.loggedIn))
            }
        } else {
            result.message?.let{
                mainStore.dispatch(LoginFailedAction(userName = result.userName,
                        message = result.message as String))
            }

        }

    }
}

interface RepoListTaskListenerInterface {
    fun onFinished(result: RepoListCompletedAction)
}

class RepoListTaskListenerMiddleware: RepoListTaskListenerInterface {
    override fun onFinished(result: RepoListCompletedAction) {
        mainStore.dispatch(result)
    }

}

// TODO: Do it like below - change to traditional func signature

//fun gitHubMiddlewarex(dispatch: (Action) -> Unit, getState:  () -> StateType?): (DispatchFunction) -> DispatchFunction

//typealias DispatchFunction = (Action) -> Unit
//typealias Middleware<State> = (DispatchFunction, () -> State?) -> (DispatchFunction) -> DispatchFunction
internal val gitHubMiddleware: Middleware<GitHubAppState> = { dispatch, getState ->
    { next ->
        { action ->
             when (action) {
                is LoginAction -> {
                    executeGitHubLogin(action, dispatch)
                }
                 is LoggedInDataSaveAction -> {
                     executeSaveLoginData(action)
                 }
                 is RepoDetailListAction -> {
                     executeGitHubRepoListRetrieval(dispatch)
                 }
             }

            next(action)

        }
    }
}

private fun executeGitHubRepoListRetrieval(dispatch: DispatchFunction) {
    //Log.d(TAG, "Inside RepoDetailListAction")
    var userName: String? = null
    var token: String? = null
    val context = AppController.instance?.applicationContext
    context?.let {
        userName = PreferenceApiService.getPreference(context, GITHUB_PREFS_KEY_USERNAME)
        token = PreferenceApiService.getPreference(context, GITHUB_PREFS_KEY_TOKEN)
    }

    userName?.let {
        token?.let {
            val repoListTaskListenerMiddleware = RepoListTaskListenerMiddleware()
            val repoTask = RepoListTask(repoListTaskListenerMiddleware,
                    userName as String,
                    token as String)
            // TODO - remove this
          //  repoTask.githubService = MockGitHubApiService()
            repoTask.execute()
            dispatch(RepoListRetrivalStartedAction())
        }
    }
}

private fun executeSaveLoginData(action: LoggedInDataSaveAction) {
    val context = AppController.instance?.applicationContext
    context?.let {
        PreferenceApiService.savePreference(context,
                GITHUB_PREFS_KEY_TOKEN, action.token)
        PreferenceApiService.savePreference(context,
                GITHUB_PREFS_KEY_USERNAME, action.userName)
        PreferenceApiService.savePreference(context,
                GITHUB_PREFS_KEY_LOGINSTATUS, LoggedInState.loggedIn.name)
    }
}

private fun executeGitHubLogin(action: LoginAction, dispatch: DispatchFunction) {
    val loginTaskListenerMiddleware = LoginTaskListenerMiddleware()


    val authTask = UserLoginTask(loginTaskListenerMiddleware,
            action.userName,
            action.password )

    // TODO - remove this
    //authTask.githubService = MockGitHubApiService()
    authTask.execute()
    dispatch(LoginStartedAction(action.userName))
}




