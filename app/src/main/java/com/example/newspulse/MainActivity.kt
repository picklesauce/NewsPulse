package com.example.newspulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.data.OnboardingPreferences
import com.example.newspulse.data.ReadingHistoryPreferences
import com.example.newspulse.data.UserPreferences
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.data.mock.MockTopicsCatalogRepository
import com.example.newspulse.domain.Model
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.ViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.ArticleDetailScreen
import com.example.newspulse.ui.view.ArticleListScreen
import com.example.newspulse.ui.view.ExploreScreen
import com.example.newspulse.ui.view.FiltersScreen
import com.example.newspulse.ui.view.LoginScreen
import com.example.newspulse.ui.view.NewsPulseScaffold
import com.example.newspulse.ui.view.ProfileScreen
import com.example.newspulse.ui.view.SavedArticlesScreen
import com.example.newspulse.ui.view.TopicSelectionScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val model = Model(
            newsRepository = MockNewsRepository(),
            interestsRepository = OnboardingPreferences(this),
            userPreferencesRepository = UserPreferences(this),
            readingHistoryRepository = ReadingHistoryPreferences(this),
            savedArticlesRepository = InMemorySavedArticlesRepository(),
            topicsCatalogRepository = MockTopicsCatalogRepository()
        )
        val viewModelFactory = ViewModelFactory(model)

        setContent {
            NewsPulseTheme {
                CompositionLocalProvider(
                    CompositionLocals.LocalViewModelFactory provides viewModelFactory
                ) {
                    val navController = rememberNavController()
                    val startDestination = remember {
                        if (model.isOnboardingComplete()) "login" else "topicSelection"
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("topicSelection") {
                            TopicSelectionScreen(navController = navController)
                        }
                        composable("login") {
                            LoginScreen(navController = navController)
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
                        composable("articleDetail/{title}") { backStackEntry ->
                            val title = backStackEntry.arguments?.getString("title")
                            NewsPulseScaffold(navController = navController) {
                                ArticleDetailScreen(
                                    navController = navController,
                                    title = title
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
