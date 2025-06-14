package com.example.main.components.appbar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.main.MainViewModel
import com.example.main.R
import com.example.ui.components.items.SearchSuggestionItem
import com.example.util.Logger
import com.example.util.ToastUtil
import com.example.util.constants.AppColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(
    onSearchClicked: (String) -> Unit,
    mainViewModel: MainViewModel,
    updateSearchBarState: (SearchBarState) -> Unit,
    searchBarState: SearchBarState,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    val focusRequester = remember { FocusRequester() }


    Box {
        when (searchBarState) {
            SearchBarState.CLOSED -> {
                DefaultAppBar(
                    onSearchClicked = { updateSearchBarState(SearchBarState.OPENED) },
                    scrollBehavior = scrollBehavior
                )
            }

            SearchBarState.OPENED -> {
                CustomSearchAppBar(
                    mainViewModel = mainViewModel,
                    updateSearchBarState = updateSearchBarState,
                    onSearchClicked = { onSearchClicked(it) },
                    focusRequester = focusRequester,
                )
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchAppBar(
    mainViewModel: MainViewModel,
    updateSearchBarState: (SearchBarState) -> Unit,
    onSearchClicked: (String) -> Unit,
    focusRequester: FocusRequester,
) {

    val suggestionKeywords by mainViewModel.suggestionKeywords.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    SideEffect {
        focusRequester.requestFocus()
    }

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        query = searchQuery,
        onQueryChange = {
            searchQuery = it
            mainViewModel.setSuggestionKeywords(it)
        },
        onSearch = {
            onSearchClicked(it)
            searchQuery = ""
            mainViewModel.clearSuggestionKeywords()
        },
        active = true,
        placeholder = { Text(text = stringResource(id = R.string.searchView_hint)) },
        leadingIcon = {
            IconButton(onClick = {
                updateSearchBarState(SearchBarState.CLOSED)
                mainViewModel.clearSuggestionKeywords()
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
                    searchQuery = ""
                    mainViewModel.clearSuggestionKeywords()
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
//                mainUiStateViewModel.onCloseSearchBar()
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
                        },
                    )
                }

            }
            BackHandler {
                Logger.d("CustomSearchAppBar BackHandler")
                searchQuery = ""
                mainViewModel.clearSuggestionKeywords()
                updateSearchBarState(SearchBarState.CLOSED)
//                mainUiStateViewModel.onCloseSearchBar()
            }

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAppBar(
    onSearchClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {

    val context = LocalContext.current
    TopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppColors.BlueBackground,
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
            IconButton(onClick = { onSearchClicked() }) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )

            }
            IconButton(onClick = { ToastUtil.showNotImplemented(context) }) {
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

