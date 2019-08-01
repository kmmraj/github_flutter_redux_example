package org.rekotlinexample.github.middleware

import android.content.Context
import android.content.SharedPreferences
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.anyInt
import org.rekotlinexample.github.actions.*
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.states.LoggedInState
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.rekotlin.Action
import org.rekotlin.DispatchFunction
import org.rekotlin.StateType
import org.rekotlin.Store
import java.util.concurrent.TimeUnit


@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TestGitHubMiddleware{

    internal data class TestState(var name:String? = null, var password:String? = null):StateType
    internal class TestAction(var name:String? = null, var password:String? = null): Action
    internal class TestStateReducer {
        var mAction: Action? = null
        fun handleAction(action: Action, state: TestState?): TestState {
            @Suppress("NAME_SHADOWING")
            var state = state ?: TestState()

            when(action){
                is TestAction -> {
                    state = state.copy(name = action.name)
                }
//                is LoginStartedAction -> {
//                    mAction = action
//                }
//                is RepoListRetrivalStartedAction -> {
//                    mAction = action
//                }
                else -> {
                    mAction = action
                }
            }

            return state
        }
    }

    var dispatch: ((Action) -> Unit)? = null
    internal val testStateReducer = TestStateReducer()
    internal var testStore: Store<TestState>? = null

    @Before
    fun setUp(){

         testStore = Store(
                reducer = testStateReducer::handleAction,
                state = TestState(),
                middleware = arrayListOf()
        )
        dispatch = {action: Action ->
            testStore?.dispatch(action)
        }

    }


    @Test // @DisplayName("Verify executeGitHubLogin function dispatches StartLoginAction")
    fun test_executeGitHubLogin_dispatches_StartLoginAction() {
        //Given

        val mockSharedPrefs = Mockito.mock(SharedPreferences::class.java)
        val mockSharedPrefsEditor = Mockito.mock(SharedPreferences.Editor::class.java)
        val mockContext = Mockito.mock(Context::class.java)
        testAppContext = mockContext
        Mockito.`when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPrefs)
        Mockito.`when`(mockSharedPrefs.edit()).thenReturn(mockSharedPrefsEditor)
        Mockito.`when`(mockSharedPrefsEditor.putString(anyString(), anyString())).thenReturn(mockSharedPrefsEditor)
        Mockito.`when`(mockSharedPrefsEditor.commit()).thenReturn(true)

        //When
        executeGitHubLogin(LoginAction("test","test"),dispatch = dispatch as DispatchFunction)


        //Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted { object : Runnable {
            override fun run() {
                assertThat(testStateReducer.mAction).isInstanceOf(LoginStartedAction::class.java)
            }
        }}


    }

    @Test // @DisplayName("Verify loginTaskListenerMiddleware function dispatches LoggedInDataSaveAction")
    fun test_loginTaskListenerMiddleware_dispatches_LoggedInDataSaveAction() {
        //Given
        val loginCompletedAction = LoginCompletedAction(userName = "test",
                token = "181816",
                loginStatus = LoggedInState.loggedIn)
        val loginTaskListenerMiddleware = LoginTaskListenerMiddleware()

        //When
        loginTaskListenerMiddleware.onFinished(loginCompletedAction, store = testStore as Store<StateType>)

        //Then
        assertThat(testStateReducer.mAction).isInstanceOf(LoggedInDataSaveAction::class.java)
    }

    @Test // @DisplayName("Verify loginTaskListenerMiddleware function dispatches LoginFailedAction")
    fun test_loginTaskListenerMiddleware_dispatches_LoginFailedAction() {
        //Given
        val loginCompletedAction = LoginCompletedAction(userName = "test",
                message = "Error Message",
                loginStatus = LoggedInState.notLoggedIn)
        val loginTaskListenerMiddleware = LoginTaskListenerMiddleware()

        //When
        loginTaskListenerMiddleware.onFinished(loginCompletedAction, store = testStore as Store<StateType>)

        //Then
        assertThat(testStateReducer.mAction).isInstanceOf(LoginFailedAction::class.java)
    }

    @Test // @DisplayName("Verify executeGitHubRepoListRetrieval function dispatches RepoListRetrivalStartedAction")
    fun test_executeGitHubRepoListRetrieval_dispatches_RepoListRetrivalStartedAction() {
        //Given
        val repoDetailListAction = RepoDetailListAction(userName = "test", token = "1818186")


        //When
        val executionResult = executeGitHubRepoListRetrieval(action = repoDetailListAction, dispatch = dispatch as DispatchFunction)

        //Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted { object : Runnable {
            override fun run() {
                assertThat(testStateReducer.mAction).isInstanceOf(RepoListRetrivalStartedAction::class.java)
                assertThat(executionResult).isTrue()
            }
        }}


    }

    @Test // @DisplayName("Verify executeGitHubRepoListRetrieval when token is null")
    fun test_executeGitHubRepoListRetrieval_when_token_is_null() {
        //Given
        val repoDetailListAction = RepoDetailListAction(userName = "test", token = null)

        //When
        val executionResult = executeGitHubRepoListRetrieval(action = repoDetailListAction, dispatch = dispatch as DispatchFunction)

        //Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted { object : Runnable {
            override fun run() {
                assertThat(executionResult).isFalse()
            }
        }}

    }

    @Test // @DisplayName("Verify executeGitHubRepoListRetrieval when context is not null")
    fun test_executeGitHubRepoListRetrieval_when_context_is_not_null() {
        //Given
        val repoDetailListAction = RepoDetailListAction(userName = "test", token = null)
        val sharedPrefs = Mockito.mock(SharedPreferences::class.java)
        val context = Mockito.mock(Context::class.java)
        testAppContext = context
        Mockito.`when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)
        Mockito.`when`(sharedPrefs.getString(anyString(), ArgumentMatchers.isNull())).thenReturn("teststring")

        //When
        val executionResult = executeGitHubRepoListRetrieval(action = repoDetailListAction, dispatch = dispatch as DispatchFunction)

        //Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted { object : Runnable {
            override fun run() {
                assertThat(executionResult).isTrue()
            }
        }}

    }
}