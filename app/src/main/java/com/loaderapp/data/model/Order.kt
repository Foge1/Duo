package com.loaderapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String,
    val dateTime: Long, // timestamp в миллисекундах
    val cargoDescription: String,
    val pricePerHour: Double,
    val status: OrderStatus = OrderStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis(),
    val workerId: Long? = null, // ID грузчика, который взял заказ
    val dispatcherId: Long = 0 // ID диспетчера, который создал заказ
)

enum class OrderStatus {
    AVAILABLE,    // Доступен для взятия
    TAKEN,        // Взят грузчиком
    COMPLETED,    // Выполнен
    CANCELLED     // Отменен
}
