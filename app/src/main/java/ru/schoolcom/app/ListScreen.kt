@file:OptIn(ExperimentalMaterial3Api::class)

package ru.schoolcom.app

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ru.schoolcom.biblio.format.Styles

@Composable
fun ListScreen(vm: SourcesViewModel) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    fun copy(text: String, message: String) {
        clipboard.setText(AnnotatedString(text))
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val entries = vm.entries()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список литературы") },
                actions = {
                    if (entries.isNotEmpty()) {
                        TextButton(onClick = { copy(vm.bibliographyText(), "Список скопирован") }) {
                            Text("Копировать всё")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = vm::startAdding) { Text("Добавить источник") }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Styles.all.forEach { style ->
                    FilterChip(
                        selected = vm.style == style,
                        onClick = { vm.selectStyle(style) },
                        label = { Text(style.name) },
                    )
                }
            }
            if (entries.isEmpty()) {
                Text(
                    "Добавьте первый источник — приложение оформит его по выбранным правилам.",
                    Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(entries) { entry ->
                    Card(Modifier.fillMaxWidth().clickable { copy(entry.text, "Запись скопирована") }) {
                        Column(Modifier.padding(12.dp)) {
                            Text(entry.text, style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { copy(entry.citation, "Ссылка ${entry.citation} скопирована") }) {
                                    Text("Ссылка ${entry.citation}")
                                }
                                TextButton(onClick = { vm.startEditing(entry.source) }) { Text("Изменить") }
                                TextButton(onClick = { vm.delete(entry.source) }) { Text("Удалить") }
                            }
                        }
                    }
                }
            }
        }
    }
}
