/*
 * MainComposable.kt
 *
 * Copyright 2026 Yasuhiro Yamakawa <withlet11@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.withlet11.clockwithplanispherelite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isClockHandsVisible: Boolean,
    onClockHandsVisibleChanged: (Boolean) -> Unit,
    isSouthernSky: Boolean,
    onSouthernSkyChanged: (Boolean) -> Unit,
    latitudeText: String,
    onLatitudeChanged: (String) -> Unit,
    longitudeText: String,
    onLongitudeChanged: (String) -> Unit,
    isLocationFieldsEnabled: Boolean,
    onApplyLocation: () -> Unit,
    onGetLocation: () -> Unit,
    statusText: String,
    onLicensesClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onCreditsClick: () -> Unit,
    onFinish: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CwpTopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.license)) },
                            onClick = {
                                menuExpanded = false
                                onLicensesClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.privacy_policy_header)) },
                            onClick = {
                                menuExpanded = false
                                onPrivacyPolicyClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.opensource_licenses)) },
                            onClick = {
                                menuExpanded = false
                                onCreditsClick()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.attention),
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Text(
                text = stringResource(id = R.string.display_control),
                style = MaterialTheme.typography.titleLarge
            )

            // Display Control Table
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.clock_hands_visibility))
                    Switch(
                        checked = isClockHandsVisible,
                        onCheckedChange = onClockHandsVisibleChanged
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.celestial_hemisphere))
                    Box {
                        Button(
                            onClick = {
                                expanded = true
                            }
                        ) {
                            Text(
                                if (isSouthernSky) stringResource(id = R.string.south_full_label) else stringResource(
                                    id = R.string.north_full_label
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.north_full_label)) },
                                onClick = {
                                    expanded = false
                                    onSouthernSkyChanged(false)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.south_full_label)) },
                                onClick = {
                                    expanded = false
                                    onSouthernSkyChanged(true)
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Text(
                text = stringResource(id = R.string.locationSettings),
                style = MaterialTheme.typography.titleLarge
            )

            // Location Settings Table / Inputs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.latitude),
                    )
                    TextField(
                        value = latitudeText,
                        onValueChange = onLatitudeChanged,
                        modifier = Modifier.width(140.dp),
                        enabled = isLocationFieldsEnabled,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.longitude),
                    )
                    TextField(
                        value = longitudeText,
                        onValueChange = onLongitudeChanged,
                        modifier = Modifier.width(140.dp),
                        enabled = isLocationFieldsEnabled,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onGetLocation,
                    enabled = isLocationFieldsEnabled,
                ) {
                    Text(stringResource(id = R.string.gps))
                }
                Button(
                    onClick = onApplyLocation,
                    enabled = isLocationFieldsEnabled
                ) {
                    Text(stringResource(id = R.string.modify_location))
                }
            }

            if (statusText.isNotEmpty()) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    CwpTheme {
        MainScreen(
            isClockHandsVisible = true,
            onClockHandsVisibleChanged = {},
            isSouthernSky = false,
            onSouthernSkyChanged = {},
            latitudeText = "+35.6895",
            onLatitudeChanged = {},
            longitudeText = "+139.6917",
            onLongitudeChanged = {},
            isLocationFieldsEnabled = true,
            onApplyLocation = {},
            onGetLocation = {},
            statusText = "",
            onLicensesClick = {},
            onPrivacyPolicyClick = {},
            onCreditsClick = {},
            onFinish = {}
        )
    }
}
