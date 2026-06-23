package com.islamux.khatir.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamux.khatir.di.AppModule
import com.islamux.khatir.data.static.AppStrings
import com.islamux.khatir.ui.theme.AmiriFontFamily
import com.islamux.khatir.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onChapterClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = viewModel(
        factory = AppModule.provideSearchViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppStrings.searchLabel, fontFamily = AmiriFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = AppStrings.backLabel,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.search(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(AppStrings.searchHint, fontFamily = AmiriFontFamily) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.query.isBlank() -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = AppStrings.searchPrompt,
                        fontFamily = AmiriFontFamily,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                uiState.isSearching -> {
                    Text(AppStrings.searchSearching, fontFamily = AmiriFontFamily)
                }
                uiState.results.isEmpty() -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = AppStrings.searchNoResultsFound,
                        fontFamily = AmiriFontFamily,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                else -> {
                    Text(
                        text = AppStrings.resultCount(uiState.results.size),
                        fontFamily = AmiriFontFamily,
                        fontSize = 14.sp,
                        color = AppColors.grey
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(uiState.results) { result ->
                            SearchResultItem(
                                matchedText = result.matchedText,
                                subtitle = "${result.chapter.title} - ${fieldLabel(result.matchedField)}",
                                onClick = { onChapterClick(result.chapter.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    matchedText: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = matchedText,
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AppColors.black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontFamily = AmiriFontFamily,
                fontSize = 12.sp,
                color = AppColors.grey
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = AppColors.grey,
            modifier = Modifier.width(16.dp)
        )
    }
}
