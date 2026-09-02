package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.salesiq.demoapp.ui.theme.LocalAppColors
import androidx.navigation.NavController
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
import com.salesiq.demoapp.ui.components.TitleTone
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.zoho.livechat.android.SIQVisitorLocation
import com.zoho.livechat.android.listeners.RegisterListener
import com.zoho.livechat.android.listeners.UnRegisterListener
import com.zoho.livechat.android.modules.visitor.models.SalesIQVisitorProfile
import com.zoho.salesiqembed.ZohoSalesIQ

private val SALUTATIONS = listOf(
    SalesIQVisitorProfile.Salutation.Mr,
    SalesIQVisitorProfile.Salutation.Ms,
    SalesIQVisitorProfile.Salutation.Mrs,
    SalesIQVisitorProfile.Salutation.Dr,
    SalesIQVisitorProfile.Salutation.Prof,
    SalesIQVisitorProfile.Salutation.None,
)

private data class InfoEntry(var key: String, var value: String)

/** Screen 04 — full profile via Visitor.updateProfile; no deprecated setName/setEmail/etc. */
@Composable
fun VisitorScreen(nav: NavController) {
    val context = LocalContext.current
    val c = LocalAppColors.current
    var salutationIndex by remember { mutableStateOf(0) }
    var firstName by remember { mutableStateOf("Jordan") }
    var lastName by remember { mutableStateOf("Rivera") }
    var email by remember { mutableStateOf("jordan@acme.com") }
    var phoneCode by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var customAction by remember { mutableStateOf("promo_banner") }
    val customInfo = remember { mutableStateListOf(InfoEntry("plan", "Enterprise")) }
    var result by remember { mutableStateOf<String?>(null) }

    val salutation = SALUTATIONS[salutationIndex]

    ScreenScaffold(
        title = "Visitor",
        subtitle = "Profile, custom info, and registration",
        onBack = { nav.popBackStack() },
    ) {
        Section(title = "Profile") {
            Card {
                row {
                    Row {
                        Field("First name", firstName, { firstName = it }, modifier = Modifier.weight(1f))
                        Field("Last name", lastName, { lastName = it }, modifier = Modifier.weight(1f))
                    }
                }
                row {
                    ListRow("Salutation", value = salutation.name, chevron = true, onClick = {
                        salutationIndex = (salutationIndex + 1) % SALUTATIONS.size
                    })
                }
                row { Field("Email", email, { email = it }, autoCapitalize = false, keyboardType = androidx.compose.ui.text.input.KeyboardType.Email) }
                row { Field("Country code", phoneCode, { phoneCode = it }, placeholder = "+1", keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone) }
                row { Field("Phone", phone, { phone = it }, placeholder = "(555) 000-0000", keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone) }
                row { Field("User ID", userId, { userId = it }, placeholder = "Unique visitor identifier", autoCapitalize = false) }
            }
        }

        Section(title = "Custom info") {
            Card {
                customInfo.forEachIndexed { index, entry ->
                    row {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Field(if (index == 0) "Key" else "Key ${index + 1}", entry.key, { customInfo[index] = entry.copy(key = it) }, modifier = Modifier.weight(1f), placeholder = "e.g. plan", autoCapitalize = false)
                            Field(if (index == 0) "Value" else "Value ${index + 1}", entry.value, { customInfo[index] = entry.copy(value = it) }, modifier = Modifier.weight(1f), placeholder = "e.g. Enterprise")
                            Icon(
                                AppIcon.Close.vector,
                                contentDescription = "Remove field",
                                tint = c.danger,
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 6.dp)
                                    .size(20.dp)
                                    .clickable { customInfo.removeAt(index) },
                            )
                        }
                    }
                }
                row {
                    ListRow("Add key / value", icon = AppIcon.Plus, tint = IconTint.Primary, titleTone = TitleTone.Brand, onClick = {
                        customInfo.add(InfoEntry("", ""))
                    })
                }
            }
        }

        AppButton("Update profile", onClick = {
            val info = customInfo.filter { it.key.isNotBlank() }.associate { it.key.trim() to it.value }
            val profile = SalesIQVisitorProfile(
                salutation = salutation,
                firstName = firstName.ifBlank { null },
                lastName = lastName.ifBlank { null },
                email = email.ifBlank { null },
                // Country dialing code (e.g. "+1") and number are sent as separate fields.
                phoneNumber = if (phone.isNotBlank() || phoneCode.isNotBlank()) SalesIQVisitorProfile.PhoneNumber(phoneCode, phone) else null,
                customInfo = info.ifEmpty { null },
            )
            // Push the visitor's profile details (name, email, custom info) to SalesIQ.
            ZohoSalesIQ.Visitor.updateProfile(profile)
            result = jsonOf(
                "salutation" to salutation.name,
                "firstName" to profile.firstName,
                "lastName" to profile.lastName,
                "email" to profile.email,
                "customInfo" to info.toString(),
            )
            Toaster.show("Profile updated", ToastTone.Success)
        })

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppButton("Register visitor", modifier = Modifier.weight(1f), variant = ButtonVariant.Secondary, onClick = {
                if (userId.isBlank()) {
                    Toaster.show("Add a user ID first", ToastTone.Danger)
                } else {
                    // Identify this visitor to SalesIQ by a unique user ID (a known, returning user).
                    ZohoSalesIQ.registerVisitor(userId.trim(), object : RegisterListener {
                    override fun onSuccess() {
                        result = jsonOf("registered" to userId.trim())
                        Toaster.show("Visitor registered", ToastTone.Success)
                    }

                    override fun onFailure(code: Int, message: String?) {
                        result = jsonOf("errorCode" to code, "message" to (message ?: "Unknown"))
                        Toaster.show("Registration failed", ToastTone.Danger)
                    }
                    })
                }
            })
            AppButton("Unregister", modifier = Modifier.weight(1f), variant = ButtonVariant.Secondary, onClick = {
                // Clear the registered visitor, returning them to an anonymous guest.
                ZohoSalesIQ.unregisterVisitor(context, object : UnRegisterListener {
                    override fun onSuccess() {
                        result = jsonOf("unregistered" to true)
                        Toaster.show("Visitor unregistered", ToastTone.Success)
                    }

                    override fun onFailure(code: Int, message: String?) {
                        result = jsonOf("errorCode" to code, "message" to (message ?: "Unknown"))
                        Toaster.show("Unregister failed", ToastTone.Danger)
                    }
                })
            })
        }

        Section(title = "Custom action", footer = "Trigger a tracking action you configured in the SalesIQ portal.") {
            Card {
                row { Field("Action name", customAction, { customAction = it }, placeholder = "e.g. promo_banner", autoCapitalize = false) }
                row {
                    ListRow("Perform custom action", icon = AppIcon.Visitor, tint = IconTint.Primary, titleTone = TitleTone.Brand, onClick = {
                        if (customAction.isBlank()) {
                            Toaster.show("Enter an action name first", ToastTone.Danger)
                            return@ListRow
                        }
                        // Fire a named tracking action configured in the SalesIQ portal.
                        ZohoSalesIQ.Visitor.performCustomAction(customAction.trim())
                        result = jsonOf("performCustomAction" to customAction.trim())
                        Toaster.show("Action performed", ToastTone.Success)
                    })
                }
            }
        }

        Section(title = "Location", footer = "Attach a geo location to the visitor (distinct from the profile fields).") {
            AppButton("Set visitor location", variant = ButtonVariant.Secondary, onClick = {
                val location = SIQVisitorLocation("IN", 12.9716, 77.5946, "India", "Bengaluru", "Karnataka", "560001")
                // Attach a geographic location to the visitor for operator context.
                ZohoSalesIQ.Visitor.setLocation(location)
                result = jsonOf("setLocation" to "Bengaluru, India")
                Toaster.show("Visitor location set", ToastTone.Success)
            })
        }

        result?.let { ResultBlock(text = it, label = "Last result") }
    }
}
