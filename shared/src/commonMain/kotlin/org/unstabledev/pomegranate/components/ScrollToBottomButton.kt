package org.unstabledev.pomegranate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch

@Composable
fun ScrollToBottomButton(listState: LazyListState, messagesSize: Int, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) true
            else {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index <= 1
            }
        }
    }

    if (!isAtBottom && messagesSize > 0) {
        Box(modifier = modifier) {
            IconButton(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                onClick = {
                    scope.launch { listState.animateScrollToItem(0) }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "К последнему сообщению",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}