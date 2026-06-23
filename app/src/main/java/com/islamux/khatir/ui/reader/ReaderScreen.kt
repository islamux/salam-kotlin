package com.islamux.khatir.ui.reader

import android.content.Intent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamux.khatir.di.AppModule
import com.islamux.khatir.R
import com.islamux.khatir.data.model.Page
import com.islamux.khatir.data.static.AppStrings
import com.islamux.khatir.ui.theme.AmiriFontFamily
import com.islamux.khatir.ui.theme.AppColors
import com.islamux.khatir.ui.theme.ContentStyles
import kotlinx.coroutines.launch



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
    val useGoldenTitle = chapterId == "pre" || chapterId == "final"

    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, (uiState.pages.size - 1).coerceAtLeast(0)),
        pageCount = { uiState.pages.size.coerceAtLeast(1) }
    )

    LaunchedEffect(pagerState.currentPage) {
        viewModel.navigateToPage(pagerState.currentPage)
    }

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
                            viewModel.getShareText().let { text ->
                                if (text.isNotBlank()) {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    context.startActivity(
                                        Intent.createChooser(sendIntent, AppStrings.shareLabel)
                                    )
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
                            text = AppStrings.chapterTitle(chapterId),
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (useGoldenTitle) AppColors.golden else AppColors.golden
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
                        contentScale = ContentScale.Fit
                    )

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            reverseLayout = true,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { pageIndex ->
                            val page = uiState.pages.getOrNull(pageIndex)
                            if (page != null) {
                                PageContent(
                                    page = page,
                                    fontSize = uiState.fontSize
                                )
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
                                Slider(
                                    value = pagerState.currentPage.toFloat(),
                                    onValueChange = { target ->
                                        scope.launch {
                                            pagerState.scrollToPage(target.toInt())
                                        }
                                    },
                                    valueRange = 0f..(uiState.pages.size - 1).coerceAtLeast(0).toFloat(),
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = AppColors.black,
                                        inactiveTrackColor = AppColors.grey,
                                        thumbColor = AppColors.black
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${uiState.pages.size}",
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "${pagerState.currentPage + 1} / ${uiState.pages.size}",
                                fontFamily = AmiriFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PageContent(page: Page, fontSize: Float) {
    val scrollState = rememberScrollState()
    var titleIndex by remember { mutableIntStateOf(0) }
    var subtitleIndex by remember { mutableIntStateOf(0) }
    var textIndex by remember { mutableIntStateOf(0) }
    var ayahIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 60.dp)
    ) {
        for (field in page.order) {
            when (field) {
                "titles" -> {
                    if (titleIndex < page.titles.size) {
                        Text(
                            text = page.titles[titleIndex],
                            style = ContentStyles.title.copy(fontSize = (fontSize + 4).sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        titleIndex++
                    }
                }
                "subtitles" -> {
                    if (subtitleIndex < page.subtitles.size) {
                        Text(
                            text = page.subtitles[subtitleIndex],
                            style = ContentStyles.subtitle.copy(fontSize = (fontSize + 2).sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )
                        subtitleIndex++
                    }
                }
                "texts" -> {
                    if (textIndex < page.texts.size) {
                        Text(
                            text = page.texts[textIndex],
                            fontFamily = AmiriFontFamily,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.6).sp,
                            color = AppColors.black,
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        textIndex++
                    }
                }
                "ayahs" -> {
                    if (ayahIndex < page.ayahs.size) {
                        Text(
                            text = page.ayahs[ayahIndex],
                            style = ContentStyles.ayahHadith.copy(fontSize = (fontSize + 2).sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        ayahIndex++
                    }
                }
                "footer" -> {
                    page.footer?.let { footer ->
                        Text(
                            text = footer,
                            style = ContentStyles.footer.copy(fontSize = (fontSize - 4).sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
