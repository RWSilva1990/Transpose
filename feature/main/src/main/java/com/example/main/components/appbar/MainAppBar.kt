package com.example.main.components.appbar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.main.R
import com.example.ui.components.items.SearchSuggestionItem
import com.example.util.ToastUtil
import com.example.util.constants.AppColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(
    onSettingClicked: () -> Unit,
    onSearchClicked: (String) -> Unit,
    searchBarState: SearchBarState,
    updateSearchBarState: (SearchBarState) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    isOnLocalFilesScreen: Boolean = false,
    isLocalSearchActive: Boolean,
    localSearchQuery: String,
    searchQuery: String,
    suggestionKeywords: List<String>,
    onUpdateLocalSearchQuery: (String) -> Unit,
    onSetLocalSearchActive: (Boolean) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onClearSearchQuery: () -> Unit,
) {

    val focusRequester = remember { FocusRequester() }

    Box {
        when {
            isLocalSearchActive && isOnLocalFilesScreen -> {
                LocalSearchAppBar(
                    query = localSearchQuery,
                    onQueryChange = onUpdateLocalSearchQuery,
                    onClose = { onSetLocalSearchActive(false) },
                    focusRequester = focusRequester
                )
            }
            searchBarState == SearchBarState.OPENED -> {
                CustomSearchAppBar(
                    searchQuery = searchQuery,
                    suggestionKeywords = suggestionKeywords,
                    onUpdateSearchQuery = onUpdateSearchQuery,
                    onClearSearchQuery = onClearSearchQuery,
                    updateSearchBarState = updateSearchBarState,
                    onSearchClicked = { onSearchClicked(it) },
                    focusRequester = focusRequester,
                )
            }
            else -> {
                DefaultAppBar(
                    onSettingClicked = { onSettingClicked() },
                    onSearchClicked = { updateSearchBarState(SearchBarState.OPENED) },
                    onLocalSearchClicked = { onSetLocalSearchActive(true) },
                    scrollBehavior = scrollBehavior,
                    showLocalSearchIcon = isOnLocalFilesScreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalSearchAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester
) {
    SideEffect {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.DarkGray
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = androidx.compose.ui.Alignment.CenterStart
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.local_search_hint),
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.DarkGray
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        HorizontalDivider(
            color = Color.LightGray,
            thickness = 1.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchAppBar(
    searchQuery: String,
    suggestionKeywords: List<String>,
    onUpdateSearchQuery: (String) -> Unit,
    onClearSearchQuery: () -> Unit,
    updateSearchBarState: (SearchBarState) -> Unit,
    onSearchClicked: (String) -> Unit,
    focusRequester: FocusRequester,
) {

    val context = LocalContext.current

    SideEffect {
        focusRequester.requestFocus()
    }

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        query = searchQuery,
        onQueryChange = onUpdateSearchQuery,
        onSearch = {
            onSearchClicked(it)
            onClearSearchQuery()
        },
        active = true,
        placeholder = { Text(text = stringResource(id = R.string.searchView_hint)) },
        leadingIcon = {
            IconButton(onClick = {
                updateSearchBarState(SearchBarState.CLOSED)
                onClearSearchQuery()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        trailingIcon = {
            if (searchQuery.isEmpty()) {
                IconButton(onClick = { ToastUtil.showNotImplemented(context = context) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            } else {
                IconButton(onClick = {
                    onClearSearchQuery()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Text")
                }
            }

        },
        shape = SearchBarDefaults.dockedShape,
        colors = SearchBarDefaults.colors(
            containerColor = Color.White,
            dividerColor = Color.Black,
        ),
        onActiveChange = { isActive ->
            if (!isActive) {
                updateSearchBarState(SearchBarState.CLOSED)
            }
        },
        content = {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(suggestionKeywords.size) { index ->
                    val suggestionKeyword = suggestionKeywords[index]
                    SearchSuggestionItem(
                        suggestionText = suggestionKeyword,
                        onClick = {
                            onSearchClicked(suggestionKeyword)
                            onClearSearchQuery()
                        },
                    )
                }

            }
            BackHandler {
                onClearSearchQuery()
                updateSearchBarState(SearchBarState.CLOSED)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAppBar(
    onSettingClicked: () -> Unit,
    onSearchClicked: () -> Unit,
    onLocalSearchClicked: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior,
    showLocalSearchIcon: Boolean = false
) {

    TopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppColors.BlueBackground,
            scrolledContainerColor = AppColors.BlueBackground,
            titleContentColor = Color.White
        ),
        title = {
            Text(
                "Transpose",
                maxLines = 1,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            Row {
                Spacer(modifier = Modifier.size(25.dp))
                Image(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = R.mipmap.transpose_app_icon),
                    contentDescription = "app icon"
                )
                Spacer(modifier = Modifier.size(15.dp))

            }

        },
        actions = {
            if (showLocalSearchIcon) {
                IconButton(onClick = { onLocalSearchClicked() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search_list),
                        contentDescription = "Local Search",
                        tint = Color.White
                    )
                }
            }
            IconButton(onClick = { onSearchClicked() }) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
            IconButton(onClick = { onSettingClicked() }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Setting",
                    tint = Color.White
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )

}
