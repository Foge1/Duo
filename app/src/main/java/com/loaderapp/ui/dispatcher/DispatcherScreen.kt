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
                drawerContainerColor = Color.White,
                drawerShape = RectangleShape
            ) {
                // Кнопка закрытия (три полоски) + заголовок
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
                
                Divider()

                val primary = MaterialTheme.colorScheme.primary

                // Кастомный пункт меню с градиентом
                @Composable
                fun DrawerItem(label: String, selected: Boolean, onClick: () -> Unit) {
                    val bgAlpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = tween(200),
                        label = "drawer_bg"
                    )
                    val textColor = if (selected) primary
                    else MaterialTheme.colorScheme.onSurfaceVariant

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                if (selected) Brush.horizontalGradient(
                                    colors = listOf(
                                        primary.copy(alpha = 0.15f * bgAlpha),
                                        Color.Transparent
                                    )
                                ) else Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color.Transparent)
                                )
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

                Divider(modifier = Modifier.padding(vertical = 8.dp))

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
                slideInHorizontally(
                    animationSpec = tween(220),
                    initialOffsetX = { it / 12 }
                ) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "dispatcher_nav"
        ) { destination ->
            when (destination) {
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
    val accentColor = when (order.status) {
        OrderStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
        OrderStatus.TAKEN -> Color(0xFFE67E22)
        OrderStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
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
            // Левый акцент-полоска цветом статуса
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

                Text(
                    text = "${order.pricePerHour.toInt()} ₽/час",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (order.status == OrderStatus.AVAILABLE) {
                    var cancelPressed by remember { mutableStateOf(false) }
                    val cancelScale by animateFloatAsState(
                        targetValue = if (cancelPressed) 0.96f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "cancel_scale"
                    )
                    OutlinedButton(
                        onClick = { onCancel(order) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                            .scale(cancelScale),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.error
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
