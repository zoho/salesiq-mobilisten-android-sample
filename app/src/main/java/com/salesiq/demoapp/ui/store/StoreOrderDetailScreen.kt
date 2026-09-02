package com.salesiq.demoapp.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.zoho.salesiqembed.ZohoSalesIQ

/** Store order detail — shows delivery progress and starts a chat scoped to this order. */
@Composable
fun StoreOrderDetailScreen(nav: NavController, orderId: String) {
    val c = LocalAppColors.current
    val order = storeOrders.firstOrNull { it.id == orderId }
    if (order == null) {
        ScreenScaffold(title = "Order", onBack = { nav.popBackStack() }, backLabel = "Zylker") {}
        return
    }
    val product = productById(order.productId)
    if (product == null) {
        ScreenScaffold(title = "Order", onBack = { nav.popBackStack() }, backLabel = "Zylker") {}
        return
    }
    val (bg, fg) = tintFor(c, product.tint)
    val delivered = order.status == "Delivered"
    val steps = listOf(
        "Ordered · ${order.placed}" to true,
        "Shipped · Jul 5" to (order.status != "Processing"),
        order.eta to delivered,
    )

    val getHelp = {
        // Start a Support chat scoped to this order (customChatId = order id).
        runSdk("Opening chat…") { ZohoSalesIQ.Chat.start("I need help with order ${order.id}", order.id, "Support") { } }
    }

    ScreenScaffold(title = "Order #${order.id}", onBack = { nav.popBackStack() }, backLabel = "Zylker") {
        Card {
            row {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).background(bg, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(product.icon, contentDescription = null, tint = fg, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(product.name, fontSize = 13.sp, color = c.textPrimary)
                        Text("Qty 1 · ${money(product.price)}", fontSize = 12.sp, color = c.textSecondary)
                    }
                    Box(Modifier.background(if (delivered) c.tintSecondary else c.tintPrimary, RoundedCornerShape(8.dp)).padding(horizontal = 9.dp, vertical = 3.dp)) {
                        Text(order.status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (delivered) c.secondary else c.primary)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.padding(start = 6.dp)) {
            steps.forEachIndexed { i, (label, done) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (done) Icons.Outlined.CheckCircle else Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = if (done) c.secondary else c.textSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(label, fontSize = 12.5.sp, color = if (done) c.textPrimary else c.textSecondary)
                }
                if (i < steps.size - 1) {
                    Box(Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp).size(width = 1.5.dp, height = 14.dp).background(c.border))
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        AppButton("Get help with this order", onClick = getHelp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { AppButton("Track", variant = ButtonVariant.Secondary, onClick = { Toaster.show("Tracking opened", ToastTone.Default) }) }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) { AppButton("Return", variant = ButtonVariant.Secondary, onClick = getHelp) }
        }
    }
}
