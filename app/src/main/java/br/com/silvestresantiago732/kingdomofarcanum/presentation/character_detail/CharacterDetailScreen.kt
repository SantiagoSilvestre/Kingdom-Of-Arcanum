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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.silvestresantiago732.kingdomofarcanum.R

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
                title = { Text(if (name.isEmpty()) stringResource(R.string.char_detail_new_char) else name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.char_detail_back_desc))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::saveCharacter, enabled = !isLoading && name.isNotBlank()) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.char_detail_save_desc))
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
                label = { Text(stringResource(R.string.char_detail_name_label)) },
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
                    label = { Text(stringResource(R.string.char_detail_race_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = characterClass,
                    onValueChange = viewModel::onCharacterClassChange,
                    label = { Text(stringResource(R.string.char_detail_class_label)) },
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
                        label = { Text(stringResource(R.string.char_detail_int_label)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = strength,
                        onValueChange = viewModel::onStrengthChange,
                        label = { Text(stringResource(R.string.char_detail_str_label)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = agility,
                        onValueChange = viewModel::onAgilityChange,
                        label = { Text(stringResource(R.string.char_detail_agi_label)) },
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
                label = { Text(stringResource(R.string.char_detail_obs_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = lore,
                onValueChange = viewModel::onLoreChange,
                label = { Text(stringResource(R.string.char_detail_lore_label)) },
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
