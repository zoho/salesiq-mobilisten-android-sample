package com.salesiq.demoapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.Field
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ResultBlock
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.TitleTone
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.zoho.livechat.android.modules.deeplinking.models.SalesIQUriScheme

private enum class MatchKind { Exact, Prefix, Suffix, Pattern }
private data class PathRule(val path: String, val kind: MatchKind)

/** Detail — setUriScheme(scheme, hosts, paths): mirrors native SalesIQUriScheme. */
@Composable
fun UriSchemeScreen(nav: NavController) {
    var scheme by remember { mutableStateOf("myapp") }
    val hosts = remember { mutableStateListOf("example.com") }
    var newHost by remember { mutableStateOf("") }
    val rules = remember { mutableStateListOf(PathRule("/orders", MatchKind.Prefix), PathRule("/support/faq", MatchKind.Exact)) }
    var newRule by remember { mutableStateOf("") }
    var newRuleKind by remember { mutableStateOf(MatchKind.Prefix) }
    var result by remember { mutableStateOf<String?>(null) }

    ScreenScaffold(
        title = "URI scheme",
        subtitle = "setUriScheme(scheme, hosts, paths)",
        onBack = { nav.popBackStack() },
        backLabel = "Core",
    ) {
        Section(title = "Scheme") {
            Card { row { Field("Scheme", scheme, { scheme = it }, placeholder = "myapp", autoCapitalize = false) } }
        }

        Section(title = "Hosts") {
            Card {
                hosts.forEach { host ->
                    row { ListRow(host, trailing = null, onClick = { hosts.remove(host) }, subtitle = "Tap to remove") }
                }
                row { Field("New host", newHost, { newHost = it }, placeholder = "api.example.com", autoCapitalize = false) }
                row {
                    ListRow("Add host", icon = AppIcon.Plus, tint = IconTint.Primary, titleTone = TitleTone.Brand, onClick = {
                        if (newHost.isNotBlank()) { hosts.add(newHost.trim()); newHost = "" }
                    })
                }
            }
        }

        Section(title = "Path matchers") {
            Card {
                rules.forEach { rule ->
                    row { ListRow(rule.path, subtitle = rule.kind.name, onClick = { rules.remove(rule) }) }
                }
                row { Field("New path", newRule, { newRule = it }, placeholder = "/checkout", autoCapitalize = false) }
                row {
                    ListRow("Match kind", value = newRuleKind.name, chevron = true, onClick = {
                        newRuleKind = when (newRuleKind) {
                            MatchKind.Exact -> MatchKind.Prefix
                            MatchKind.Prefix -> MatchKind.Suffix
                            MatchKind.Suffix -> MatchKind.Pattern
                            MatchKind.Pattern -> MatchKind.Exact
                        }
                    })
                }
                row {
                    ListRow("Add rule", subtitle = "exact / prefix / suffix / pattern", icon = AppIcon.Plus, tint = IconTint.Primary, titleTone = TitleTone.Brand, onClick = {
                        if (newRule.isNotBlank()) { rules.add(PathRule(newRule.trim(), newRuleKind)); newRule = "" }
                    })
                }
            }
        }

        AppButton("Apply URI scheme", icon = AppIcon.Link, onClick = {
            // Build a deep-link scheme (e.g. "myapp://") the SDK should recognize.
            val uriScheme = SalesIQUriScheme(scheme.trim())
            // Register which hosts belong to this scheme.
            uriScheme.addHosts(*hosts.toTypedArray())
            rules.forEach { rule ->
                val matcher = when (rule.kind) {
                    MatchKind.Exact -> SalesIQUriScheme.PathMatcher.Exact(rule.path)
                    MatchKind.Prefix -> SalesIQUriScheme.PathMatcher.Prefix(rule.path)
                    MatchKind.Suffix -> SalesIQUriScheme.PathMatcher.Suffix(rule.path)
                    MatchKind.Pattern -> SalesIQUriScheme.PathMatcher.Pattern(rule.path)
                }
                // Register a path rule (exact/prefix/suffix/pattern) for this scheme.
                uriScheme.addPaths(matcher)
            }
            // Hand the finished scheme to the SDK so it can handle matching deep links.
            com.zoho.salesiqembed.ZohoSalesIQ.setUriScheme(uriScheme)
            result = jsonOf("scheme" to scheme.trim(), "hosts" to hosts.size, "paths" to rules.size)
            Toaster.show("URI scheme applied", ToastTone.Success)
        })

        result?.let { ResultBlock(text = it, label = "Applied") }
    }
}
