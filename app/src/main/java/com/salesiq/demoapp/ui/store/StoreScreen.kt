package com.salesiq.demoapp.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.salesiq.demoapp.ui.Routes
import com.salesiq.demoapp.ui.screens.failToast
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.Field
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.SwitchRow
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.AppColors
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.zoho.livechat.android.modules.visitor.models.SalesIQVisitorProfile
import com.zoho.salesiq.mobilisten.calls.apis.ZohoSalesIQCalls
import com.zoho.salesiqembed.ZohoSalesIQ

fun money(n: Int) = "\$$n"

fun tintFor(c: AppColors, t: StoreTint): Pair<Color, Color> = when (t) {
    StoreTint.Secondary -> c.tintSecondary to c.secondary
    StoreTint.Accent -> c.tintAccent to c.accent
    StoreTint.Primary -> c.tintPrimary to c.primary
}

/** Fires an SDK call, toasting failure; safe before the SDK is live. */
fun runSdk(label: String, fn: () -> Unit) {
    try {
        fn()
        if (label.isNotEmpty()) Toaster.show(label, ToastTone.Success)
    } catch (error: Throwable) {
        // Common cause before init is missing keys; still surface the real reason + log it.
        failToast("Add keys in Settings to run live", error)
    }
}

/** Zylker demo store — a self-contained shopping app; additive, its own route. */
@Composable
fun StoreScreen(nav: NavController) {
    val c = LocalAppColors.current
    var tab by remember { mutableIntStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Keep the floating chat launcher visible on the store.
        runSdk("") { ZohoSalesIQ.Launcher.show(ZohoSalesIQ.Launcher.VisibilityMode.ALWAYS) }
        // Tell the SDK the visitor is on the store page, for operator context.
        runSdk("") { ZohoSalesIQ.Tracking.setPageTitle("Zylker store") }
    }

    val tabs = listOf(
        Triple("Shop", Icons.Outlined.Storefront, 0),
        Triple("Orders", Icons.Outlined.Inventory2, 1),
        Triple("Help", Icons.Outlined.SupportAgent, 2),
        Triple("Account", Icons.Outlined.PersonOutline, 3),
    )

    Scaffold(
        containerColor = c.page,
        floatingActionButton = {
            FloatingActionButton(
                // Start a support chat when the visitor taps the floating help button.
                onClick = {
                    runSdk("Opening chat…") {
                        ZohoSalesIQ.Chat.start(
                            "Hi, I need help with Zylker",
                            null,
                            "Support"
                        ) { }
                    }
                },
                containerColor = c.primary,
            ) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Chat",
                    tint = Color.White
                )
            }
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(c.card)
                    .padding(top = 8.dp, bottom = 16.dp),
            ) {
                tabs.forEach { (label, icon, index) ->
                    val active = tab == index
                    Column(
                        Modifier
                            .weight(1f)
                            .clickable { tab = index },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = if (active) c.primary else c.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            label,
                            fontSize = 10.5.sp,
                            color = if (active) c.primary else c.textSecondary
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Zylker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = "Cart",
                        tint = c.textPrimary,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { nav.navigate(Routes.STORE_CART) }
                            .padding(8.dp),
                    )
                    if (StoreCart.count > 0) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .background(c.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Text("${StoreCart.count}", fontSize = 10.sp, color = Color.White) }
                    }
                }
            }
            when (tab) {
                0 -> ShopTab(nav, c)
                1 -> OrdersTab(nav, c)
                2 -> HelpTab(c)
                else -> AccountTab(c)
            }
        }
    }
}

