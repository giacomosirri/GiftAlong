package com.giacomosirri.myapplication.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.giacomosirri.myapplication.R
import com.giacomosirri.myapplication.ui.AppContext
import com.giacomosirri.myapplication.ui.theme.Background
import com.giacomosirri.myapplication.ui.theme.Primary
import com.giacomosirri.myapplication.ui.theme.Typography
import com.giacomosirri.myapplication.viewmodel.AppViewModel
import com.giacomosirri.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class NavigationScreen(val name: String) {
    object Home: NavigationScreen(AppContext.getContext()?.getString(R.string.home)!!)
    object Wishlist: NavigationScreen(AppContext.getContext()?.getString(R.string.wishlist)!!)
    object NewItem: NavigationScreen(AppContext.getContext()?.getString(R.string.new_item)!!)
    object NewEvent: NavigationScreen(AppContext.getContext()?.getString(R.string.new_event)!!)
    object UserProfile: NavigationScreen(AppContext.getContext()?.getString(R.string.user_profile)!!)
    object Relationships: NavigationScreen(AppContext.getContext()?.getString(R.string.relationships)!!)
    object DataCenter: NavigationScreen(AppContext.getContext()?.getString(R.string.data_center)!!)
    object Login: NavigationScreen(AppContext.getContext()?.getString(R.string.login)!!)
    object Registration: NavigationScreen(AppContext.getContext()?.getString(R.string.registration)!!)
}

class Navigation(
    val navController: NavHostController,
    val drawerState: DrawerState,
    val scope: CoroutineScope,
    val isSearchBarOpen: MutableState<Boolean>
)

class Navigator private constructor() {
    companion object {
        private lateinit var navigation: Navigation
        fun setNavigation(navigation: Navigation) { this.navigation = navigation }
        fun getNavigation(): Navigation = navigation
    }
}

class LeadingNavigationIconStrategy(val onBackArrow: () -> Unit, val onMenuIcon: () -> Unit)

@Composable
fun NavigationApp(
    navController: NavHostController = rememberNavController(),
    paddingValues: PaddingValues,
    isUserLoggedIn: Boolean,
    appViewModel: AppViewModel,
    settingsViewModel: SettingsViewModel
) {
    Navigator.setNavigation(Navigation(
        navController = navController,
        drawerState = rememberDrawerState(DrawerValue.Closed),
        scope = rememberCoroutineScope(),
        isSearchBarOpen = rememberSaveable { mutableStateOf(false) }
    ))
    val entryScreenName = navController.currentBackStackEntryAsState().value?.destination?.route ?:
        if (isUserLoggedIn) NavigationScreen.Home.name else NavigationScreen.Login.name
    if (entryScreenName != NavigationScreen.Login.name) {
        NavigationDrawer(appViewModel, settingsViewModel) {
            Scaffold {
                NavigationGraph(
                    navController = navController,
                    paddingValues = paddingValues,
                    appViewModel = appViewModel,
                    settingsViewModel = settingsViewModel,
                    isLoginRequired = !isUserLoggedIn,
                )
            }
        }
    } else {
        NavigationGraph(
            navController = navController,
            paddingValues = paddingValues,
            appViewModel = appViewModel,
            settingsViewModel = settingsViewModel,
            isLoginRequired = !isUserLoggedIn,
        )
    }
}

