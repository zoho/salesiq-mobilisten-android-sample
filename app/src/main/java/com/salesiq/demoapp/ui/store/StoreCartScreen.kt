package com.salesiq.demoapp.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
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
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.zoho.salesiqembed.ZohoSalesIQ

/** Store cart — review items, place an order (fires a checkout trigger), or chat about payment. */
@Composable
fun StoreCartScreen(nav: NavController) {
    val c = LocalAppColors.current
    val items = StoreCart.items

    ScreenScaffold(title = "Your cart", onBack = { nav.popBackStack() }, backLabel = "Zylker") {
        Card {
            if (items.isEmpty()) {
                row { ListRow("Your cart is empty", subtitle = "Browse the store and add something you like", enabled = false) }
            } else {
                items.forEach { item ->
                    val (bg, fg) = tintFor(c, item.product.tint)
                    row {
                        ListRow(
                            item.product.name,
                            subtitle = "Qty ${item.qty} · ${money(item.product.price)}",
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(34.dp).background(bg, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                                        Icon(item.product.icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = c.textSecondary, modifier = Modifier.size(18.dp).clickable { StoreCart.remove(item.product.id) })
                                }
                            },
                        )
                    }
                }
            }
        }

        if (items.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Total", fontSize = 14.sp, color = c.textSecondary, modifier = Modifier.weight(1f))
                Text(money(StoreCart.total), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            }
            Spacer(Modifier.height(12.dp))
            AppButton("Place order", onClick = {
                // A proactive trigger — the SDK offers help at checkout the way a store would.
                runSdk("Order placed — we’ll follow up in chat") {
                    // Fire a "checkout completed" trigger to proactively offer help after ordering.
                    ZohoSalesIQ.Chat.startWithTrigger("checkout_completed", "cart", "Support") { }
                }
                StoreCart.clear()
                nav.popBackStack()
            })
            Spacer(Modifier.height(10.dp))
            AppButton("Chat about payment", variant = ButtonVariant.Secondary, onClick = {
                // Start a Support chat scoped to the cart (customChatId = "cart").
                runSdk("Opening chat…") { ZohoSalesIQ.Chat.start("I have a question about payment", "cart", "Support") { } }
            })
        } else {
            Spacer(Modifier.height(12.dp))
            AppButton("Continue shopping", variant = ButtonVariant.Secondary, onClick = { nav.popBackStack() })
        }
    }
}
