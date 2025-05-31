package com.example.main.components.bottom_navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.main.MainUiStateViewModel
import com.example.main.components.appbar.SearchBarState
import com.example.util.constants.AppColors

@Composable
fun BottomNavigationBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    mainUiStateViewModel: MainUiStateViewModel
) {

    val bottomSheetOffset by mainUiStateViewModel.bottomSheetOffset.collectAsState()
    val searchBarState by mainUiStateViewModel.searchBarState.collectAsState()

    val totalOffset = remember(bottomSheetOffset, searchBarState) {
        derivedStateOf {
            // BottomSheet에 의한 오프셋 계산
            val normalizedBottomSheetOffset = when {
                bottomSheetOffset <= 0f -> 0.dp
                bottomSheetOffset >= 1f -> 56.dp
                else -> (bottomSheetOffset * 56).dp
            }

            // SearchBar 상태에 의한 오프셋
            val searchBarOffset = if (searchBarState == SearchBarState.OPENED) 56.dp else 0.dp

            normalizedBottomSheetOffset + searchBarOffset
        }
    }

    val mainTabs = remember { MainTab.ALL_TABS }
    BottomNavigation(
        modifier = Modifier
            .navigationBarsPadding()
            .offset(y = totalOffset.value),
        backgroundColor = AppColors.BlueBackground

    ) {


        mainTabs.forEach { tab ->

            val isSelected = (tab == selectedTab)

            BottomNavigationItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (isSelected) tab.filledIcon else tab.outlinedIcon
                        ),
                        contentDescription = tab.label,
                        tint = Color.White
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

    }
}

