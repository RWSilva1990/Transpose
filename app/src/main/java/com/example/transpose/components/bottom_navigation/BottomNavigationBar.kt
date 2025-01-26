package com.example.transpose.components.bottom_navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.example.transpose.components.appbar.SearchWidgetState
import com.example.transpose.utils.constants.AppColors
import com.example.ui.components.bottom_navigation.MainTab

@Composable
fun BottomNavigationBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    searchWidgetState: SearchWidgetState,
    normalizedOffset: Float
) {

    // BottomSheet에 의한 오프셋 계산
    val bottomSheetOffset = lerp(
        start = 0.dp, stop = 56.dp, fraction = normalizedOffset.coerceIn(0f, 1f)
    )

    // SearchBar 상태에 따른 오프셋 계산
    val searchBarOffset = if (searchWidgetState == SearchWidgetState.OPENED) {
        56.dp
    } else {
        0.dp
    }

    val totalOffset = bottomSheetOffset + searchBarOffset


    BottomNavigation(
        modifier = Modifier
            .navigationBarsPadding()
            .offset(y = totalOffset),
        backgroundColor = AppColors.BlueBackground

    ) {
        MainTab.ALL_TABS.forEach { tab ->
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

