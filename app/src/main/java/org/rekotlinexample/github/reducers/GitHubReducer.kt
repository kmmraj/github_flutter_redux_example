package org.rekotlinexample.github.reducers

import org.rekotlinexample.github.actions.*
import org.rekotlinexample.github.states.AuthenticationState
import org.rekotlinexample.github.states.GitHubAppState
import org.rekotlinexample.github.states.LoggedInState
import org.rekotlinexample.github.states.RepoListState
import org.rekotlinrouter.NavigationReducer
import org.rekotlinrouter.NavigationState
import org.rekotlinrouter.SetRouteAction
import org.rekotlinrouter.SetRouteSpecificData
import tw.geothings.rekotlin.Action

/**
* Created by Mohanraj Karatadipalayam on 14/10/17.
*/

//fun loginReducer(action: Action, state: GitHubAppState?)
fun loginReducer(action: Action, oldState: GitHubAppState?): GitHubAppState {
    // if no state has been provided, create the default state
//    var authenticationState = AuthenticationState(loggedInState = LoggedInState.loggedIn,
//            userName = "")
//    val navigationState = NavigationReducer.handleAction(action = action,
//            state = state?.navigationState)

    var state = oldState ?: GitHubAppState(navigationState = NavigationReducer.handleAction(action = action,
            state = oldState?.navigationState),
            authenticationState = AuthenticationState(loggedInState = LoggedInState.loggedIn,
                    userName = ""),
            repoListState = RepoListState())

    when (action) {

        is LoginStartedAction -> {
            val authenticationState = state.authenticationState.copy(isFetching = true)
            state =  state.copy(authenticationState = authenticationState)
        }
        is LoginCompletedAction -> {
            val authenticationState = state.authenticationState.copy(isFetching = false,
                    loggedInState = LoggedInState.loggedIn,
                    fullName = action.fullName,
                    createdAt = action.createdAt,
                    avatarUrl = action.avatarUrl,
                    location = action.location)
            state =  state.copy(authenticationState = authenticationState)
        }
        is LoginFailedAction -> {
            val authenticationState = state.authenticationState.copy(isFetching = false,
                    loggedInState = LoggedInState.notLoggedIn,
                    errorMessage = action.message,
                    userName = action.userName)
            state =  state.copy(authenticationState = authenticationState)
        }
        is LoggedInDataSaveAction -> {
            val authenticationState = state.authenticationState.copy(isCompleted = true )
            state =  state.copy(authenticationState = authenticationState)
        }
        is RepoListRetrivalStartedAction -> {
            val repoListState = state.repoListState.copy(isFetching = true)
            state = state.copy(repoListState = repoListState)
        }
        is RepoListCompletedAction -> {
            val repoListState = state.repoListState.copy(isFetching = false,
                    isCompleted = true,
                    repoList = action.repoList)
            state = state.copy(repoListState = repoListState)
        }
        is SetRouteAction -> {
//            val navigationState = NavigationReducer.handleAction(action = action,
//                    state = state?.navigationState)
            state = state.copy(navigationState = NavigationReducer.handleAction(action = action,
                    state = state.navigationState))
        }

        is SetRouteSpecificData -> {
//            val navigationState = NavigationReducer.handleAction(action = action,
//                    state = state?.navigationState)
            state = state.copy(navigationState = NavigationReducer.handleAction(action = action,
                    state = state.navigationState))
        }

    }
    return state
}

//TODO : Abstract it like the below
fun appReducer(action: Action, oldState: GitHubAppState?) : GitHubAppState {

    // if no state has been provided, create the default state
    val state = oldState ?: GitHubAppState(
            navigationState = NavigationReducer.handleAction(action = action, state = oldState?.navigationState),
            authenticationState = AuthenticationState(loggedInState = LoggedInState.loggedIn,
                    userName = ""),
            repoListState = RepoListState())

    return state.copy(
            navigationState = (::navigationReducer)(action, state.navigationState),
            authenticationState = (::authenticationReducer)(action, state.authenticationState),
            repoListState = (::repoListReducer)(action, state.repoListState))
}

fun authenticationReducer(action: Action, state: AuthenticationState?): AuthenticationState {

    val newState =  state ?: AuthenticationState(LoggedInState.notLoggedIn,userName = "")
    when (action) {

        is LoginStartedAction -> {
             return newState.copy(isFetching = true)
        }
        is LoginCompletedAction -> {
             return newState.copy(isFetching = false,
                    loggedInState = LoggedInState.loggedIn,
                    fullName = action.fullName,
                    createdAt = action.createdAt,
                    avatarUrl = action.avatarUrl,
                    location = action.location)
        }
        is LoginFailedAction -> {
            return newState.copy(isFetching = false,
                    loggedInState = LoggedInState.notLoggedIn,
                    errorMessage = action.message,
                    userName = action.userName)
        }
        is LoggedInDataSaveAction -> {
            return newState.copy(isCompleted = true)
        }
    }
    return newState
}

fun repoListReducer(action: Action, state: RepoListState?): RepoListState {
    val newState =  state ?: RepoListState()
    when (action) {
        is RepoListRetrivalStartedAction -> {
           return newState.copy(isFetching = true)
        }
        is RepoListCompletedAction -> {
            return newState.copy(isFetching = false,
                    isCompleted = true,
                    repoList = action.repoList)
        }
    }
    return newState
}

fun navigationReducer(action: Action, oldState: NavigationState?): NavigationState {
    val state =  oldState ?: NavigationReducer.handleAction(action = action, state = oldState)
    when (action) {
        is SetRouteAction -> {
            return NavigationReducer.handleAction(action = action, state = state)
        }

        is SetRouteSpecificData -> {
            return NavigationReducer.handleAction(action = action, state = state)
        }
    }
    return state
}
