@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.model.preferences.VideoQuality
import com.example.transpose.core.ui.R
import com.example.ui.util.getDisplayString
import com.example.ui.util.getDisplayStringResId
import com.example.util.ToastUtil

@Composable
fun SettingsScreen(
    onUpdateCheckClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentQuality by viewModel.videoQuality.collectAsStateWithLifecycle()
    var showQualityDialog by remember { mutableStateOf(false) }

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = stringResource(R.string.settings_playback)) {
                SettingsItem(
                    title = stringResource(R.string.settings_default_resolution),
                    subtitle = currentQuality.getDisplayString(context),
                    onClick = { showQualityDialog = true }
                )
            }

            HorizontalDivider(
                thickness = 8.dp,
                color = Color(0xFFF5F5F5)
            )

            SettingsSection(title = stringResource(R.string.settings_app)) {
                SettingsItem(
                    title = stringResource(R.string.settings_app_design),
                    subtitle = stringResource(R.string.settings_light_dark_mode),
                    onClick = { ToastUtil.showNotImplemented(context) }
                )
            }

            HorizontalDivider(
                thickness = 8.dp,
                color = Color(0xFFF5F5F5)
            )

            SettingsSection(title = stringResource(R.string.settings_info)) {
                SettingsItem(
                    title = stringResource(R.string.settings_check_update),
                    onClick = onUpdateCheckClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )

                SettingsItem(
                    title = stringResource(R.string.settings_contact),
                    onClick = onContactClick
                )
            }
        }
    }

    if (showQualityDialog) {
        VideoQualityDialog(
            currentQuality = currentQuality,
            onQualitySelected = { quality ->
                viewModel.setVideoQuality(quality)
                showQualityDialog = false
            },
            onDismiss = { showQualityDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCCCCCC)
        )
    }
}

@Composable
fun VideoQualityDialog(
    currentQuality: VideoQuality,
    onQualitySelected: (VideoQuality) -> Unit,
    onDismiss: () -> Unit
) {
    val qualityOptions = VideoQuality.selectableQualities
    var selectedQuality by remember { mutableStateOf(currentQuality) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.video_quality_dialog_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                qualityOptions.forEach { quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (quality == selectedQuality),
                                onClick = { selectedQuality = quality },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (quality == selectedQuality),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(quality.getDisplayStringResId()),
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onQualitySelected(selectedQuality) }
            ) {
                Text(stringResource(R.string.confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}