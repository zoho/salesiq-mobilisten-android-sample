package com.zoho.salesiq.mobilisten.showcase.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.zoho.livechat.android.modules.knowledgebase.ui.entities.Resource
import com.zoho.livechat.android.modules.knowledgebase.ui.listeners.ResourcesListener
import com.zoho.salesiq.mobilisten.showcase.sdk.DetailCache
import com.zoho.salesiq.mobilisten.showcase.ui.Routes
import com.zoho.salesiq.mobilisten.showcase.ui.components.AppIcon
import com.zoho.salesiq.mobilisten.showcase.ui.components.Card
import com.zoho.salesiq.mobilisten.showcase.ui.components.IconTint
import com.zoho.salesiq.mobilisten.showcase.ui.components.ListRow
import com.zoho.salesiq.mobilisten.showcase.ui.components.ScreenScaffold
import com.zoho.salesiq.mobilisten.showcase.ui.components.StateRows
import com.zoho.salesiqembed.ZohoSalesIQ

/** Detail — tapping a category / resource-department lists its resources (getResources filtered). */
@Composable
fun CategoryDrillInScreen(nav: NavController, title: String, categoryId: String) {
    var resources by remember { mutableStateOf<List<Resource>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(categoryId, reloadKey) {
        loading = true
        error = null
        // categoryId may be blank (department drill-in with no category filter).
        val category = categoryId.takeIf { it.isNotBlank() }
        // Fetch the articles belonging to this category (or all, if no category filter).
        ZohoSalesIQ.KnowledgeBase.getResources(
            ZohoSalesIQ.ResourceType.Articles,
            null,
            category,
            null,
            true,
            1,
            99,
            object : ResourcesListener {
                override fun onSuccess(articles: List<Resource>, moreDataAvailable: Boolean) {
                    resources = articles
                    DetailCache.resources = DetailCache.resources + articles
                    loading = false
                }

                override fun onFailure(code: Int, message: String?) {
                    error = message ?: "Failed to load resources"
                    loading = false
                }
            }
        )
    }

    ScreenScaffold(
        title = title.ifBlank { "Category" },
        subtitle = if (loading) "Loading…" else "${resources.size} articles in this category",
        onBack = { nav.popBackStack() },
        backLabel = "Knowledge base",
    ) {
        Card {
            StateRows(
                loading = loading,
                error = error,
                empty = resources.isEmpty(),
                onRetry = { reloadKey++ },
                skeletonRows = 5,
                emptyIcon = AppIcon.Article,
                emptyTitle = "No articles here",
                emptySubtitle = "This category has no published articles",
            ) {
                resources.forEach { article ->
                    row {
                        ListRow(
                            article.title ?: "Article",
                            subtitle = article.category?.name ?: title,
                            icon = AppIcon.Article,
                            tint = IconTint.Accent,
                            chevron = true,
                            onClick = { nav.navigate(Routes.resourceDetail(article.id)) },
                        )
                    }
                }
            }
        }
    }
}
