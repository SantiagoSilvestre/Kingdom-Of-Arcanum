package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Item
import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Skill
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSheetScreen(
    onBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToImage: (String) -> Unit,
    viewModel: CharacterSheetViewModel = hiltViewModel()
) {
    val character by viewModel.character
    val isLoading by viewModel.isLoading
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var showSkillDialog by remember { mutableStateOf(false) }
    var showItemDialog by remember { mutableStateOf(false) }
    var showAttributePointsDialog by remember { mutableStateOf(false) }
    var skillToEdit by remember { mutableStateOf<Skill?>(null) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var skillToDelete by remember { mutableStateOf<Skill?>(null) }
    var itemToDelete by remember { mutableStateOf<Item?>(null) }
    var showGoldDialog by remember { mutableStateOf(false) }

    if (showGoldDialog && character != null) {
        GoldControlDialog(
            currentGold = character!!.gold,
            onDismiss = { showGoldDialog = false },
            onConfirm = { newVal ->
                viewModel.updateAttribute("gold", newVal)
                showGoldDialog = false
            }
        )
    }

    if (skillToDelete != null) {
        DeleteConfirmationDialog(
            title = "Excluir Habilidade",
            text = "Tem certeza que deseja excluir a habilidade ${skillToDelete?.name}?",
            onDismiss = { skillToDelete = null },
            onConfirm = {
                skillToDelete?.let { viewModel.deleteSkill(it.id) }
                skillToDelete = null
            }
        )
    }

    if (itemToDelete != null) {
        DeleteConfirmationDialog(
            title = "Excluir Item",
            text = "Tem certeza que deseja excluir o item ${itemToDelete?.name}?",
            onDismiss = { itemToDelete = null },
            onConfirm = {
                itemToDelete?.let { viewModel.deleteItem(it.id) }
                itemToDelete = null
            }
        )
    }

    if (showFullScreenImage && character?.imageUrl?.isNotEmpty() == true) {
        AlertDialog(
            onDismissRequest = { showFullScreenImage = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            text = {
                Box(modifier = Modifier.fillMaxSize().clickable { showFullScreenImage = false }) {
                    AsyncImage(
                        model = character?.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {}
        )
    }

    if (showAttributePointsDialog && character != null) {
        AttributePointsDialog(
            character = character!!,
            onDismiss = { showAttributePointsDialog = false },
            onConfirm = { intInc, strInc ->
                viewModel.spendPoints(intInc, strInc)
                showAttributePointsDialog = false
            }
        )
    }

    if (showSkillDialog || skillToEdit != null) {
        EntryDialog(
            title = if (skillToEdit != null) "Editar Habilidade" else "Nova Habilidade",
            initialName = skillToEdit?.name ?: "",
            initialDamage = skillToEdit?.damage ?: "",
            initialObservation = skillToEdit?.observation ?: "",
            initialManaCost = skillToEdit?.manaCost ?: 0,
            isSkill = true,
            onDismiss = { 
                showSkillDialog = false
                skillToEdit = null
            },
            onConfirm = { name, damage, obs, manaCost ->
                if (skillToEdit != null) {
                    viewModel.updateSkill(skillToEdit!!.id, name, damage, obs, manaCost)
                } else {
                    viewModel.addSkill(name, damage, obs, manaCost)
                }
                showSkillDialog = false
                skillToEdit = null
            }
        )
    }

    if (showItemDialog || itemToEdit != null) {
        EntryDialog(
            title = if (itemToEdit != null) "Editar Item" else "Novo Item",
            initialName = itemToEdit?.name ?: "",
            initialDamage = itemToEdit?.damage ?: "",
            initialObservation = itemToEdit?.observation ?: "",
            onDismiss = { 
                showItemDialog = false
                itemToEdit = null
            },
            onConfirm = { name, damage, obs, _ ->
                if (itemToEdit != null) {
                    viewModel.updateItem(itemToEdit!!.id, name, damage, obs)
                } else {
                    viewModel.addItem(name, damage, obs)
                }
                showItemDialog = false
                itemToEdit = null
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Ações do Personagem",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Editar Personagem") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        character?.let { onNavigateToEdit(it.id) }
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Gerar imagem do personagem") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        character?.let { onNavigateToImage(it.id) }
                    },
                    icon = { Icon(Icons.Default.Face, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (character?.imageUrl?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = character?.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .clickable { showFullScreenImage = true }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column {
                                Text(character?.name ?: "Carregando...")
                                Text(
                                    text = "${character?.race ?: ""} ${character?.characterClass ?: ""} - Nível ${character?.level ?: 1}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                character?.let { char ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Barra de Vida
                        StatBar(
                            label = "Vida",
                            current = char.currentHp,
                            max = char.maxHp,
                            color = Color(0xFFE57373),
                            onAddClick = { viewModel.updateHealth(1) },
                            onRemoveClick = { viewModel.updateHealth(-1) }
                        )

                        // Barra de Mana
                        StatBar(
                            label = "Mana",
                            current = char.currentMana,
                            max = char.maxMana,
                            color = Color(0xFF64B5F6),
                            onAddClick = { viewModel.updateMana(1) },
                            onRemoveClick = { viewModel.updateMana(-1) }
                        )

                        // Barra de XP
                        StatBar(
                            label = "XP",
                            current = char.currentXp,
                            max = char.maxXp,
                            color = Color(0xFFFFD54F),
                            onAddClick = { viewModel.addXp(10) } // Adiciona 10 XP por clique para testar
                        )

                        if (char.attributePoints > 0) {
                            Text(
                                text = "Você tem ${char.attributePoints} ponto(s) de atributo disponível(is)!",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showAttributePointsDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Seção de Atributos
                        Text(
                            text = "Atributos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EditableAttribute(
                                label = "Int",
                                value = char.intelligence,
                                onValueChange = { },
                                modifier = Modifier.weight(1f),
                                enabled = false
                            )
                            EditableAttribute(
                                label = "For",
                                value = char.strength,
                                onValueChange = { },
                                modifier = Modifier.weight(1f),
                                enabled = false
                            )
                            
                            // Botão de Ouro estilizado para combinar com os atributos
                            OutlinedButton(
                                onClick = { showGoldDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp), // Mesma altura padrão do OutlinedTextField
                                shape = MaterialTheme.shapes.extraSmall,
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Ouro",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = char.gold.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tabs para Habilidades e Itens
                        var selectedTab by remember { mutableIntStateOf(0) }
                        val tabs = listOf("Habilidades", "Itens")

                        Column(modifier = Modifier.fillMaxWidth()) {
                            TabRow(selectedTabIndex = selectedTab) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = { Text(title) }
                                    )
                                }
                            }
                            
                            when (selectedTab) {
                                0 -> SkillListTab(
                                    skills = char.skills,
                                    currentMana = char.currentMana,
                                    onAddSkill = { showSkillDialog = true },
                                    onEditSkill = { skillToEdit = it },
                                    onDeleteSkill = { skillToDelete = it },
                                    onUseSkill = viewModel::useSkill
                                )
                                1 -> ItemListTab(
                                    items = char.items,
                                    onAddItem = { showItemDialog = true },
                                    onEditItem = { itemToEdit = it },
                                    onDeleteItem = { itemToDelete = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoldControlDialog(
    currentGold: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gerenciar Ouro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ouro atual: $currentGold", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onConfirm(currentGold + amount) },
                        modifier = Modifier.weight(1f),
                        enabled = amount > 0
                    ) {
                        Text("Adicionar")
                    }
                    Button(
                        onClick = { onConfirm((currentGold - amount).coerceAtLeast(0)) },
                        modifier = Modifier.weight(1f),
                        enabled = amount > 0 && currentGold >= amount,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Remover")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AttributePointsDialog(
    character: br.com.silvestresantiago732.kingdomofarcanum.domain.model.Character,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var intIncrease by remember { mutableIntStateOf(0) }
    var strIncrease by remember { mutableIntStateOf(0) }
    val pointsLeft = character.attributePoints - (intIncrease + strIncrease)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Distribuir Pontos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Pontos disponíveis: $pointsLeft", fontWeight = FontWeight.Bold)
                
                AttributeControl(
                    label = "Inteligência",
                    currentValue = character.intelligence + intIncrease,
                    canIncrease = pointsLeft > 0,
                    onIncrease = { intIncrease++ },
                    canDecrease = intIncrease > 0,
                    onDecrease = { intIncrease-- }
                )

                AttributeControl(
                    label = "Força",
                    currentValue = character.strength + strIncrease,
                    canIncrease = pointsLeft > 0,
                    onIncrease = { strIncrease++ },
                    canDecrease = strIncrease > 0,
                    onDecrease = { strIncrease-- }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(intIncrease, strIncrease) },
                enabled = (intIncrease + strIncrease) > 0
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AttributeControl(
    label: String,
    currentValue: Int,
    canIncrease: Boolean,
    onIncrease: () -> Unit,
    canDecrease: Boolean,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label: $currentValue")
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease, enabled = canDecrease) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Diminuir",
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onIncrease, enabled = canIncrease) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar")
            }
        }
    }
}

@Composable
fun EditableAttribute(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            textValue = newValue
            newValue.toIntOrNull()?.let { onValueChange(it) }
        },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        enabled = enabled
    )
}

@Composable
fun SkillListTab(
    skills: List<Skill>,
    currentMana: Int,
    onAddSkill: () -> Unit,
    onEditSkill: (Skill) -> Unit,
    onDeleteSkill: (Skill) -> Unit,
    onUseSkill: (Skill) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Button(onClick = onAddSkill, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Adicionar Habilidade")
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            items(skills) { skill ->
                val canUse = currentMana >= skill.manaCost
                ListItem(
                    modifier = Modifier.clickable { onEditSkill(skill) },
                    headlineContent = { Text(skill.name, fontWeight = FontWeight.Bold) },
                    supportingContent = { 
                        Text("${skill.damage} - ${skill.observation}\nCusto de Mana: ${skill.manaCost}") 
                    },
                    trailingContent = {
                        Row {
                            IconButton(
                                onClick = { onUseSkill(skill) },
                                enabled = canUse
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow, 
                                    contentDescription = "Usar Habilidade",
                                    tint = if (canUse) Color(0xFF4CAF50) else Color.Gray
                                )
                            }
                            IconButton(onClick = { onDeleteSkill(skill) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red)
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ItemListTab(
    items: List<Item>,
    onAddItem: () -> Unit,
    onEditItem: (Item) -> Unit,
    onDeleteItem: (Item) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Button(onClick = onAddItem, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Adicionar Item")
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            items(items) { item ->
                ListItem(
                    modifier = Modifier.clickable { onEditItem(item) },
                    headlineContent = { Text(item.name, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("${item.damage} - ${item.observation}") },
                    trailingContent = {
                        IconButton(onClick = { onDeleteItem(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red)
                        }
                    }
                )
                HorizontalDivider()
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
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EntryDialog(
    title: String,
    initialName: String = "",
    initialDamage: String = "",
    initialObservation: String = "",
    initialManaCost: Int = 0,
    isSkill: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var damage by remember { mutableStateOf(initialDamage) }
    var observation by remember { mutableStateOf(initialObservation) }
    var manaCost by remember { mutableStateOf(initialManaCost.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
                if (isSkill) {
                    OutlinedTextField(
                        value = manaCost, 
                        onValueChange = { manaCost = it }, 
                        label = { Text("Custo de Mana") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                OutlinedTextField(value = damage, onValueChange = { damage = it }, label = { Text("Dano/Efeito") })
                OutlinedTextField(value = observation, onValueChange = { observation = it }, label = { Text("Observação") }, minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, damage, observation, manaCost.toIntOrNull() ?: 0) }, 
                enabled = name.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun StatBar(
    label: String,
    current: Int,
    max: Int,
    color: Color,
    onAddClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null
) {
    val progress = if (max > 0) current.toFloat() / max else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onRemoveClick != null) {
                    IconButton(onClick = onRemoveClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuir",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(text = "$current / $max", style = MaterialTheme.typography.bodyMedium)
                if (onAddClick != null) {
                    IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
