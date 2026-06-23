package com.islamux.khatir.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.islamux.khatir.ui.home.HomeScreen
import com.islamux.khatir.ui.reader.ReaderScreen
import com.islamux.khatir.ui.search.SearchScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onChapterClick = { chapterId ->
                    navController.navigate(Routes.readerRoute(chapterId))
                },
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
                }
            )
        }

        composable(
            route = Routes.READER,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("initialPage") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
            val initialPage = backStackEntry.arguments?.getInt("initialPage") ?: 0
            ReaderScreen(
                chapterId = chapterId,
                initialPage = initialPage,
                onBackClick = { navController.popBackStack() },
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onChapterClick = { chapterId ->
                    navController.navigate(Routes.readerRoute(chapterId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
