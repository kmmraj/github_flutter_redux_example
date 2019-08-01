package org.rekotlinexample.github.asynctasks

import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.rekotlin.StateType
import org.rekotlin.Store
import org.rekotlinexample.github.actions.LoginDataModel
import org.rekotlinexample.github.actions.RepoListCompletedAction
import org.rekotlinexample.github.apirequests.GitHubApi
import org.rekotlinexample.github.asyntasks.RepoListTask
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.middleware.RepoListTaskListenerInterface
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Created by Mohanraj Karatadipalayam on 17/11/17.
 */

@Config(manifest= Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TestRepoListTask {
    @Test // @DisplayName("Verify RepoListTask dispatches RepoListCompletedAction(")
    fun test_RepoListTask_dispatches_RepoListCompletedAction() {

        // Given
        class TestMockGitHubApiService : GitHubApi {
            override fun getRepoList(userName: String, token: String): List<RepoViewModel> {
                val repo1 = RepoViewModel(repoName = "cleanAndroid"
                        ,htmlUrl = "https://github.com/kmmraj/android-clean-code",
                        forks = 1,
                        stargazersCount = 9,
                        language = "java",
                        description = "Clean Code")
                val repo2 = RepoViewModel(repoName = "reKotlinRouter",
                        htmlUrl = "https://github.com/kmmraj/ReKotlin",
                        forks = 1,
                        stargazersCount = 6,
                        language = "kotlin",
                        description = "Kotlin Code")
                return arrayListOf<RepoViewModel>(repo1,repo2)
            }

            override fun createToken(username: String, password: String): LoginDataModel {
                TODO()
            }

        }

        class TestRepoListTaskListenerMiddleware : RepoListTaskListenerInterface {
            var action: RepoListCompletedAction? = null

            override fun onFinished(result: RepoListCompletedAction,store: Store<StateType>) {
                action = result
            }

        }

        val testRepoListTaskListenerMiddleware = TestRepoListTaskListenerMiddleware()
        val repoListTask = RepoListTask(testRepoListTaskListenerMiddleware, "test", "test")
        repoListTask.githubService = TestMockGitHubApiService()
        //When
        repoListTask.execute()
        //Then
        Assertions.assertThat(testRepoListTaskListenerMiddleware.action).isInstanceOf(RepoListCompletedAction::class.java)
    }

}