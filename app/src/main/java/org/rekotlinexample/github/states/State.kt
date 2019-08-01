package org.rekotlinexample.github.states

import org.rekotlin.StateType
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinrouter.HasNavigationState
import org.rekotlinrouter.NavigationState

/**
 * Created by Mohanraj Karatadipalayam on 12/10/17.
 */



data class AuthenticationState(var loggedInState: LoggedInState,
                               var userName: String,
                               var isFetching: Boolean = false,
                               var isCompleted: Boolean = false,
                               var errorMessage: String? = null,
                               var fullName: String? = null,
                               var location:String? = null,
                               var avatarUrl:String? = null,
                               var createdAt: String? = null): StateType

data class RepoListState(var repoList: List<RepoViewModel>? = null,
                         var isFetching: Boolean = false,
                         var isCompleted: Boolean = false,
                         var selectedRepoIndex: Int = -1): StateType

enum class LoggedInState {
    notLoggedIn,
    loggedIn
}

data class GitHubAppState(override var navigationState: NavigationState,
                          var authenticationState: AuthenticationState,
                          var repoListState: RepoListState): StateType, HasNavigationState

