package io.github.janmalch.simplerssreader.ui.screens.main

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.github.janmalch.simplerssreader.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun MoreMenu(
    onGoToManageSources: () -> Unit,
    onGoToLicenses: () -> Unit,
    onGoToLogs: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.open_menu)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.manage_feeds)) },
                onClick = {
                    onGoToManageSources()
                    expanded = false
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermissionState = rememberPermissionState(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                if (!notificationPermissionState.status.isGranted) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.grant_permissions)) },
                        onClick = {
                            notificationPermissionState.launchPermissionRequest()
                            expanded = false
                        }
                    )
                }
            }
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.view_licenses)) },
                onClick = {
                    onGoToLicenses()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.view_logs)) },
                onClick = {
                    onGoToLogs()
                    expanded = false
                }
            )
        }
    }
}