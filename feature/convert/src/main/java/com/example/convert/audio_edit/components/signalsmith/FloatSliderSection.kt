package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.util.constants.AppColors

@Composable
fun FloatSliderSection(
    title: String,
    displayValueText: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onReset: () -> Unit,
    currentValue: Float,
    valueRange: ClosedFloatingPointRange<Float>
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 15.dp, bottom = 8.dp)
    ) {
        val (titleText, valueText, resetButton, slider) = createRefs()

        Text(
            text = title,
            fontSize = 14.sp,
            color = AppColors.BlueBackground,
            modifier = Modifier.constrainAs(titleText) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(slider.top)
            }
        )

        Text(
            text = displayValueText,
            fontSize = 14.sp,
            color = AppColors.BlueBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(valueText) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(slider.top)
                }
                .background(Color.Transparent)
        )

        IconButton(
            onClick = onReset,
            modifier = Modifier.constrainAs(resetButton) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(slider.top)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset $title",
                tint = AppColors.BlueBackground
            )
        }

        Slider(
            value = currentValue,
            valueRange = valueRange,
            onValueChange = { onValueChange(it) },
            onValueChangeFinished = { onValueChangeFinished() },
            colors = SliderDefaults.colors(
                thumbColor = AppColors.StatusBarBackground,
                activeTrackColor = AppColors.StatusBarBackground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(slider) {
                    top.linkTo(titleText.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
        )
    }
}
