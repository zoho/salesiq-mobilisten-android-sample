package com.salesiq.demoapp.ui.store

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Earbuds
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.vector.ImageVector

/** Colour family for a product's placeholder image tile. */
enum class StoreTint { Primary, Secondary, Accent }

data class Product(
    val id: String,
    val name: String,
    val price: Int,
    val oldPrice: Int?,
    val rating: Double,
    val reviews: Int,
    val blurb: String,
    val icon: ImageVector,
    val tint: StoreTint,
)

data class Order(
    val id: String,
    val productId: String,
    val status: String,
    val placed: String,
    val eta: String,
)

val storeProducts = listOf(
    Product("ZY-HP1", "Wireless headphones", 129, 159, 4.6, 2318, "Active noise cancellation, 30-hour battery, USB-C fast charge.", Icons.Outlined.Headphones, StoreTint.Primary),
    Product("ZY-WT2", "Smart watch", 199, null, 4.4, 1102, "AMOLED display, GPS, heart-rate and SpO2 tracking, 7-day battery.", Icons.Outlined.Watch, StoreTint.Secondary),
    Product("ZY-SP3", "Bluetooth speaker", 89, 99, 4.7, 845, "360° sound, IP67 waterproof, 20-hour playtime.", Icons.Outlined.Speaker, StoreTint.Accent),
    Product("ZY-EB4", "Wireless earbuds", 59, null, 4.3, 3760, "Compact, sweat-resistant, wireless charging case.", Icons.Outlined.Earbuds, StoreTint.Primary),
    Product("ZY-PC5", "Phone case", 19, null, 4.1, 512, "Shock-absorbing bumper with a matte finish.", Icons.Outlined.Smartphone, StoreTint.Secondary),
    Product("ZY-CM6", "Action camera", 249, 279, 4.5, 289, "4K60 video, stabilization, waterproof to 10m.", Icons.Outlined.CameraAlt, StoreTint.Accent),
)

val storeOrders = listOf(
    Order("A-1024", "ZY-HP1", "Shipped", "Jul 3", "Arriving Jul 9"),
    Order("A-1009", "ZY-SP3", "Delivered", "Jun 21", "Delivered Jun 25"),
)

fun productById(id: String): Product? = storeProducts.firstOrNull { it.id == id }

data class CartItem(val product: Product, val qty: Int)

/** In-memory cart backed by a snapshot list so Compose observes changes. */
object StoreCart {
    val items = mutableStateListOf<CartItem>()
    val count: Int get() = items.sumOf { it.qty }
    val total: Int get() = items.sumOf { it.product.price * it.qty }

    fun add(product: Product) {
        val existingIndex = items.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) items[existingIndex] = items[existingIndex].copy(qty = items[existingIndex].qty + 1)
        else items.add(CartItem(product, 1))
    }

    fun remove(id: String) = items.removeAll { it.product.id == id }
    fun clear() = items.clear()
}