@Composable
private fun NavigationDrawer(
    appViewModel: AppViewModel,
    settingsViewModel: SettingsViewModel,
    content: @Composable () -> Unit
) {
    val navigation = Navigator.getNavigation()
    ModalNavigationDrawer(
        drawerState = navigation.drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth(.8f)
                    .fillMaxHeight(),
                drawerContainerColor = Background
            ) {
                val items = mapOf(
                    Pair(
                        AppContext.getContext()?.getString(R.string.menu_item1),
                        ImageVector.vectorResource(R.drawable.round_card_giftcard_24)
                    ),
                    Pair(
                        AppContext.getContext()?.getString(R.string.menu_item2),
                        Icons.Rounded.Favorite
                    ),
                    Pair(
                        AppContext.getContext()?.getString(R.string.menu_item3),
                        ImageVector.vectorResource(R.drawable.round_group_24)
                    ),
                    Pair(
                        AppContext.getContext()?.getString(R.string.menu_item4),
                        ImageVector.vectorResource(R.drawable.round_camera_alt_24)
                    ),
                    Pair(
                        AppContext.getContext()?.getString(R.string.menu_item5),
                        ImageVector.vectorResource(R.drawable.round_query_stats_24)
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.08f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppContext.getContext()?.getString(R.string.app_name).toString(),
                        color = Primary,
                        style = Typography.headlineLarge
                    )
                }
                Spacer(Modifier.height(12.dp))
                val selectedItem: MutableState<String?> = remember { mutableStateOf(null) }
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.value, contentDescription = null) },
                        label = { item.key?.let { Text(it) } },
                        colors = NavigationDrawerItemDefaults.colors(),
                        selected = item.key == selectedItem.value,
                        onClick = {
                            navigation.scope.launch { navigation.drawerState.close() }
                            selectedItem.value = item.key
                            // Open the search bar only when the user selects the third option from the drawer,
                            // which navigates to the find new users screen.
                            navigation.isSearchBarOpen.value =
                                selectedItem.value == AppContext.getContext()?.getString(R.string.menu_item3)
                            navigateFromDrawer(selectedItem.value, navigation.navController)
                        },
                        modifier = Modifier.padding(bottom = 6.dp, start = 6.dp, end = 6.dp)
                    )
                }
                Spacer(Modifier.fillMaxHeight(.57f))
                var darkMode by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.padding(start = 20.dp, end = 25.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Mode")
                    Spacer(modifier = Modifier.fillMaxWidth(.8f))
                    Switch(
                        checked = darkMode,
                        onCheckedChange = {
                            darkMode = it
                            if (darkMode) settingsViewModel.deactivateDarkMode() else settingsViewModel.activateDarkMode()
                        }
                    )
                }
                val isLogoutDialogOpen = remember { mutableStateOf(false) }
                if (isLogoutDialogOpen.value) {
                    QuitScreenDialog(
                        isDialogOpen = isLogoutDialogOpen,
                        onQuit = {
                            navigation.scope.launch { navigation.drawerState.close() }
                            settingsViewModel.deactivateAutomaticAuthentication()
                            navigation.navController.navigate(NavigationScreen.Login.name)
                        },
                        dialogTitle = "Logout",
                        mainText = "Are you sure you want to logout?",
                        quitText = "Yes",
                        stayText = "Don't logout"
                    )
                }
                TextButton(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    onClick = {
                        isLogoutDialogOpen.value = true
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(text = "Logout", fontSize = 16.sp)
                }
                val isDeleteAccountDialogOpen = remember { mutableStateOf(false) }
                if (isDeleteAccountDialogOpen.value) {
                    QuitScreenDialog(
                        isDialogOpen = isDeleteAccountDialogOpen,
                        onQuit = {
                            appViewModel.unregisterUser(AppContext.getCurrentUser())
                            navigation.scope.launch { navigation.drawerState.close() }
                            navigation.navController.navigate(NavigationScreen.Login.name)
                        },
                        dialogTitle = "Delete Account",
                        mainText = "Are you really sure you want to delete your account? This operation cannot be undone.",
                        quitText = "Yes",
                        stayText = "Don't delete"
                    )
                }
                TextButton(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    onClick = { isDeleteAccountDialogOpen.value = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(text = "Delete Account", fontSize = 16.sp)
                }
            }
        },
        content = content
    )
}

