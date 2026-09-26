package com.islamux.khatir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.islamux.khatir.navigation.NavGraph
import com.islamux.khatir.ui.theme.KhatirTheme

/**
 * The app's only Activity (single-Activity architecture).
 *
 * Android starts here right after [KhatirApp]. Everything the user sees from
 * now on is Jetpack Compose UI swapped inside this one Activity by
 * navigation/NavGraph.kt — there are no other Activities in this project.
 *
 * Recommended reading path from here:
 *   setContent -> KhatirTheme (ui/theme/Theme.kt) -> NavGraph (navigation/NavGraph.kt)
 *   -> HomeScreen (ui/home/HomeScreen.kt).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw the app behind the system status/navigation bars (no colored strips).
        enableEdgeToEdge()
        // Replaces XML layouts: this lambda builds the whole UI in Kotlin.
        setContent {
            KhatirTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // The navigation brain: it owns the screen back stack.
                    // `remember*` = the value survives recomposition (see HomeScreen
                    // for why that matters); creating it inside setContent ties its
                    // lifetime to this composition.
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
