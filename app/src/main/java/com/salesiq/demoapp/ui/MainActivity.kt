package com.salesiq.demoapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.salesiq.demoapp.state.SettingsStore
import com.zoho.salesiqembed.ZohoSalesIQ
import com.salesiq.demoapp.ui.components.ToastHost
import com.salesiq.demoapp.ui.screens.CallDetailScreen
import com.salesiq.demoapp.ui.screens.CallsScreen
import com.salesiq.demoapp.ui.screens.CategoryDrillInScreen
import com.salesiq.demoapp.ui.store.StoreScreen
import com.salesiq.demoapp.ui.store.StoreProductScreen
import com.salesiq.demoapp.ui.store.StoreCartScreen
import com.salesiq.demoapp.ui.store.StoreOrderDetailScreen
import com.salesiq.demoapp.ui.screens.ChatDetailScreen
import com.salesiq.demoapp.ui.screens.ChatScreen
import com.salesiq.demoapp.ui.screens.CoreScreen
import com.salesiq.demoapp.ui.screens.DepartmentPickerScreen
import com.salesiq.demoapp.ui.screens.EventDetailScreen
import com.salesiq.demoapp.ui.screens.EventsScreen
import com.salesiq.demoapp.ui.screens.HomeScreen
import com.salesiq.demoapp.ui.screens.HomepageHelpCenterScreen
import com.salesiq.demoapp.ui.screens.KnowledgeBaseScreen
import com.salesiq.demoapp.ui.screens.LauncherScreen
import com.salesiq.demoapp.ui.screens.NotificationsScreen
import com.salesiq.demoapp.ui.screens.OrdersShowcaseScreen
import com.salesiq.demoapp.ui.screens.RefreshDataScreen
import com.salesiq.demoapp.ui.screens.ResourceDetailScreen
import com.salesiq.demoapp.ui.screens.SettingsScreen
import com.salesiq.demoapp.ui.screens.UriSchemeScreen
import com.salesiq.demoapp.ui.screens.VisitorScreen
import com.salesiq.demoapp.ui.theme.MobilistenTheme

/** Route names for the single-Activity Compose navigation graph. */
object Routes {
    const val HOME = "home"
    const val CORE = "core"
    const val LAUNCHER = "launcher"
    const val VISITOR = "visitor"
    const val CHAT = "chat"
    const val CALLS = "calls"
    const val KNOWLEDGE_BASE = "knowledge_base"
    const val HOMEPAGE = "homepage"
    const val NOTIFICATIONS = "notifications"
    const val EVENTS = "events"
    const val SETTINGS = "settings"

    // Detail / destination / showcase screens.
    const val REFRESH_DATA = "refresh_data"
    const val URI_SCHEME = "uri_scheme"
    const val ORDERS = "orders"
    const val STORE = "store"
    const val STORE_CART = "store_cart"
    const val STORE_PRODUCT = "store_product/{productId}"
    const val STORE_ORDER_DETAIL = "store_order_detail/{orderId}"
    const val CHAT_DETAIL = "chat_detail/{chatId}"
    const val CALL_DETAIL = "call_detail/{callId}"
    const val RESOURCE_DETAIL = "resource_detail/{resourceId}"
    const val CATEGORY_DRILL = "category_drill/{title}/{categoryId}"
    const val DEPARTMENT_PICKER = "department_picker"
    const val EVENT_DETAIL = "event_detail/{id}"

    fun storeProduct(productId: String) = "store_product/$productId"
    fun storeOrderDetail(orderId: String) = "store_order_detail/$orderId"
    fun chatDetail(chatId: String) = "chat_detail/$chatId"
    fun callDetail(callId: String) = "call_detail/$callId"
    fun resourceDetail(resourceId: String) = "resource_detail/$resourceId"
    fun categoryDrill(title: String, categoryId: String) = "category_drill/$title/$categoryId"
    fun eventDetail(id: Long) = "event_detail/$id"
}

/**
 * Human-readable page title per route — reported to SalesIQ so the operator can see the
 * visitor's in-app navigation path. Keyed by the route pattern (destination.route), so the
 * argument-carrying routes are matched by their template (e.g. "store_product/{productId}").
 */
