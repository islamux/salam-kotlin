package com.islamux.khatir.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamux.khatir.di.AppModule
import com.islamux.khatir.R
import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.static.AppStrings
import com.islamux.khatir.ui.theme.AmiriFontFamily
import com.islamux.khatir.ui.theme.AppColors
import com.islamux.khatir.util.BackPressHandlerWithExitDialog
import com.islamux.khatir.util.ShareUtil
import com.islamux.khatir.util.WhatsAppUtil
import kotlinx.coroutines.launch

/**
 * The chapter list screen — the app's home.
 *
 * CANONICAL COMPOSE PATTERNS (explained here, reused by every other screen —
 * read this file first):
 *  - @Composable: a function that DESCRIBES UI instead of mutating views. Compose
 *    re-runs it (recomposition) whenever state it reads changes, so we read state
 *    and emit UI; nothing is imperatively updated.
 *  - State hoisting: this screen receives onChapterClick / onSearchClick lambdas
 *    from NavGraph rather than navigating itself. The screen stays reusable and
 *    testable, and navigation policy lives in exactly one place.
 *  - viewModel(factory = AppModule.provideHomeViewModelFactory(...)): the manual
 *    DI hookup (no Hilt — see di/AppModule.kt). LocalContext.current is the
 *    surrounding Android Context, needed here for the share intents.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onChapterClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = AppModule.provideHomeViewModelFactory(LocalContext.current)
    )
) {
    // Subscribes to the ViewModel's flow and turns every emission into Compose
    // state. `by` is a property delegate, so below we can write uiState.chapters
    // instead of the noisier uiState.value.chapters. Reading uiState here is
    // exactly what makes Compose recompose this screen when chapters arrive.
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    // rememberDrawerState keeps the drawer open/closed across recompositions —
    // without `remember` it would reset (snap shut) on every re-render.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // Coroutines started in `scope` are cancelled when this composable leaves the
    // composition — used to open and close the drawer, both suspend calls.
    val scope = rememberCoroutineScope()

    // Intercepts the system Back button and asks for confirmation before leaving
    // the app (see util/AlertExitDialog.kt).
    BackPressHandlerWithExitDialog()

    // The side drawer: `drawerContent` slides in over the main content declared
    // in the trailing lambda below.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = AppColors.golden
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Drawer entries. `clickable` makes ANY composable tappable — here a
                // Row of icon + label that calls into util/.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { WhatsAppUtil.openChat(context) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = AppColors.black
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = AppStrings.drawerContactUs,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            ShareUtil.shareText(context, AppStrings.homeShareText, AppStrings.shareLabel)
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = AppColors.black
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = AppStrings.drawerShareApp,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = AppStrings.homeAppBarTitle,
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                    Icon(
                            Icons.Default.Menu,
                            contentDescription = AppStrings.menuLabel,
                            tint = AppColors.golden
                        )
                        }
                    },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = AppStrings.searchLabel,
                                tint = AppColors.golden
                            )
                        }
                        TextButton(onClick = {
                            ShareUtil.shareText(context, AppStrings.homeShareText, AppStrings.shareLabel)
                        }) {
                            Text(
                                text = AppStrings.homeShareButton,
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = AppColors.golden
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppColors.black,
                        titleContentColor = AppColors.golden
                    )
                )
            }
        ) { padding ->
            // The screen has exactly three states, chosen purely from the
            // ViewModel's state object. Error is checked FIRST so a failure is
            // never hidden behind a loading spinner, then loading, then content.
            if (uiState.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            // `!!` asserts "not null" and throws otherwise. It is safe
                            // HERE only because the enclosing `if (uiState.error != null)`
                            // branch already proved it; elsewhere prefer `?.let`.
                            text = uiState.error!!,
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppColors.golden
                        )
                    }
                } else if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Image(
                        painter = painterResource(R.drawable.bg_home),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = AppColors.golden,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            text = AppStrings.homeScrollHint,
                            fontFamily = AmiriFontFamily,
                            fontSize = 18.sp,
                            color = AppColors.golden
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 120.dp, bottom = 130.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .heightIn(max = 480.dp),
                            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // One entry per chapter. The lambda given to
                                // ChapterButton is the hoisted event: the button
                                // reports the click upward, it does not navigate.
                                uiState.chapters.forEach { chapter ->
                                    ChapterButton(
                                        chapter = chapter,
                                        onClick = { onChapterClick(chapter.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * One tappable chapter entry in the list.
 *
 * Stateless by design: it receives a chapter and an onClick lambda and owns no
 * state of its own, so the same composable could be rendered anywhere — another
 * screen, a Compose preview, a test — without dragging dependencies along.
 */
@Composable
fun ChapterButton(chapter: Chapter, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.golden,
            contentColor = AppColors.black
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = AppColors.black
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                                text = AppStrings.chapterTitle(chapter.id),
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }
    }
}
