package org.unstabledev.pomegranate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LabeledTextField(state: TextFieldState, hint: String, label: String? = null, onClick: () -> Unit = {}, singleLineIn: Boolean = false) {
    Column(modifier = Modifier.clickable{onClick()}) {
        if (label != null) {
            Text(
                modifier = Modifier.padding(start = 30.dp),
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                textAlign = TextAlign.End
            )
        }
        var focused by remember { mutableStateOf(false) }
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth().height(34.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ).padding(horizontal = 16.dp, vertical = 8.dp)
                .onFocusChanged {
                    focused = it.isFocused
                },
            lineLimits = if(singleLineIn) TextFieldLineLimits.SingleLine else TextFieldLineLimits.Default,
            state = state,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            decorator = {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!focused) {
                        Text(
                            text = hint,
                            style = TextStyle(
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
                it()
            }
        )
    }
}