private val PAGE_TITLES: Map<String, String> = mapOf(
    Routes.HOME to "Home",
    Routes.CORE to "Core & configuration",
    Routes.LAUNCHER to "Launcher",
    Routes.VISITOR to "Visitor",
    Routes.CHAT to "Chat",
    Routes.CALLS to "Calls",
    Routes.KNOWLEDGE_BASE to "Knowledge base",
    Routes.HOMEPAGE to "Homepage & help center",
    Routes.NOTIFICATIONS to "Notifications",
    Routes.EVENTS to "Events",
    Routes.SETTINGS to "Settings",
    Routes.REFRESH_DATA to "Refresh data",
    Routes.URI_SCHEME to "URI scheme",
    Routes.ORDERS to "Orders",
    Routes.STORE to "Zylker store",
    Routes.STORE_CART to "Cart",
    Routes.STORE_PRODUCT to "Store product",
    Routes.STORE_ORDER_DETAIL to "Order detail",
    Routes.CHAT_DETAIL to "Chat detail",
    Routes.CALL_DETAIL to "Call detail",
    Routes.RESOURCE_DETAIL to "Resource detail",
    Routes.CATEGORY_DRILL to "Resources",
    Routes.DEPARTMENT_PICKER to "Department picker",
    Routes.EVENT_DETAIL to "Event detail",
)

class MainActivity : ComponentActivity() {
    // Android 13+ (API 33): POST_NOTIFICATIONS is a runtime permission. Registered here and launched from
    // onCreate so push notifications can be displayed. No-op below API 33 (granted at install).
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* OS records the choice */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maybeRequestNotificationPermission()
        enableEdgeToEdge()
        setContent {
            val mode by SettingsStore.themeMode.collectAsState()
            MobilistenTheme(mode = mode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                    ToastHost()
                }
            }
        }
    }

    /** Prompts for POST_NOTIFICATIONS on Android 13+ if not already granted; no-op otherwise. */
    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@androidx.compose.runtime.Composable
private fun AppNavHost() {
    val nav = rememberNavController()

    // Central page-title tracking: report every screen the visitor lands on to SalesIQ, so the
    // operator sees their in-app navigation path. Keyed to the current destination, this fires
    // once per page from one place rather than per-screen. Mirrors the React Native sample.
    val currentEntry by nav.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            ZohoSalesIQ.Tracking.setPageTitle(PAGE_TITLES[route] ?: route)
        }
    }

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(nav) }
        composable(Routes.CORE) { CoreScreen(nav) }
        composable(Routes.LAUNCHER) { LauncherScreen(nav) }
        composable(Routes.VISITOR) { VisitorScreen(nav) }
        composable(Routes.CHAT) { ChatScreen(nav) }
        composable(Routes.CALLS) { CallsScreen(nav) }
        composable(Routes.KNOWLEDGE_BASE) { KnowledgeBaseScreen(nav) }
        composable(Routes.HOMEPAGE) { HomepageHelpCenterScreen(nav) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(nav) }
        composable(Routes.EVENTS) { EventsScreen(nav) }
        composable(Routes.SETTINGS) { SettingsScreen(nav) }

        composable(Routes.REFRESH_DATA) { RefreshDataScreen(nav) }
        composable(Routes.URI_SCHEME) { UriSchemeScreen(nav) }
        composable(Routes.ORDERS) { OrdersShowcaseScreen(nav) }
        composable(Routes.STORE) { StoreScreen(nav) }
        composable(Routes.STORE_CART) { StoreCartScreen(nav) }
        composable(
            Routes.STORE_PRODUCT,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) { entry -> StoreProductScreen(nav, entry.arguments?.getString("productId").orEmpty()) }
        composable(
            Routes.STORE_ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { entry -> StoreOrderDetailScreen(nav, entry.arguments?.getString("orderId").orEmpty()) }
        composable(Routes.DEPARTMENT_PICKER) { DepartmentPickerScreen(nav) }
        composable(
            Routes.CHAT_DETAIL,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
        ) { entry -> ChatDetailScreen(nav, entry.arguments?.getString("chatId").orEmpty()) }
        composable(
            Routes.CALL_DETAIL,
            arguments = listOf(navArgument("callId") { type = NavType.StringType }),
        ) { entry -> CallDetailScreen(nav, entry.arguments?.getString("callId").orEmpty()) }
        composable(
            Routes.RESOURCE_DETAIL,
            arguments = listOf(navArgument("resourceId") { type = NavType.StringType }),
        ) { entry -> ResourceDetailScreen(nav, entry.arguments?.getString("resourceId").orEmpty()) }
        composable(
            Routes.CATEGORY_DRILL,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType },
            ),
        ) { entry ->
            CategoryDrillInScreen(
                nav,
                entry.arguments?.getString("title").orEmpty(),
                entry.arguments?.getString("categoryId").orEmpty(),
            )
        }
        composable(
            Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry -> EventDetailScreen(nav, entry.arguments?.getLong("id") ?: -1L) }
    }
}
