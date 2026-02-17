package com.loaderapp.ui.loader

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

enum class LoaderDestination {
    ORDERS, SETTINGS, RATING, HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoaderScreen(
    viewModel: LoaderViewModel,
    userName: String,
    onSwitchRole: () -> Unit,
    onDarkThemeChanged: ((Boolean) -> Unit)? = null
) {
    val availableOrders by viewModel.availableOrders.collectAsState()
    val myOrders by viewModel.myOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState(initial = 0)
    val totalEarnings by viewModel.totalEarnings.collectAsState(initial = null)
    val averageRating by viewModel.averageRating.collectAsState(initial = null)
    
    var selectedTab by remember { mutableStateOf(0) }
    var showSwitchDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf<Order?>(null) }
    var currentDestination by remember { mutableStateOf(LoaderDestination.ORDERS) }
    val tabs = listOf("Доступные", "Мои заказы")
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(240.dp),
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
                        Text(text = userName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Грузчик",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider()

                val primary = MaterialTheme.colorScheme.primary

                @Composable
                fun DrawerItem(label: String, selected: Boolean, onClick: () -> Unit) {
                    val textColor = if (selected) primary else MaterialTheme.colorScheme.onSurfaceVariant

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

                DrawerItem("Заказы", currentDestination == LoaderDestination.ORDERS) {
                    currentDestination = LoaderDestination.ORDERS
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Рейтинг", currentDestination == LoaderDestination.RATING) {
                    currentDestination = LoaderDestination.RATING
                    scope.launch { drawerState.close() }
                }
                DrawerItem("История", currentDestination == LoaderDestination.HISTORY) {
                    currentDestination = LoaderDestination.HISTORY
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Настройки", currentDestination == LoaderDestination.SETTINGS) {
                    currentDestination = LoaderDestination.SETTINGS
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
            label = "loader_nav"
        ) { destination ->
            when (destination) {
                LoaderDestination.ORDERS -> {
                    LoaderOrdersContent(
                        availableOrders = availableOrders,
                        myOrders = myOrders,
                        isLoading = isLoading,
                        userName = userName,
                        selectedTab = selectedTab,
                        tabs = tabs,
                        onTabSelected = { selectedTab = it },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onTakeOrder = { viewModel.takeOrder(it) },
                        onCompleteOrder = { order ->
                            viewModel.completeOrder(order)
                            showRatingDialog = order
                        }
                    )
                }
                LoaderDestination.SETTINGS -> {
                    SettingsScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { currentDestination = LoaderDestination.ORDERS },
                        onDarkThemeChanged = onDarkThemeChanged
                    )
                }
                LoaderDestination.RATING -> {
                    RatingScreen(
                        userName = userName,
                        userRating = averageRating?.toDouble() ?: 5.0,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { currentDestination = LoaderDestination.ORDERS },
                        completedCount = completedCount,
                        totalEarnings = totalEarnings ?: 0.0,
                        averageRating = averageRating ?: 0f,
                        isDispatcher = false
                    )
                }
                LoaderDestination.HISTORY -> {
                    HistoryScreen(
                        orders = myOrders,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { currentDestination = LoaderDestination.ORDERS }
                    )
                }
            }
        }
    }
    
    // Диалог оценки после завершения заказа
    showRatingDialog?.let { order ->
        RateOrderDialog(
            onDismiss = { showRatingDialog = null },
            onRate = { rating ->
                viewModel.rateOrder(order.id, rating)
                showRatingDialog = null
            }
        )
    }
    
    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            title = { Text("Сменить роль?") },
            text = { Text("Вы хотите выйти из режима грузчика?") },
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

@Composable
fun RateOrderDialog(
    onDismiss: () -> Unit,
    onRate: (Float) -> Unit
) {
    var selectedRating by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Оцените заказ") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Как прошёл заказ?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { selectedRating = i },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$i звезд",
                                tint = if (i <= selectedRating) Color(0xFFFFC107)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                if (selectedRating > 0) {
                    Text(
                        text = when (selectedRating) {
                            1 -> "Плохо"
                            2 -> "Неплохо"
                            3 -> "Хорошо"
                            4 -> "Очень хорошо"
                            5 -> "Отлично!"
                            else -> ""
                        },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedRating > 0) onRate(selectedRating.toFloat()) },
                enabled = selectedRating > 0
            ) {
                Text("Отправить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Пропустить") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoaderOrdersContent(
    availableOrders: List<Order>,
    myOrders: List<Order>,
    isLoading: Boolean,
    userName: String,
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    onMenuClick: () -> Unit,
    onTakeOrder: (Order) -> Unit,
    onCompleteOrder: (Order) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Грузчик")
                            Text(
                                text = userName,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Меню")
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> AvailableOrdersList(orders = availableOrders, onTakeOrder = onTakeOrder)
                1 -> MyOrdersList(orders = myOrders, onCompleteOrder = onCompleteOrder)
            }
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun AvailableOrdersList(
    orders: List<Order>,
    onTakeOrder: (Order) -> Unit
) {
    if (orders.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WorkOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Нет доступных заказов",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Новые заказы появятся здесь",
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
            items(orders, key = { it.id }) { order ->
                AvailableOrderCard(order = order, onTake = { onTakeOrder(order) })
            }
        }
    }
}

@Composable
fun MyOrdersList(
    orders: List<Order>,
    onCompleteOrder: (Order) -> Unit
) {
    if (orders.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AssignmentTurnedIn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "У вас нет заказов",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Возьмите заказ на вкладке «Доступные»",
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
            items(orders, key = { it.id }) { order ->
                MyOrderCard(order = order, onComplete = { onCompleteOrder(order) })
            }
        }
    }
}

@Composable
fun AvailableOrderCard(
    order: Order,
    onTake: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val accentColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                Text(text = order.address, fontSize = 16.sp, fontWeight = FontWeight.Bold)

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
                        fontSize = 20.sp,
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

                var pressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (pressed) 0.96f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "take_scale"
                )
                Button(
                    onClick = onTake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .scale(scale),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Взять заказ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun MyOrderCard(
    order: Order,
    onComplete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val accentColor = when (order.status) {
        OrderStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
        OrderStatus.TAKEN, OrderStatus.IN_PROGRESS -> Color(0xFFE67E22)
        OrderStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    LoaderStatusChip(status = order.status)
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
                            text = " · ~${(order.pricePerHour * order.estimatedHours).toInt()} ₽",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Показываем оценку если уже поставлена
                order.workerRating?.let { rating ->
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { i ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i < rating.toInt()) Color(0xFFFFC107) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = " Ваша оценка",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (order.status == OrderStatus.TAKEN) {
                    var pressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (pressed) 0.96f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "complete_scale"
                    )
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .scale(scale),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Завершить", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun LoaderStatusChip(status: OrderStatus) {
    val (text, color) = when (status) {
        OrderStatus.AVAILABLE -> "Доступен" to MaterialTheme.colorScheme.primary
        OrderStatus.TAKEN -> "В работе" to Color(0xFFE67E22)
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

// Keep StatusChip alias for HistoryScreen compatibility
@Composable
fun StatusChip(status: OrderStatus) = LoaderStatusChip(status)
