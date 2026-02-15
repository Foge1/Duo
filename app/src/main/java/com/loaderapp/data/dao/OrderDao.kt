package com.loaderapp.data.dao

import androidx.room.*
import com.loaderapp.data.model.Order
import com.loaderapp.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    
    @Query("SELECT * FROM orders ORDER BY dateTime DESC")
    fun getAllOrders(): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE status = :status ORDER BY dateTime DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE workerId = :workerId ORDER BY dateTime DESC")
    fun getOrdersByWorker(workerId: Long): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE dispatcherId = :dispatcherId ORDER BY dateTime DESC")
    fun getOrdersByDispatcher(dispatcherId: Long): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): Order?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long
    
    @Update
    suspend fun updateOrder(order: Order)
    
    @Delete
    suspend fun deleteOrder(order: Order)
    
    @Query("UPDATE orders SET status = :status, workerId = :workerId WHERE id = :orderId")
    suspend fun takeOrder(orderId: Long, workerId: Long, status: OrderStatus)
    
    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus)
}
