package kr.hs.jung.example.ui.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kr.hs.jung.example.R

@Composable
fun ExampleErrorAlert(
    isPresented: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    if (isPresented) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = null,
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}
