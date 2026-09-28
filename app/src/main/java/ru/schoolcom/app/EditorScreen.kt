@file:OptIn(ExperimentalMaterial3Api::class)

package ru.schoolcom.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.schoolcom.biblio.form.FieldKind
import ru.schoolcom.biblio.form.FormResult
import ru.schoolcom.biblio.form.SourceType

@Composable
fun EditorScreen(vm: SourcesViewModel) {
    BackHandler(onBack = vm::cancelEditing)
    var errors by remember { mutableStateOf(emptyList<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Источник") },
                navigationIcon = { TextButton(onClick = vm::cancelEditing) { Text("Назад") } },
                actions = { TextButton(onClick = { errors = vm.save() }) { Text("Сохранить") } },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SourceType.entries.forEach { type ->
                    FilterChip(
                        selected = vm.formType == type,
                        onClick = { vm.selectType(type) },
                        label = { Text(type.title) },
                    )
                }
            }

            PreviewCard(vm)

            errors.forEach {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            vm.formType.fields.forEach { field ->
                OutlinedTextField(
                    value = vm.formValues[field.key].orEmpty(),
                    onValueChange = { vm.setValue(field.key, it) },
                    label = { Text(if (field.required) "${field.label} *" else field.label) },
                    placeholder = { if (field.hint.isNotEmpty()) Text(field.hint) },
                    singleLine = field.kind != FieldKind.PEOPLE,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = when (field.kind) {
                            FieldKind.NUMBER -> KeyboardType.Number
                            FieldKind.URL -> KeyboardType.Uri
                            else -> KeyboardType.Text
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PreviewCard(vm: SourcesViewModel) {
    val result = vm.buildForm()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Предпросмотр", style = MaterialTheme.typography.labelMedium)
            when (result) {
                is FormResult.Ok -> {
                    Text(vm.preview().orEmpty(), style = MaterialTheme.typography.bodyMedium)
                    result.warnings.forEach {
                        Text(it, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                is FormResult.Invalid -> Text(
                    "Заполните поля, отмеченные *",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
