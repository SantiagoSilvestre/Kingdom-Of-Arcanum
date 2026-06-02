package br.com.silvestresantiago732.kingdomofarcanum

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.AuthRepository
import br.com.silvestresantiago732.kingdomofarcanum.presentation.navigation.NavGraph
import br.com.silvestresantiago732.kingdomofarcanum.presentation.navigation.Screen
import br.com.silvestresantiago732.kingdomofarcanum.ui.theme.KingdomOfArcanumTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KingdomOfArcanumTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                var showDeleteAccountDialog by remember { mutableStateOf(false) }

                val showGlobalAppBar = currentRoute == Screen.Home.route

                if (showDeleteAccountDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteAccountDialog = false },
                        title = { Text(stringResource(R.string.delete_account_title)) },
                        text = { Text(stringResource(R.string.delete_account_confirm)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDeleteAccountDialog = false
                                    scope.launch {
                                        val result = authRepository.deleteAccount()
                                        if (result.isSuccess) {
                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                R.string.delete_account_error,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(stringResource(R.string.char_sheet_delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteAccountDialog = false }) {
                                Text(stringResource(R.string.char_sheet_cancel))
                            }
                        }
                    )
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = showGlobalAppBar,
                    drawerContent = {
                        ModalDrawerSheet {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.app_name),
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            NavigationDrawerItem(
                                label = { Text(text = stringResource(id = R.string.home_title)) },
                                selected = currentRoute == Screen.Home.route,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                },
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text(text = stringResource(id = R.string.logout_button)) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    authRepository.logout()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                icon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text(text = stringResource(id = R.string.delete_account_button)) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    showDeleteAccountDialog = true
                                },
                                icon = {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            if (showGlobalAppBar) {
                                TopAppBar(
                                    title = {
                                        Text(text = stringResource(id = R.string.app_name))
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch { drawerState.open() }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = stringResource(id = R.string.char_sheet_menu_desc)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            NavGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
