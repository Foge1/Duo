package com.loaderapp.ui.dispatcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.loaderapp.data.model.Order
import com.loaderapp.data.model.OrderStatus
import com.loaderapp.data.repository.AppRepository
import com.loaderapp.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DispatcherViewModel(
    application: Application,
    private val repository: AppRepository,
    private val dispatcherId: Long
) : AndroidViewModel(application) {
    
    private val notificationHelper = NotificationHelper(application)
    
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadOrders()
    }
    
    private fun loadOrders() {
        viewModelScope.launch {
            try {
                repository.getOrdersByDispatcher(dispatcherId).collect { ordersList ->
                    _orders.value = ordersList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки заказов: ${e.message}"
            }
        }
    }
    
    fun createOrder(
        address: String,
        dateTime: Long,
        cargoDescription: String,
        pricePerHour: Double
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val order = Order(
                    address = address,
                    dateTime = dateTime,
                    cargoDescription = cargoDescription,
                    pricePerHour = pricePerHour,
                    status = OrderStatus.AVAILABLE,
                    dispatcherId = dispatcherId
                )
                repository.createOrder(order)
                
                // Отправляем уведомление грузчикам
                notificationHelper.sendNewOrderNotification(address, pricePerHour)
                
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка создания заказа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun cancelOrder(order: Order) {
        viewModelScope.launch {
            try {
                repository.cancelOrder(order.id)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка отмены заказа: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
