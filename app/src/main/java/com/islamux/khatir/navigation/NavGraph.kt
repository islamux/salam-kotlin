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

/**
 * Declares every screen and wires screen-to-screen events to the navController.
 *
 * The important idea here: screens do NOT hold the navController. HomeScreen only
 * knows `onChapterClick`; NavGraph decides that this means "go to the reader with
 * that id". That is "hoisted" navigation — it keeps each screen reusable and
 * testable, because the screen can be rendered in a test with a fake lambda and
 * no navigation at all.
 *
 * @Composable for the same reason the screens are: the whole graph must be part
 * of one composition so Compose can recompose navigation state correctly.
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        // The screen shown when the app launches.
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
            // The arguments the READER pattern promised, with their types.
            // defaultValue supplies the value when the caller leaves it out.
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("initialPage") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            // Read the arguments back out of the entry. `?.` because arguments is
            // nullable; `?: return@composable` bails out of this screen if chapterId
            // is missing (a corrupt deep link) instead of crashing on a null id.
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
            val initialPage = backStackEntry.arguments?.getInt("initialPage") ?: 0
            ReaderScreen(
                chapterId = chapterId,
                initialPage = initialPage,
                // popBackStack() = what the system Back button does: drop the current
                // screen from the stack and reveal the previous one.
                onBackClick = { navController.popBackStack() },
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                // Search reuses the READER route, so a result can deep-link
                // straight to a specific chapter.
                onChapterClick = { chapterId ->
                    navController.navigate(Routes.readerRoute(chapterId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
