package com.example.newspulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.view.ArticleDetailScreen
import com.example.newspulse.view.ArticleListScreen
import com.example.newspulse.view.FiltersScreen
import com.example.newspulse.view.LoginScreen
import com.example.newspulse.view.SavedArticlesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NewsPulseTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(navController = navController)
                    }
                    composable("articles") {
                        ArticleListScreen(navController = navController)
                    }
                    composable("filters") {
                        FiltersScreen(navController = navController)
                    }
                    composable("savedArticles") {
                        SavedArticlesScreen(navController = navController)
                    }
                    composable("articleDetail/{title}") { backStackEntry ->
                        val title = backStackEntry.arguments?.getString("title")
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