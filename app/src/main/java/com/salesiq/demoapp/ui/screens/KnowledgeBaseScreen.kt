package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.salesiq.demoapp.sdk.DetailCache
import com.salesiq.demoapp.ui.Routes
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.Field
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ResultBlock
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.SegmentedControl
import com.salesiq.demoapp.ui.components.SwitchRow
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.AppType
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.salesiq.demoapp.ui.theme.Radius
import com.zoho.livechat.android.modules.knowledgebase.ui.entities.Resource
import com.zoho.livechat.android.modules.knowledgebase.ui.entities.ResourceCategory
import com.zoho.livechat.android.modules.knowledgebase.ui.entities.ResourceDepartment
import com.zoho.livechat.android.modules.knowledgebase.ui.listeners.OpenResourceListener
import com.zoho.livechat.android.modules.knowledgebase.ui.listeners.ResourceCategoryListener
import com.zoho.livechat.android.modules.knowledgebase.ui.listeners.ResourceDepartmentsListener
import com.zoho.livechat.android.modules.knowledgebase.ui.listeners.ResourcesListener
import com.zoho.salesiqembed.ZohoSalesIQ

private data class RecentResource(val id: String, val title: String, val category: String)

/**
 * Per-resource-type display content. The KnowledgeBase APIs are keyed by
 * ZohoSalesIQ.ResourceType, so the screen keeps one KbContent per type and swaps to whichever
 * the segmented control selects — labels, sample data, and icon all follow the selection.
 */
private data class KbContent(
    val label: String,        // Title-case for headings/toasts: "Articles" / "FAQs"
    val plural: String,       // Mid-sentence: "articles" / "FAQs"
    val icon: AppIcon,
    val recentlyViewed: List<RecentResource>,
)

private val KB_CONTENT: Map<ZohoSalesIQ.ResourceType, KbContent> = mapOf(
    ZohoSalesIQ.ResourceType.Articles to KbContent(
        label = "Articles",
        plural = "articles",
        icon = AppIcon.Article,
        recentlyViewed = listOf(
            RecentResource("a1", "Getting started with live chat", "Onboarding"),
            RecentResource("a2", "Setting up push notifications", "Configuration"),
            RecentResource("a3", "Routing chats to departments", "Advanced"),
        ),
    ),
    ZohoSalesIQ.ResourceType.FAQs to KbContent(
        label = "FAQs",
        plural = "FAQs",
        icon = AppIcon.KnowledgeBase,
        recentlyViewed = listOf(
            RecentResource("f1", "How do I reset my password?", "Account"),
            RecentResource("f2", "Supported file types for attachments", "Messaging"),
            RecentResource("f3", "Why am I not receiving notifications?", "Notifications"),
        ),
    ),
)

