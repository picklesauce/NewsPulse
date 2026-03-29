package com.example.newspulse

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.data.ArticleDiskCache
import com.example.newspulse.data.LocalAuthRepository
import com.example.newspulse.data.NewsApiRepository
import com.example.newspulse.data.ReadingHistoryPreferences
import com.example.newspulse.data.SupabaseAuthRepository
import com.example.newspulse.data.SupabaseInterestsCatalogRepository
import com.example.newspulse.data.SupabaseInterestsRepository
import com.example.newspulse.data.SupabaseReadingHistoryRepository
import com.example.newspulse.data.SupabaseSavedArticlesRepository
import com.example.newspulse.data.SupabaseUserPreferencesRepository
import com.example.newspulse.data.UserPreferences
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.data.remote.EventRegistryApi
import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.data.remote.SupabaseSdkHolder
import com.example.newspulse.data.remote.SupabaseUserSession
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import com.example.newspulse.domain.AuthRepository
import com.example.newspulse.domain.InterestsCatalogRepository
import com.example.newspulse.domain.InterestsRepository
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.NewsRepository
import com.example.newspulse.domain.ReadingHistoryRepository
import com.example.newspulse.domain.SavedArticlesRepository
import com.example.newspulse.domain.UserPreferencesRepository
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.ViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.ArticleDetailScreen
import com.example.newspulse.ui.view.ArticleListScreen
import com.example.newspulse.ui.view.ExploreScreen
import com.example.newspulse.ui.view.FiltersScreen
import com.example.newspulse.ui.view.InterestsScreen
import com.example.newspulse.ui.view.LoginScreen
import com.example.newspulse.ui.view.NewsPulseScaffold
import com.example.newspulse.ui.view.ProfileScreen
import com.example.newspulse.ui.view.ReadingHistoryScreen
import com.example.newspulse.ui.view.SavedArticlesScreen
import com.example.newspulse.ui.view.SignUpScreen
import com.example.newspulse.ui.view.TopicSelectionScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            SupabaseSdkHolder.client?.handleDeeplinks(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val useSupabase = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
        val interestsCatalogRepository: InterestsCatalogRepository
        val interestsRepository: InterestsRepository
        val userPreferencesRepository: UserPreferencesRepository
        val readingHistoryRepository: ReadingHistoryRepository
        val savedArticlesRepository: SavedArticlesRepository
        val authRepository: AuthRepository

        if (useSupabase) {
            val userSession = SupabaseUserSession(this)
            val supabaseSdk = SupabaseSdkHolder.init(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
            ) ?: error("Supabase SDK init failed")
            // Prefer JWT from the Auth SDK (always fresh after refresh / OAuth); fall back to cached session.
            // Using only prefs missed refreshes and caused anon-key REST calls → RLS blocked writes/reads for Google.
            val resolveAccessToken: () -> String? = {
                val t = supabaseSdk.auth.currentSessionOrNull()?.accessToken
                if (!t.isNullOrBlank()) t else userSession.accessToken
            }
            val resolveUserId: () -> String? = {
                userSession.userId
                    ?: supabaseSdk.auth.currentSessionOrNull()?.user?.id?.toString()
            }
            val restClient = SupabaseRestClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                anonKey = BuildConfig.SUPABASE_ANON_KEY,
                accessTokenProvider = resolveAccessToken
            )
            interestsCatalogRepository = SupabaseInterestsCatalogRepository(restClient)
            interestsRepository = SupabaseInterestsRepository(restClient, resolveUserId)
            userPreferencesRepository = SupabaseUserPreferencesRepository(this, restClient, resolveUserId)
            readingHistoryRepository = SupabaseReadingHistoryRepository(restClient, resolveUserId)
            savedArticlesRepository = SupabaseSavedArticlesRepository(restClient, resolveUserId)
            authRepository = SupabaseAuthRepository(
                client = restClient,
                session = userSession,
                supabase = supabaseSdk
            )
        } else {
            interestsCatalogRepository = MockInterestsCatalogRepository()
            interestsRepository = MockInterestsRepository()
            userPreferencesRepository = UserPreferences(this)
            readingHistoryRepository = ReadingHistoryPreferences(this)
            savedArticlesRepository = InMemorySavedArticlesRepository()
            authRepository = LocalAuthRepository(userPreferencesRepository)
        }

        val newsRepository = createNewsRepository(interestsCatalogRepository, interestsRepository)
        val model = NewsPulseModel(
            newsRepository = newsRepository,
            interestsRepository = interestsRepository,
            interestsCatalogRepository = interestsCatalogRepository,
            userPreferencesRepository = userPreferencesRepository,
            readingHistoryRepository = readingHistoryRepository,
            savedArticlesRepository = savedArticlesRepository,
            authRepository = authRepository
        )
        val viewModelFactory = ViewModelFactory(model)

        if (useSupabase) {
            SupabaseSdkHolder.client?.handleDeeplinks(intent)
        }

        setContent {
            NewsPulseTheme {
                CompositionLocalProvider(
                    CompositionLocals.LocalViewModelFactory provides viewModelFactory
                ) {
                    val navController = rememberNavController()
                    var supabaseReady by remember { mutableStateOf(!useSupabase) }
                    var startDestination by remember {
                        mutableStateOf(computeStartDestination(useSupabase, authRepository, model))
                    }

                    LaunchedEffect(useSupabase) {
                        if (!useSupabase) return@LaunchedEffect
                        withContext(Dispatchers.IO) {
                            runCatching {
                                // Restores session + loads interests/onboarding + profile from DB for current user
                                model.syncSupabaseAuthSessionToApp()
                                (interestsCatalogRepository as SupabaseInterestsCatalogRepository)
                                    .preloadCatalogIfEmpty()
                                (savedArticlesRepository as SupabaseSavedArticlesRepository)
                                    .refreshSavedSuspend()
                                (readingHistoryRepository as SupabaseReadingHistoryRepository)
                                    .refreshFromRemote()
                            }
                        }
                        startDestination = computeStartDestination(useSupabase, authRepository, model)
                        supabaseReady = true
                    }

                    if (!supabaseReady) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("signup") {
                            SignUpScreen(navController = navController)
                        }
                        composable("login") {
                            LoginScreen(navController = navController)
                        }
                        composable("topicSelection") {
                            TopicSelectionScreen(navController = navController)
                        }
                        composable("home") {
                            NewsPulseScaffold(navController = navController) {
                                ArticleListScreen(navController = navController)
                            }
                        }
                        composable("explore") {
                            NewsPulseScaffold(navController = navController) {
                                ExploreScreen(navController = navController)
                            }
                        }
                        composable("saved") {
                            NewsPulseScaffold(navController = navController) {
                                SavedArticlesScreen(navController = navController)
                            }
                        }
                        composable("profile") {
                            NewsPulseScaffold(navController = navController) {
                                ProfileScreen(navController = navController)
                            }
                        }
                        composable("filters") {
                            NewsPulseScaffold(navController = navController) {
                                FiltersScreen(navController = navController)
                            }
                        }
                        composable("interests") {
                            NewsPulseScaffold(navController = navController) {
                                InterestsScreen(navController = navController)
                            }
                        }
                        composable("readingHistory") {
                            NewsPulseScaffold(navController = navController) {
                                ReadingHistoryScreen(navController = navController)
                            }
                        }
                        composable("articleDetail/{id}") { backStackEntry ->
                            val articleId = backStackEntry.arguments?.getString("id")
                            NewsPulseScaffold(navController = navController) {
                                ArticleDetailScreen(
                                    navController = navController,
                                    articleId = articleId
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }

    private fun createNewsRepository(
        catalog: InterestsCatalogRepository,
        interestsRepo: InterestsRepository
    ): NewsRepository {
        val apiKey = BuildConfig.NEWSAPI_AI_KEY.takeIf { it.isNotBlank() } ?: ""
        if (apiKey.isBlank()) return MockNewsRepository()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://eventregistry.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(EventRegistryApi::class.java)
        return NewsApiRepository(
            apiKey = apiKey,
            api = api,
            catalogProvider = { catalog.getAllInterests() },
            followedIdsProvider = { interestsRepo.getFollowedInterestIds() },
            diskCache = ArticleDiskCache(this)
        )
    }

    private fun computeStartDestination(
        useSupabase: Boolean,
        authRepository: AuthRepository,
        model: NewsPulseModel
    ): String = when {
        useSupabase && authRepository.getCurrentUserId() == null -> "login"
        model.isOnboardingComplete() -> "home"
        useSupabase && authRepository.getCurrentUserId() != null -> "topicSelection"
        else -> "login"
    }
}
