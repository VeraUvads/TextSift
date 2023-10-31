@file:OptIn(ExperimentalMaterial3Api::class)

package com.uva.metaquotestest.feature

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uva.metaquotestest.AppContainer
import com.uva.metaquotestest.R
import com.uva.metaquotestest.utils.observeWith
import com.uva.metaquotestest.utils.viewModelFactory

@Composable
fun UploadScreen(
    viewModel: UploadingViewModel = viewModel(
        factory = viewModelFactory {
            UploadingViewModel(
                repository = AppContainer.repository,
                resourceProvider = AppContainer.resourceProvider,
            )
        },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.showUploadUI) {
        UploadContent(state) { url, filter -> viewModel.downloadAndMatch(url, filter) }
    } else {
        ResultContent(state) { viewModel.onCopyClick() }
    }
    val context = LocalContext.current

    viewModel.effect.observeWith { effect ->
        when (effect) {
            is UploadingScreenEffect.ShowToast -> {
                Toast.makeText(context, effect.text, Toast.LENGTH_LONG).show()
            }
        }
    }

    BackHandler(!state.showUploadUI) {
        viewModel.showUploadUI()
    }
}

@Composable
fun ResultContent(state: UploadingScreenState, onCopyClick: () -> Unit) {
    val selectedItems = rememberSaveable { mutableMapOf<Int, String>() }
    val clipboardManager = LocalClipboardManager.current
    LazyColumn {
        item {
            Row {
                Text(
                    text = stringResource(R.string.result),
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.Center,
                )

                Image(
                    painter = painterResource(id = R.drawable.baseline_content_copy_24),
                    contentDescription = "copy icon",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            val result = selectedItems.values.joinToString("\n")
                            clipboardManager.setText(AnnotatedString(result))
                            onCopyClick()
                        },
                )
            }
        }
        itemsIndexed(state.result) { index, value ->
            key(index) {
                var isSelected by rememberSaveable { mutableStateOf(false) }
                Box(
                    modifier = Modifier,
                ) {
                    Text(
                        text = value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedItems.remove(index)
                                } else {
                                    selectedItems[index] = value
                                }
                                isSelected = !isSelected
                            },
                        color = if (isSelected) Gray else Black,
                    )
                }
            }
        }
    }
}

@Composable
fun UploadContent(state: UploadingScreenState, downloadAndMatch: (String, String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var url by rememberSaveable { mutableStateOf("") }
        TextField(
            value = url,
            onValueChange = { url = it },
            placeholder = {
                Text(stringResource(R.string.enter_url))
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        var filter by rememberSaveable { mutableStateOf("") }
        TextField(
            value = filter,
            onValueChange = { filter = it },
            placeholder = {
                Text(stringResource(R.string.enter_filter))
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                downloadAndMatch(url, filter)
            },
            enabled = !state.isLoading && url.isNotEmpty(),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(R.string.download_and_match))
            }
        }
    }
}