/** Screen 07 — browse and search self-service articles / FAQs, categories, and departments. */
@Composable
fun KnowledgeBaseScreen(nav: NavController) {
    val c = LocalAppColors.current
    // The resource type every action on this screen targets. The segmented control switches it,
    // and `content` gives the labels + sample data for the selected type.
    var resourceType by remember { mutableStateOf(ZohoSalesIQ.ResourceType.Articles) }
    val content = KB_CONTENT.getValue(resourceType)
    var search by remember { mutableStateOf("") }
    var showKb by remember { mutableStateOf(true) }
    var groupByCategory by remember { mutableStateOf(true) }
    var combineDepts by remember { mutableStateOf(false) }
    var recentCount by remember { mutableStateOf("5") }
    var resourceId by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<ResourceCategory>>(emptyList()) }
    var departments by remember { mutableStateOf<List<ResourceDepartment>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<Resource>>(emptyList()) }
    var result by remember { mutableStateOf<String?>(null) }

    ScreenScaffold(
        title = "Knowledge Base",
        subtitle = "Articles, categories, and departments",
        onBack = { nav.popBackStack() },
    ) {
        // Pick which knowledge-base resource type every action below targets.
        SegmentedControl(
            segments = listOf(
                ZohoSalesIQ.ResourceType.Articles to "Articles",
                ZohoSalesIQ.ResourceType.FAQs to "FAQs",
            ),
            selected = resourceType,
            onSelect = {
                resourceType = it
                // Clear results from the previous type so the list matches the new selection.
                searchResults = emptyList()
                categories = emptyList()
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.card))
                .background(c.card)
                .border(1.dp, c.border, RoundedCornerShape(Radius.card))
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                AppIcon.Search.vector,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(18.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (search.isEmpty()) {
                    Text(
                        "Search ${content.plural}",
                        style = AppType.body.copy(fontSize = 14.5.sp),
                        color = c.textTertiary
                    )
                }
                BasicTextField(
                    value = search,
                    onValueChange = { search = it },
                    textStyle = AppType.body.copy(fontSize = 14.5.sp, color = c.textPrimary),
                    cursorBrush = SolidColor(c.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AppButton(
            "Search ${content.plural}",
            icon = AppIcon.Search,
            variant = ButtonVariant.Secondary,
            onClick = {
                // Search knowledge-base resources of the selected type matching the query.
                ZohoSalesIQ.KnowledgeBase.getResources(
                    resourceType,
                    null,
                    null,
                    search.trim().ifBlank { null },
                    true,
                    1,
                    25,
                    object : ResourcesListener {
                        override fun onSuccess(
                            resources: List<Resource>,
                            moreDataAvailable: Boolean
                        ) {
                            searchResults = resources
                            DetailCache.resources = DetailCache.resources + resources
                            result = jsonOf(
                                "query" to search.trim(),
                                "count" to resources.size,
                                "moreDataAvailable" to moreDataAvailable
                            )
                            Toaster.show("Found ${resources.size} ${content.plural}", ToastTone.Success)
                        }

                        override fun onFailure(code: Int, message: String?) {
                            searchResults = emptyList()
                            result =
                                jsonOf("errorCode" to code, "message" to (message ?: "Unknown"))
                            Toaster.show("Search failed", ToastTone.Danger)
                        }
                    })
            })

        if (searchResults.isNotEmpty()) {
            Section(title = "Results") {
                Card {
                    searchResults.forEach { resource ->
                        row {
                            ListRow(
                                resource.title ?: content.label,
                                subtitle = resource.category?.name ?: content.label,
                                icon = content.icon,
                                tint = IconTint.Accent,
                                chevron = true,
                                onClick = { nav.navigate(Routes.resourceDetail(resource.id)) },
                            )
                        }
                    }
                }
            }
        }

        Section(title = "Visibility & behavior") {
            Card {
                row {
                    SwitchRow("Show knowledge base", showKb, {
                        showKb = it
                        // Show or hide the articles widget on the SDK's home screen.
                        ZohoSalesIQ.Homepage.setVisibility(ZohoSalesIQ.Homepage.Widget.ARTICLES, it)
                    })
                }
                row {
                    SwitchRow("Group by category", groupByCategory, {
                        groupByCategory = it
                        // Group the list by category instead of a flat list.
                        ZohoSalesIQ.KnowledgeBase.categorize(resourceType, it)
                    })
                }
                row {
                    SwitchRow("Combine departments", combineDepts, {
                        combineDepts = it
                        // Merge resources from all departments into one combined list.
                        ZohoSalesIQ.KnowledgeBase.combineDepartments(resourceType, it)
                    }, subtitle = "Merge ${content.plural} across departments")
                }
                row {
                    ListRow("${content.label} enabled?", value = "Check", chevron = true, onClick = {
                        // Check whether the selected module is turned on in the portal.
                        val enabled = ZohoSalesIQ.KnowledgeBase.isEnabled(resourceType)
                        result = jsonOf("enabled" to enabled)
                        Toaster.show(
                            if (enabled) "${content.label} enabled" else "${content.label} disabled",
                            ToastTone.Default
                        )
                    })
                }
            }
        }

        Section(
            title = "Recently viewed",
            footer = "Set how many recently-viewed ${content.plural} the SDK keeps."
        ) {
            Card {
                content.recentlyViewed.forEach { item ->
                    row {
                        ListRow(
                            item.title,
                            subtitle = item.category,
                            icon = content.icon,
                            tint = IconTint.Accent,
                            chevron = true,
                            onClick = {
                                // Open a specific resource in the SDK's viewer by ID.
                                ZohoSalesIQ.KnowledgeBase.open(
                                    resourceType,
                                    item.id,
                                    object : OpenResourceListener {
                                        override fun onSuccess() {
                                            result = jsonOf("opened" to item.id)
                                        }

                                        override fun onFailure(code: Int, message: String?) {
                                            result = jsonOf(
                                                "errorCode" to code,
                                                "message" to (message ?: "Unknown")
                                            )
                                        }
                                    })
                            })
                    }
                }
                row {
                    Field(
                        "Recently-viewed count",
                        recentCount,
                        { recentCount = it },
                        placeholder = "5"
                    )
                }
                row {
                    ListRow(
                        "Apply count",
                        icon = AppIcon.KnowledgeBase,
                        tint = IconTint.Primary,
                        onClick = {
                            val limit = recentCount.trim().toIntOrNull()
                            if (limit == null) {
                                Toaster.show("Enter a number", ToastTone.Danger)
                                return@ListRow
                            }
                            // Set how many recently-viewed resources the SDK remembers.
                            ZohoSalesIQ.KnowledgeBase.setRecentlyViewedCount(limit)
                            Toaster.show("Recently-viewed count set to $limit", ToastTone.Success)
                        })
                }
            }
        }

        Section(
            title = "Single resource",
            footer = "Fetch one resource by ID (getSingleResource) and open its detail."
        ) {
            Card {
                row {
                    Field(
                        "Resource ID",
                        resourceId,
                        { resourceId = it },
                        placeholder = "e.g. a1",
                        autoCapitalize = false
                    )
                }
                row {
                    ListRow(
                        "Open resource detail",
                        icon = content.icon,
                        tint = IconTint.Primary,
                        chevron = true,
                        onClick = {
                            if (resourceId.isBlank()) {
                                Toaster.show("Enter a resource ID", ToastTone.Danger)
                                return@ListRow
                            }
                            nav.navigate(Routes.resourceDetail(resourceId.trim()))
                        })
                }
            }
        }

        AppButton(
            "Fetch categories",
            icon = AppIcon.KnowledgeBase,
            variant = ButtonVariant.Secondary,
            onClick = {
                // Fetch the list of categories for the selected type.
                ZohoSalesIQ.KnowledgeBase.getCategories(
                    resourceType,
                    null,
                    null,
                    object : ResourceCategoryListener {
                        override fun onSuccess(resourceCategories: List<ResourceCategory>) {
                            categories = resourceCategories
                            result = jsonOf(
                                "count" to resourceCategories.size,
                                "categories" to resourceCategories.joinToString {
                                    it.name ?: it.id
                                })
                            Toaster.show("Categories loaded", ToastTone.Success)
                        }

                        override fun onFailure(code: Int, message: String?) {
                            result =
                                jsonOf("errorCode" to code, "message" to (message ?: "Unknown"))
                            Toaster.show("Failed to load categories", ToastTone.Danger)
                        }
                    })
            })

        if (categories.isNotEmpty()) {
            Section(title = "Categories") {
                Card {
                    categories.forEach { category ->
                        row {
                            ListRow(
                                category.name ?: category.id,
                                icon = AppIcon.KnowledgeBase,
                                tint = IconTint.Primary,
                                chevron = true,
                                onClick = {
                                    nav.navigate(
                                        Routes.categoryDrill(
                                            category.name ?: "Category", category.id
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        AppButton(
            "Fetch resource departments",
            icon = AppIcon.KnowledgeBase,
            variant = ButtonVariant.Secondary,
            onClick = {
                // Fetch the departments that own knowledge-base resources.
                ZohoSalesIQ.KnowledgeBase.getResourceDepartments(object :
                    ResourceDepartmentsListener {
                    override fun onSuccess(resourceDepartments: List<ResourceDepartment>) {
                        departments = resourceDepartments
                        result = jsonOf(
                            "count" to resourceDepartments.size,
                            "departments" to resourceDepartments.joinToString { it.name })
                        Toaster.show("Departments loaded", ToastTone.Success)
                    }

                    override fun onFailure(code: Int, message: String) {
                        result = jsonOf("errorCode" to code, "message" to message)
                        Toaster.show("Failed to load departments", ToastTone.Danger)
                    }
                })
            })

        if (departments.isNotEmpty()) {
            Section(title = "Departments") {
                Card {
                    departments.forEach { department ->
                        row {
                            ListRow(
                                department.name,
                                icon = AppIcon.KnowledgeBase,
                                tint = IconTint.Secondary,
                                chevron = true,
                                onClick = {
                                    nav.navigate(
                                        Routes.categoryDrill(
                                            department.name,
                                            ""
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        result?.let { ResultBlock(text = it, label = "Last result") }
    }
}
