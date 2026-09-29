package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.salesiq.demoapp.sdk.DetailCache
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.AppText
import com.salesiq.demoapp.ui.components.Badge
import com.salesiq.demoapp.ui.components.BadgeTone
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.TextTone
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.AppType
import com.zoho.livechat.android.modules.knowledgebase.ui.entities.Resource
import com.zoho.livechat.android.modules.knowledgebase.ui.listeners.OpenResourceListener
import com.zoho.livechat.android.modules.knowledgebase.ui.listeners.ResourceListener
import com.zoho.salesiqembed.ZohoSalesIQ

/** Detail — from KB › Resources. getSingleResource(+ language fallback); Open = KnowledgeBase.open. */
@Composable
fun ResourceDetailScreen(nav: NavController, resourceId: String) {
    var resource by remember { mutableStateOf(DetailCache.resource(resourceId)) }

    LaunchedEffect(resourceId) {
        // Fetch this single article's full details by ID.
        ZohoSalesIQ.KnowledgeBase.getSingleResource(
            ZohoSalesIQ.ResourceType.Articles,
            resourceId,
            true,
            object : ResourceListener {
                override fun onSuccess(fetched: Resource?) {
                    if (fetched != null) resource = fetched
                }

                override fun onFailure(code: Int, message: String?) {
                    // Keep the cached resource; surface only if nothing to show.
                    if (resource == null) failToast("Article unavailable", code, message)
                }
            },
        )
    }

    val article = resource
    val contentText = when (article) {
        is Resource.Article -> article.content
        is Resource.FAQ -> article.answer
        null -> null
    }?.takeIf { it.isNotBlank() } ?: "Open the article in the SDK viewer to read the full content."

    ScreenScaffold(
        title = "Article",
        subtitle = article?.title ?: resourceId,
        onBack = { nav.popBackStack() },
        backLabel = "Knowledge base",
    ) {
        Section(title = "Overview") {
            Card {
                row {
                    ListRow(
                        article?.title ?: "Article",
                        icon = AppIcon.KnowledgeBase,
                        tint = IconTint.Accent,
                        trailing = { article?.category?.name?.let { Badge(it, BadgeTone.Success) } },
                    )
                }
                row {
                    ListRow(
                        article?.creator?.name ?: article?.modifier?.name ?: "—",
                        subtitle = "Author",
                        icon = AppIcon.Visitor,
                        tint = IconTint.Secondary,
                    )
                }
                row {
                    val stats = article?.stats
                    ListRow("Stats", value = "${stats?.liked ?: 0} liked · ${stats?.viewed ?: 0} viewed")
                }
            }
        }

        Section(title = "Content") {
            Card(separated = false) {
                row {
                    AppText(
                        text = contentText,
                        style = AppType.subhead.copy(fontSize = 13.sp),
                        tone = TextTone.Secondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    )
                }
            }
        }

        AppButton("Open in SDK viewer", icon = AppIcon.KnowledgeBase, onClick = {
            // Open this article in the SDK's built-in article viewer.
            ZohoSalesIQ.KnowledgeBase.open(
                ZohoSalesIQ.ResourceType.Articles,
                resourceId,
                object : OpenResourceListener {
                    override fun onSuccess() {
                        Toaster.show("Article opened", ToastTone.Success)
                    }

                    override fun onFailure(code: Int, message: String?) {
                        failToast("Couldn't open article", code, message)
                    }
                },
            )
        })
    }
}