fun navigateFromDrawer(menuItem: String?, navController: NavHostController) {
    when(menuItem) {
        AppContext.getContext()?.getString(R.string.menu_item1) ->
            navController.navigate(NavigationScreen.Wishlist.name + AppContext.getCurrentUser())
        AppContext.getContext()?.getString(R.string.menu_item2),
        AppContext.getContext()?.getString(R.string.menu_item3) ->
            navController.navigate(NavigationScreen.Relationships.name)
        AppContext.getContext()?.getString(R.string.menu_item4) ->
            navController.navigate(NavigationScreen.UserProfile.name + AppContext.getCurrentUser())
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    appViewModel: AppViewModel,
    settingsViewModel: SettingsViewModel,
    isLoginRequired: Boolean,
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoginRequired) NavigationScreen.Login.name else NavigationScreen.Home.name
    ) {
        composable(NavigationScreen.Registration.name) {
            RegistrationScreen(
                paddingValues = paddingValues,
                appViewModel = appViewModel,
                settingsViewModel = settingsViewModel,
                onRegisterClick = { navController.navigate(NavigationScreen.Home.name) }
            )
        }
        composable(NavigationScreen.Login.name) {
            LoginScreen(
                appViewModel = appViewModel,
                settingsViewModel = settingsViewModel,
                onLoginClick = { navController.navigate(NavigationScreen.Home.name) },
                onRegisterClick = { navController.navigate(NavigationScreen.Registration.name) }
            )
        }
        composable(NavigationScreen.Home.name) {
            HomeScreen(
                paddingValues = paddingValues,
                onFabClick = { navController.navigate(NavigationScreen.NewEvent.name) },
                navController = navController,
                viewModel = appViewModel
            )
        }
        composable(NavigationScreen.Wishlist.name + "{username}") {
            val username = it.arguments?.getString("username") ?: ""
            WishlistScreen(
                username = username,
                paddingValues = paddingValues,
                onFabClick = { navController.navigate(NavigationScreen.NewItem.name) },
                navController = navController,
                viewModel = appViewModel
            )
        }
        composable(NavigationScreen.NewItem.name) {
            NewItemScreen(
                appViewModel = appViewModel,
                paddingValues = paddingValues,
                onExit = { navController.navigateUp() },
                isInEditMode = false,
            )
        }
        composable(NavigationScreen.NewItem.name + "{itemId}") {
            val id = it.arguments?.getString("itemId")?.toInt()
            NewItemScreen(
                appViewModel = appViewModel,
                paddingValues = paddingValues,
                onExit = { navController.navigateUp() },
                isInEditMode = true,
                id = id
            )
        }
        composable(NavigationScreen.NewEvent.name) {
            NewEventScreen(
                appViewModel = appViewModel,
                paddingValues = paddingValues,
                onExit = { navController.navigateUp() },
                isInEditMode = false
            )
        }
        composable(NavigationScreen.NewEvent.name + "{eventId}") {
            val id = it.arguments?.getString("eventId")?.toInt()
            NewEventScreen(
                appViewModel = appViewModel,
                paddingValues = paddingValues,
                onExit = { navController.navigateUp() },
                isInEditMode = true,
                id = id
            )
        }
        composable(NavigationScreen.UserProfile.name + "{username}") {
            val username = it.arguments?.getString("username") ?: AppContext.getCurrentUser()
            UserProfileScreen(
                paddingValues = paddingValues,
                username = username,
                onWishlistButtonClick = { navController.navigate(NavigationScreen.Wishlist.name + username) },
                navController = navController
            )
        }
        composable(
            route ="${NavigationScreen.Relationships.name}?query={query}",
            arguments = listOf(navArgument("query") { nullable = true })
        ) {
            val query = it.arguments?.getString("query")
            RelationshipsScreen(
                paddingValues = paddingValues,
                navController = navController,
                viewModel = appViewModel,
                query = query
            )
        }
        composable(NavigationScreen.DataCenter.name) {
            DataCenterScreen(paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationAppBar(
    currentScreenName: String,
    hasSearchBar: Boolean,
    searchBarPlaceholder: String? = null,
    isLeadingIconMenu: Boolean = false,
    isLeadingIconBackArrow: Boolean = true
) {
    val navigation = Navigator.getNavigation()
    var query by remember { mutableStateOf("") }
    var displayQueriedEvents by remember { mutableStateOf(false) }
    val isNavigationBarVisible by derivedStateOf { !navigation.isSearchBarOpen.value }
    if (navigation.isSearchBarOpen.value) {
        SearchBar(
            modifier = Modifier.fillMaxSize(),
            query = query,
            onQueryChange = { query = it },
            placeholder = { Text(searchBarPlaceholder!!) },
            onSearch = { displayQueriedEvents = true },
            leadingIcon = {
                IconButton(onClick = { navigation.isSearchBarOpen.value = false }) {
                    Icon(Icons.Rounded.ArrowBack, "Close icon")
                }
            },
            trailingIcon = {
                IconButton(onClick = {
                    query = ""
                    displayQueriedEvents = false
                }) {
                    Icon(Icons.Rounded.Clear, "Clear icon")
                }
            },
            active = true,
            onActiveChange = {},
            colors = SearchBarDefaults.colors(containerColor = Background),
            shape = ShapeDefaults.ExtraSmall
        ) {
            if (displayQueriedEvents) {
                when (currentScreenName) {
                    AppContext.getContext()?.getString(R.string.home) -> HomeScreen(query, navigation.navController)
                    AppContext.getContext()?.getString(R.string.wishlist) -> WishlistScreen(query)
                    AppContext.getContext()?.getString(R.string.relationships) ->
                        navigation.navController.navigate("${NavigationScreen.Relationships.name}?query=$query")
                }
            }
        }
    }
    if (isNavigationBarVisible) {
        val navIconStrategy = LeadingNavigationIconStrategy(
            onBackArrow = { navigation.navController.navigateUp() },
            onMenuIcon = { navigation.scope.launch { navigation.drawerState.open() } }
        )
        CenterAlignedTopAppBar(
            actions = {
                // Only a handful of screens should have the search icon in the top app bar.
                if (hasSearchBar) {
                    IconButton(onClick = { navigation.isSearchBarOpen.value = true }) {
                        Icon(Icons.Filled.Search, "Search icon")
                    }
                }
            },
            title = {
                Text(text = currentScreenName, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            navigationIcon = {
                if (isLeadingIconMenu) {
                    IconButton(onClick = navIconStrategy.onMenuIcon) {
                        Icon(Icons.Filled.Menu, "Main menu icon")
                    }
                }
                if(isLeadingIconBackArrow) {
                    IconButton(onClick = navIconStrategy.onBackArrow) {
                        Icon(Icons.Filled.ArrowBack, "Go back icon")
                    }
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Primary)
        )
    }
}