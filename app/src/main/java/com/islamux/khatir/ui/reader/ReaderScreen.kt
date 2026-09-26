package com.islamux.khatir.ui.reader


import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.islamux.khatir.data.static.AppStrings
import com.islamux.khatir.ui.theme.AmiriFontFamily
import com.islamux.khatir.ui.theme.AppColors
import com.islamux.khatir.ui.reader.components.PageContent
import com.islamux.khatir.ui.reader.components.ShinyBlackThumb
import com.islamux.khatir.util.ShareUtil
import kotlinx.coroutines.launch



/**
 * The reading screen: one chapter, swipeable pages, font controls, share.
 *
 * The MVVM wiring is identical to HomeScreen (read HomeScreen.kt and
 * HomeViewModel.kt first — the @Composable, collectAsState and hoisting comments
 * there cover this file too). What is new here:
 *  - HorizontalPager: a built-in swipeable page container driven by pagerState.
 *  - key(chapterId) on the viewModel call: a SEPARATE ViewModel per chapter.
 *    Navigating from reader/A to reader/B reuses this composable, so without the
 *    key the second chapter would inherit the first chapter's state.
 *  - LaunchedEffect: the bridge for one-shot suspend side effects (see below).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterId: String,
    initialPage: Int = 0,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: ReaderViewModel = viewModel(
        key = chapterId,
        factory = AppModule.provideReaderViewModelFactory(LocalContext.current, chapterId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        // coerceIn clamps the requested page into the valid range, and
        // coerceAtLeast(0) handles the moment before any page has loaded, when
        // pages.size - 1 would be -1 and the range would be invalid.
        initialPage = initialPage.coerceIn(0, (uiState.pages.size - 1).coerceAtLeast(0)),
        // At least one page keeps the pager valid while loading; the real count
        // arrives with the data and the pager updates itself.
        pageCount = { uiState.pages.size.coerceAtLeast(1) }
    )

    // LaunchedEffect(key) { }: runs its suspend body when it ENTERS composition,
    // re-runs when the key changes, and CANCELS it when it leaves. That makes it
    // the right tool for a one-shot effect like "tell the ViewModel which page is
    // showing". It has to be an effect and not part of the composable BODY,
    // because a body is re-run on every recomposition — `LaunchedEffect` is the
    // sanctioned place for a side effect that should happen once.

    // Keeps the ViewModel's currentPageIndex in step with the pager. The key is
    // the page being shown, so this fires once per swipe.
    LaunchedEffect(pagerState.currentPage) {
        viewModel.navigateToPage(pagerState.currentPage)
    }

    // Scrolls to the requested page once, AFTER loading finishes. The key is
    // isLoading, so this body runs on the transition to "loaded" and then stops —
    // it must not fight the user's own swiping on every recomposition.
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.pages.isNotEmpty() && initialPage > 0) {
            pagerState.scrollToPage(
                initialPage.coerceIn(0, uiState.pages.size - 1)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            // Share asks the ViewModel for the text (the logic lives
                            // there) and only opens the sheet when there is something
                            // to share — an empty string means the pages never loaded.
                            viewModel.getShareText().let { text ->
                                if (text.isNotBlank()) {
                                    ShareUtil.shareText(context, text, AppStrings.shareLabel)
                                }
                            }
                        }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = AppStrings.shareLabel,
                                tint = AppColors.golden
                            )
                        }
                        Text(
                            text = AppStrings.topBarTitle(chapterId),
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.golden
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = AppStrings.backLabel,
                            tint = AppColors.golden
                        )
                    }
                },
                actions = {
                    // Font A- / A+ buttons: they only CALL the ViewModel, which owns
                    // the 21f..37f bounds — no limit logic in the UI.
                    IconButton(onClick = { viewModel.decreaseFontSize() }) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = AppStrings.decreaseFontLabel,
                            tint = AppColors.golden
                        )
                    }
                    Text(
                        text = AppStrings.fontLabel,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.golden
                    )
                    IconButton(onClick = { viewModel.increaseFontSize() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = AppStrings.increaseFontLabel,
                            tint = AppColors.golden
                        )
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = AppStrings.searchLabel,
                            tint = AppColors.golden
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
        // Four states this time (HomeScreen had three): loading, error, an empty
        // chapter, and the reader itself. `when` without a subject picks the first
        // matching branch, so the order is the priority order.
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: AppStrings.unknownError,
                        fontFamily = AmiriFontFamily,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            uiState.pages.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.noContent,
                        fontFamily = AmiriFontFamily
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Image(
                        painter = painterResource(R.drawable.bg_reader),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { pageIndex ->
                            val page = uiState.pages.getOrNull(pageIndex)
                            if (page != null) {
                                // key(fontSize) forces PageContent to be BUILT AGAIN
                                // whenever the font size changes. That matters because
                                // PageContent keeps `remember`ed per-field indices
                                // internally: reusing the instance would resume those
                                // counters mid-list and the page would render with
                                // elements missing or repeated.
                                key(uiState.fontSize) {
                                    PageContent(
                                        page = page,
                                        fontSize = uiState.fontSize
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1",
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 16.sp
                                )
                                val interactionSource = remember { MutableInteractionSource() }
                                Slider(
                                    value = pagerState.currentPage.toFloat(),
                                    onValueChange = { target ->
                                        // scrollToPage is a SUSPEND function (it
                                        // animates), so the slider's non-suspend
                                        // callback has to launch it on `scope`.
                                        scope.launch {
                                            pagerState.scrollToPage(target.toInt())
                                        }
                                    },
                                    // coerceAtLeast(0) keeps the range valid before
                                    // the pages arrive, when size - 1 would be -1.
                                    valueRange = 0f..(uiState.pages.size - 1).coerceAtLeast(0).toFloat(),
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = AppColors.black,
                                        inactiveTrackColor = AppColors.grey,
                                        thumbColor = AppColors.black
                                    ),
                                    // The SAME interactionSource is handed to the
                                    // Slider and to our custom thumb, which is how
                                    // the thumb learns it is being pressed.
                                    interactionSource = interactionSource,
                                    thumb = { ShinyBlackThumb(interactionSource) },
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${uiState.pages.size}",
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
