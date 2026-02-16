package com.loaderapp.ui.dispatcher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
    onSwitchRole: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSwitchDialog by remember { mutableStateOf(false) }
    var currentDestination by remember { mutableStateOf(DispatcherDestination.ORDERS) }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp),
                drawerContainerColor = androidx.compose.ui.graphics.Color.White
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Заголовок панели
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = userName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Диспетчер",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Divider()
                
                // Пункты меню
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Заказы") },
                    selected = currentDestination == DispatcherDestination.ORDERS,
                    onClick = {
                        currentDestination = DispatcherDestination.ORDERS
                        scope.launch { drawerState.close() }
                    },
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Рейтинг") },
                    selected = currentDestination == DispatcherDestination.RATING,
                    onClick = {
                        currentDestination = DispatcherDestination.RATING
                        scope.launch { drawerState.close() }
                    },
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("История") },
                    selected = currentDestination == DispatcherDestination.HISTORY,
                    onClick = {
                        currentDestination = DispatcherDestination.HISTORY
                        scope.launch { drawerState.close() }
                    },
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Настройки") },
                    selected = currentDestination == DispatcherDestination.SETTINGS,
                    onClick = {
                        currentDestination = DispatcherDestination.SETTINGS
                        scope.launch { drawerState.close() }
                    },
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    label = { Text("Сменить роль") },
                    selected = false,
                    onClick = {
                        showSwitchDialog = true
                        scope.launch { drawerState.close() }
                    },
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        when (currentDestination) {
            DispatcherDestination.ORDERS -> {
                OrdersContent(
                    orders = orders,
                    isLoading = isLoading,
                    userName = userName,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onCreateOrder = { showCreateDialog = true },
                    onCancelOrder = { viewModel.cancelOrder(it) }
                )
            }
            DispatcherDestination.SETTINGS -> {
                SettingsScreen(
                    onBackClick = { currentDestination = DispatcherDestination.ORDERS }
                )
            }
            DispatcherDestination.RATING -> {
                RatingScreen(
                    userName = userName,
                    userRating = 5.0,
                    onBackClick = { currentDestination = DispatcherDestination.ORDERS }
                )
            }
            DispatcherDestination.HISTORY -> {
                HistoryScreen(
                    orders = orders,
                    onBackClick = { currentDestination = DispatcherDestination.ORDERS }
                )
            }
        }
    }
    
    // Диалог создания заказа
    if (showCreateDialog) {
        CreateOrderDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { address, dateTime, cargo, price ->
                viewModel.createOrder(address, dateTime, cargo, price)
                showCreateDialog = false
            }
        )
    }
    
    // Диалог смены роли
    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            title = { Text("Сменить роль?") },
            text = { Text("Вы хотите выйти из режима диспетчера?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSwitchDialog = false
                        onSwitchRole()
                    }
                ) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersContent(
    orders: List<Order>,
    isLoading: Boolean,
    userName: String,
    onMenuClick: () -> Unit,
    onCreateOrder: () -> Unit,
    onCancelOrder: (Order) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Панель диспетчера")
                        Text(
                            text = userName,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Меню"
                        )
                    }
                }
            )
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
            if (orders.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Нет заказов",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Создайте первый заказ",
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
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            onCancel = { onCancelOrder(it) }
                        )
                    }
                }
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
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
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.address,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                StatusChip(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Дата: ${dateFormat.format(Date(order.dateTime))}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Груз: ${order.cargoDescription}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = "Оплата: ${order.pricePerHour} ₽/час",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
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
                        2.dp,
                        MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Отменить")
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val (text, color) = when (status) {
        OrderStatus.AVAILABLE -> "Доступен" to MaterialTheme.colorScheme.primary
        OrderStatus.TAKEN -> "Занят" to MaterialTheme.colorScheme.tertiary
        OrderStatus.COMPLETED -> "Завершен" to MaterialTheme.colorScheme.secondary
        OrderStatus.CANCELLED -> "Отменен" to MaterialTheme.colorScheme.error
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = color
        )
    }
}
