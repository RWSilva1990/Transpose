package com.example.main.components.bottom_navigation

import androidx.annotation.DrawableRes
import com.example.main.R

sealed class MainTab(
    val route: String,
    val label: String,
    @DrawableRes val filledIcon: Int,
    @DrawableRes val outlinedIcon: Int
) {
    data object Home : MainTab(
        route = "home",
        label = "Home",
        filledIcon = R.drawable.baseline_home_24,
        outlinedIcon = R.drawable.outline_home_24
    )

    data object Convert : MainTab(
        route = "convert",
        label = "Convert",
        filledIcon = R.drawable.baseline_album_24,
        outlinedIcon = R.drawable.outline_album_24
    )

    data object Library : MainTab(
        route = "library",
        label = "Library",
        filledIcon = R.drawable.baseline_library_music_24_blue,
        outlinedIcon = R.drawable.outline_library_music_24
    )

    companion object {
        val ALL_TABS: List<MainTab> by lazy { listOf(Home, Convert, Library) }
    }
}
