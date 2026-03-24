package com.openmacro.core.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * A text field with a contact picker button. Tapping the contacts icon opens
 * the system contact picker and fills in the selected contact's phone number.
 */
@Composable
fun ContactPickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri = result.data?.data ?: return@rememberLauncherForActivityResult
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (numberIndex >= 0) {
                        val number = cursor.getString(numberIndex)
                        onValueChange(number)
                    }
                }
            }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                }
                contactPickerLauncher.launch(intent)
            },
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Contacts,
                contentDescription = "Pick contact",
            )
        }
    }
}
