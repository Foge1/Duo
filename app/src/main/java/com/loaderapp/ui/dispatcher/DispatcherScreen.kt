package com.loaderapp.ui.dispatcher

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loaderapp.data.model.Order
import com.loaderapp.data.model.OrderStatus
import com.loaderapp.ui.history.HistoryScreen
import com.loaderapp.ui.rating.RatingScreen
import com.loaderapp.ui.settings.SettingsScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class DispatcherDestination {
    ORDERS, SETTINGS, RATING, HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatcherScreen(
    viewModel: DispatcherViewModel,
    userName: String,
    onSwitchRole: () -> Unit,
    onDarkThemeChanged: ((Boolean) -> Unit)? = null
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState(initial = 0)
    val activeCount by viewModel.activeCount.collectAsState(initial = 0)
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSwitchDialog by remember { mutableStateOf(false) }
    var currentDestination by remember { mutableStateOf(DispatcherDestination.ORDERS) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Свободные", "В работе")
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RectangleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Закрыть меню",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = userName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Диспетчер",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider()

                val primary = MaterialTheme.colorScheme.primary

                @Composable
                fun DrawerItem(label: String, selected: Boolean, onClick: () -> Unit) {
                    val textColor = if (selected) primary
                    else MaterialTheme.colorScheme.onSurfaceVariant

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                if (selected) Brush.horizontalGradient(
                                    colors = listOf(primary.copy(alpha = 0.15f), Color.Transparent)
                                ) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(primary.copy(alpha = if (selected) 1f else 0f))
                        )
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent,
                            onClick = onClick
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 15.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                DrawerItem("Заказы", currentDestination == DispatcherDestination.ORDERS) {
                    currentDestination = DispatcherDestination.ORDERS
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Рейтинг", currentDestination == DispatcherDestination.RATING) {
                    currentDestination = DispatcherDestination.RATING
                    scope.launch { drawerState.close() }
                }
                DrawerItem("История", currentDestination == DispatcherDestination.HISTORY) {
                    currentDestination = DispatcherDestination.HISTORY
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Настройки", currentDestination == DispatcherDestination.SETTINGS) {
                    currentDestination = DispatcherDestination.SETTINGS
                    scope.launch { drawerState.close() }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                DrawerItem("Сменить роль", false) {
                    showSwitchDialog = true
                    scope.launch { drawerState.close() }
                }
            }
        }
    ) {
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) +
                slideInHorizontally(animationSpec = tween(220), initialOffsetX = { it / 12 }) togetherWith
                fadeOut(animationSpec = tween(150))
            },
            label = "dispatcher_nav"
        ) { destination ->
            when (destination) {
                DispatcherDestination.ORDERS -> {
                    OrdersContent(
                        orders = orders,
                        isLoading = isLoading,
                        userName = userName,
                        selectedTab = selectedTab,
                        tabs = tabs,
                        searchQuery = searchQuery,
                        isSearchActive = isSearchActive,
                        onTabSelected = { selectedTab = it },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onCreateOrder = { showCreateDialog = true },
                        onCancelOrder = { viewModel.cancelOrder(it) },
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSearchToggle = { viewModel.setSearchActive(it) }
                    )
                }
                DispatcherDestination.SETTINGS -> {
                    SettingsScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { currentDestination = DispatcherDestination.ORDERS },
                        onDarkThemeChanged = onDarkThemeChanged
                    )
                }
                DispatcherDestination.RATING -> {
                    RatingScreen(
                        userName = userName,
                        userRating = 5.0,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { currentDestination = DispatcherDestination.ORDERS },
                        dispatcherCompletedCount = completedCount,
                        dispatcherActiveCount = activeCount,
                        isDispatcher = true
                    )
                }
                DispatcherDestination.HISTORY -> {
                    HistoryScreen(
                        orders = orders,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { currentDestination = DispatcherDestination.ORDERS }
                    )
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateOrderDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { address, dateTime, cargo, price, hours, comment ->
                viewModel.createOrder(address, dateTime, cargo, price, hours, comment)
                showCreateDialog = false
            }
        )
    }
    
    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            title = { Text("Сменить роль?") },
            text = { Text("Вы хотите выйти из режима диспетчера?") },
            confirmButton = {
                TextButton(onClick = {
                    showSwitchDialog = false
                    onSwitchRole()
                }) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchDialog = false }) { Text("Отмена") }
            }
        )
    }
    
    errorMessage?.let {
        LaunchedEffect(it) { viewModel.clearError() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersContent(
    orders: List<Order>,
    isLoading: Boolean,
    userName: String,
    selectedTab: Int,
    tabs: List<String>,
    searchQuery: String = "",
    isSearchActive: Boolean = false,
    onTabSelected: (Int) -> Unit,
    onMenuClick: () -> Unit,
    onCreateOrder: () -> Unit,
    onCancelOrder: (Order) -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchToggle: (Boolean) -> Unit = {}
) {
    val availableOrders = orders.filter { it.status == OrderStatus.AVAILABLE }
    val takenOrders = orders.filter { it.status == OrderStatus.TAKEN || it.status == OrderStatus.COMPLETED }
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = { Text("Поиск заказов...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        } else {
                            Column {
                                Text("Панель диспетчера")
                                Text(
                                    text = userName,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = { onSearchToggle(false) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                            }
                        } else {
                            IconButton(onClick = onMenuClick) {
                                Icon(Icons.Default.Menu, contentDescription = "Меню")
                            }
                        }
                    },
                    actions = {
                        if (!isSearchActive) {
                            IconButton(onClick = { onSearchToggle(true) }) {
                                Icon(Icons.Default.Search, contentDescription = "Поиск")
                            }
                        } else if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { onTabSelected(index) },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateOrder,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Создать заказ") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val currentOrders = if (selectedTab == 0) availableOrders else takenOrders
            val emptyText = if (selectedTab == 0) "Нет свободных заказов" else "Нет заказов в работе"
            val emptySubText = if (selectedTab == 0) "Создайте первый заказ" else "Свободные заказы появятся здесь"

            if (currentOrders.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.Inbox else Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isSearchActive && searchQuery.isNotEmpty()) "Заказы не найдены" else emptyText,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isSearchActive && searchQuery.isNotEmpty()) "Попробуйте другой запрос" else emptySubText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentOrders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onCancel = { onCancelOrder(it) }
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onCancel: (Order) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val accentColor = when (order.status) {
        OrderStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
        OrderStatus.TAKEN, OrderStatus.IN_PROGRESS -> Color(0xFFE67E22)
        OrderStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.address,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(status = order.status)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dateFormat.format(Date(order.dateTime)),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = order.cargoDescription,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (order.comment.isNotBlank()) {
                    Text(
                        text = "💬 ${order.comment}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${order.pricePerHour.toInt()} ₽/час",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    if (order.estimatedHours > 1) {
                        Text(
                            text = " · ~${order.estimatedHours} ч · ${(order.pricePerHour * order.estimatedHours).toInt()} ₽",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (order.status == OrderStatus.AVAILABLE) {
                    OutlinedButton(
                        onClick = { onCancel(order) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Отменить", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val (text, color) = when (status) {
        OrderStatus.AVAILABLE -> "Доступен" to MaterialTheme.colorScheme.primary
        OrderStatus.TAKEN -> "Занят" to Color(0xFFE67E22)
        OrderStatus.IN_PROGRESS -> "В процессе" to Color(0xFFE67E22)
        OrderStatus.COMPLETED -> "Завершён" to MaterialTheme.colorScheme.secondary
        OrderStatus.CANCELLED -> "Отменён" to MaterialTheme.colorScheme.error
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