@Composable
private fun ShopTab(nav: NavController, c: AppColors) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(c.card, RoundedCornerShape(11.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = c.textSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Search products", fontSize = 13.sp, color = c.textSecondary)
        }
        Spacer(Modifier.height(14.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(c.tintPrimary, RoundedCornerShape(14.dp))
                .padding(15.dp)
        ) {
            Text(
                "Summer sale · up to 20% off",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.primary
            )
            Text("Free delivery over \$50", fontSize = 12.sp, color = c.primary)
        }
        Spacer(Modifier.height(16.dp))
        storeProducts.chunked(2).forEach { pair ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                pair.forEachIndexed { i, product ->
                    if (i == 1) Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        ProductCard(product, c) {
                            nav.navigate(
                                Routes.storeProduct(
                                    product.id
                                )
                            )
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(Modifier.width(12.dp)); Box(Modifier.weight(1f)) {}
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun ProductCard(product: Product, c: AppColors, onClick: () -> Unit) {
    val (bg, fg) = tintFor(c, product.tint)
    Column(
        Modifier
            .fillMaxWidth()
            .background(c.card, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(78.dp)
                .background(bg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(product.icon, contentDescription = null, tint = fg, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(product.name, fontSize = 12.5.sp, color = c.textPrimary)
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                money(product.price),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary
            )
            if (product.oldPrice != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    money(product.oldPrice),
                    fontSize = 11.sp,
                    color = c.textSecondary,
                    textDecoration = TextDecoration.LineThrough
                )
            }
        }
    }
}

@Composable
private fun OrdersTab(nav: NavController, c: AppColors) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Your orders",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = c.textPrimary
        )
        Spacer(Modifier.height(12.dp))
        storeOrders.forEach { order ->
            val product = productById(order.productId) ?: return@forEach
            val (bg, fg) = tintFor(c, product.tint)
            val delivered = order.status == "Delivered"
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(c.card, RoundedCornerShape(13.dp))
                    .clickable { nav.navigate(Routes.storeOrderDetail(order.id)) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(46.dp)
                        .background(bg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        product.icon,
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(product.name, fontSize = 13.sp, color = c.textPrimary)
                    Text("#${order.id} · ${order.placed}", fontSize = 12.sp, color = c.textSecondary)
                }
                Box(
                    Modifier
                        .background(
                            if (delivered) c.tintSecondary else c.tintPrimary,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                ) {
                    Text(
                        order.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (delivered) c.secondary else c.primary
                    )
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun HelpTab(c: AppColors) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Warm up the knowledge base by pre-fetching help articles.
        runSdk("") {
            ZohoSalesIQ.KnowledgeBase.getResources(
                ZohoSalesIQ.ResourceType.Articles,
                null,
                null,
                null,
                false,
                1,
                99,
                object :
                    com.zoho.livechat.android.modules.knowledgebase.ui.listeners.ResourcesListener {
                    override fun onSuccess(
                        articles: List<com.zoho.livechat.android.modules.knowledgebase.ui.entities.Resource>,
                        moreDataAvailable: Boolean
                    ) {
                    }

                    override fun onFailure(code: Int, message: String?) {
                        // Best-effort warm-up prefetch; failures are intentionally ignored.
                    }
                })
        }
    }
    val topics = listOf(
        "Track or change my delivery",
        "Returns and refunds",
        "Payment and billing",
    )
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "How can we help?",
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = c.textPrimary
        )
        Spacer(Modifier.height(14.dp))
        Card {
            topics.forEach { topic ->
                row {
                    ListRow(
                        topic,
                        icon = AppIcon.Article,
                        tint = IconTint.Accent,
                        chevron = true,
                        onClick = {
                            // Start a support chat pre-filled with the chosen help topic.
                            runSdk("Opening chat…") {
                                ZohoSalesIQ.Chat.start(
                                    "I need help: $topic",
                                    null,
                                    "Support"
                                ) { }
                            }
                        })
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(c.tintPrimary, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                "Still need help?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.primary
            )
            Text("Our team replies in a few minutes.", fontSize = 12.sp, color = c.primary)
            Spacer(Modifier.height(12.dp))
            // Start a live support chat.
            AppButton(
                "Start live chat",
                onClick = {
                    runSdk("Opening chat…") {
                        ZohoSalesIQ.Chat.start(
                            "Hi, I have a question",
                            null,
                            "Support"
                        ) { }
                    }
                })
            Spacer(Modifier.height(10.dp))
            // Place a call to an operator for a callback.
            AppButton(
                "Request a callback",
                variant = ButtonVariant.Secondary,
                onClick = {
                    runSdk("Callback requested") {
                        ZohoSalesIQCalls.start(
                            null,
                            true,
                            null
                        ) { }
                    }
                })
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun AccountTab(c: AppColors) {
    var name by remember { mutableStateOf("Alex Rivera") }
    var email by remember { mutableStateOf("alex.rivera@example.com") }
    var notify by remember { mutableStateOf(true) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(64.dp)
                    .background(c.tintPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                name.ifBlank { "Guest" },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary
            )
        }
        Spacer(Modifier.height(18.dp))
        Card {
            row { Field("Name", name, { name = it }) }
            row { Field("Email", email, { email = it }, autoCapitalize = false) }
        }
        Spacer(Modifier.height(12.dp))
        AppButton("Sign in", onClick = {
            val parts = name.trim().split(" ")
            val profile = SalesIQVisitorProfile(
                salutation = SalesIQVisitorProfile.Salutation.None,
                firstName = parts.firstOrNull()?.ifBlank { null },
                lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else null,
                email = email.trim().ifBlank { null },
                customInfo = mapOf("tier" to "Gold", "lifetimeOrders" to "14"),
            )
            // Send the signed-in shopper's profile to SalesIQ.
            runSdk("Signed in") { ZohoSalesIQ.Visitor.updateProfile(profile) }
        })
        Spacer(Modifier.height(16.dp))
        Card {
            row {
                SwitchRow("Order notifications", notify, {
                    notify = it
                    runSdk(if (it) "Notifications on" else "Notifications off") {
                        // Turn in-app notification banners on or off.
                        if (it) ZohoSalesIQ.Notification.enableInApp() else ZohoSalesIQ.Notification.disableInApp()
                    }
                }, subtitle = "Shipping and delivery alerts")
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}
