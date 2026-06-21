package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer


@Composable
fun RelatedVideoShimmerItem() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .shimmer(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(132.dp)
                    .height(76.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.74f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(9.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
            }

            Box(
                modifier = Modifier.width(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.LightGray)
                )
            }
        }
    }
}
