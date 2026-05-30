package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    onBack: () -> Unit,
    viewModel: CharacterDetailViewModel = hiltViewModel()
) {
    val name by viewModel.name
    val race by viewModel.race
    val characterClass by viewModel.characterClass
    val observation by viewModel.observation
    val lore by viewModel.lore
    val intelligence by viewModel.intelligence
    val strength by viewModel.strength
    val agility by viewModel.agility
    val isLoading by viewModel.isLoading
    val isSaved by viewModel.isSaved
    val isNewCharacter = viewModel.isNewCharacter
    val scrollState = rememberScrollState()

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (name.isEmpty()) "Novo Personagem" else name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::saveCharacter, enabled = !isLoading && name.isNotBlank()) {
                        Icon(Icons.Default.Save, contentDescription = "Salvar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nome do Personagem") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = race,
                    onValueChange = viewModel::onRaceChange,
                    label = { Text("Raça") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = characterClass,
                    onValueChange = viewModel::onCharacterClassChange,
                    label = { Text("Classe") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isLoading
                )
            }

            if (isNewCharacter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = intelligence,
                        onValueChange = viewModel::onIntelligenceChange,
                        label = { Text("Inteligência") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = strength,
                        onValueChange = viewModel::onStrengthChange,
                        label = { Text("Força") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = agility,
                        onValueChange = viewModel::onAgilityChange,
                        label = { Text("Agilidade") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isLoading
                    )
                }
            }

            OutlinedTextField(
                value = observation,
                onValueChange = viewModel::onObservationChange,
                label = { Text("Observação") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = lore,
                onValueChange = viewModel::onLoreChange,
                label = { Text("História (Lore)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                enabled = !isLoading
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
