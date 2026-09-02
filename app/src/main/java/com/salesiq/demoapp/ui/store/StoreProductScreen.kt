package com.salesiq.demoapp.ui.store

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.salesiq.demoapp.ui.Routes
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.zoho.salesiqembed.ZohoSalesIQ

/** Store product detail — add to cart, and start a chat scoped to this specific product. */
@Composable
fun StoreProductScreen(nav: NavController, productId: String) {
    val c = LocalAppColors.current
    val product = productById(productId)
    if (product == null) {
        ScreenScaffold(title = "Product", onBack = { nav.popBackStack() }, backLabel = "Zylker") {}
        return
    }
    val (bg, fg) = tintFor(c, product.tint)

    ScreenScaffold(title = product.name, onBack = { nav.popBackStack() }, backLabel = "Zylker") {
        Box(Modifier.fillMaxWidth().height(170.dp).background(bg, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(product.icon, contentDescription = null, tint = fg, modifier = Modifier.size(72.dp))
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Star, contentDescription = null, tint = c.accent, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("${product.rating} · ${product.reviews} reviews", fontSize = 12.5.sp, color = c.textSecondary)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(money(product.price), fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            if (product.oldPrice != null) {
                Spacer(Modifier.width(10.dp))
                Text(money(product.oldPrice), fontSize = 14.sp, color = c.textSecondary, textDecoration = TextDecoration.LineThrough)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(product.blurb, fontSize = 13.sp, color = c.textSecondary)
        Spacer(Modifier.height(18.dp))
        AppButton("Add to cart", onClick = {
            StoreCart.add(product)
            Toaster.show("Added to cart", ToastTone.Success)
        })
        Spacer(Modifier.height(10.dp))
        AppButton("Ask about this product", variant = ButtonVariant.Secondary, onClick = {
            // Product-scoped chat: attributes + customChatId pin it to this SKU.
            runSdk("Opening chat…") {
                // Tag the conversation with this product's name for operator context.
                ZohoSalesIQ.Conversation.setAttributes { builder -> builder.setName(product.name); builder }
                // Start a Sales chat scoped to this product (customChatId = product id).
                ZohoSalesIQ.Chat.start("I have a question about ${product.name}", product.id, "Sales") { }
            }
        })
        Spacer(Modifier.height(9.dp))
        Text("Opens a chat scoped to this item", fontSize = 11.sp, color = c.textSecondary, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        AppButton("View cart", variant = ButtonVariant.Ghost, onClick = { nav.navigate(Routes.STORE_CART) })
    }
}
