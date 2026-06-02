package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.silvestresantiago732.kingdomofarcanum.R
import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Character
import coil.compose.AsyncImage

@Composable
fun CharacterListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: CharacterListViewModel = hiltViewModel()
) {
    val characters by viewModel.characters.collectAsState()
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val context = androidx.compose.ui.platform.LocalContext.current
    var characterToDelete by remember { mutableStateOf<Character?>(null) }

    LaunchedEffect(error) {
        error?.let {
            android.widget.Toast.makeText(context, context.getString(it), android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetError()
        }
    }

    if (characterToDelete != null) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.char_list_delete_title),
            text = stringResource(R.string.char_list_delete_confirm, characterToDelete?.name ?: ""),
            onDismiss = { characterToDelete = null },
            onConfirm = {
                characterToDelete?.let { viewModel.deleteCharacter(it.id) }
                characterToDelete = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.char_list_new_char))
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (characters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_list_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(characters) { character ->
                    CharacterItem(
                        character = character,
                        onDelete = { characterToDelete = character },
                        onClick = { onNavigateToDetail(character.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.char_sheet_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.char_sheet_cancel))
            }
        }
    )
}

@Composable
fun CharacterItem(
    character: Character,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (character.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    error = rememberVectorPainter(Icons.Default.Person)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${character.race} ${character.characterClass} - ${stringResource(R.string.char_sheet_level)} ${character.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (character.observation.isNotBlank()) {
                    Text(
                        text = character.observation,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.char_list_delete_content_desc))
            }
        }
    }
}
