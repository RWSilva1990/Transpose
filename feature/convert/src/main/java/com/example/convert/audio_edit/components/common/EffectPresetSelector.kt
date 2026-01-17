package com.example.convert.audio_edit.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEachIndexed { index, preset ->
                PresetChip(
                    name = preset.name,
                    description = preset.description,
                    isSelected = index == selectedIndex,
                    onClick = { onPresetSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    name: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color(0xFFF5F5F5)
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        Color.DarkGray
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color(0xFFE0E0E0)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 10.sp,
            color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 12.sp
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEachIndexed { index, presetName ->
            SimplePresetChip(
                name = presetName,
                isSelected = index == selectedIndex,
                onClick = { onPresetSelected(index) }
            )
        }
    }
}

@Composable
private fun SimplePresetChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color(0xFFF0F0F0)
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        Color.DarkGray
    }

    Text(
        text = name,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
