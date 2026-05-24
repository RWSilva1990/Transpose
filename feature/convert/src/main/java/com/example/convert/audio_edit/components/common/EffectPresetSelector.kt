package com.example.convert.audio_edit.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data class for preset items
 */
data class PresetItem(
    val name: String,
    val description: String
)

/**
 * Reusable preset selector component with effect description
 */
@Composable
fun EffectPresetSelector(
    modifier: Modifier = Modifier,
    description: String,
    presets: List<PresetItem>,
    selectedIndex: Int = -1,
    onPresetSelected: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Effect description
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Preset chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEachIndexed { index, preset ->
                EffectPresetChip(
                    label = preset.name,
                    isSelected = index == selectedIndex,
                    onClick = { onPresetSelected(index) }
                )
            }
        }
    }
}

@Composable
fun EffectPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Compact version with just chips (no description header)
 */
@Composable
fun EffectPresetChips(
    modifier: Modifier = Modifier,
    presets: List<String>,
    selectedIndex: Int = -1,
    onPresetSelected: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        presets.forEachIndexed { index, presetName ->
            EffectPresetChip(
                label = presetName,
                isSelected = index == selectedIndex,
                onClick = { onPresetSelected(index) }
            )
        }
    }
}
