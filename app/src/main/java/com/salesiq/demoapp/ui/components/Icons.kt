package com.salesiq.demoapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One icon vocabulary across all sample apps (DESIGN_SYSTEM rule 4),
 * mapped onto Material outlined icons. Mirrors the RN `AppIcons` map so the
 * apps read identically.
 */
enum class AppIcon(val vector: ImageVector) {
    Chat(Icons.Outlined.ChatBubbleOutline),
    Calls(Icons.Outlined.Call),
    Launcher(Icons.Outlined.GpsFixed),
    Visitor(Icons.Outlined.AccountCircle),
    KnowledgeBase(Icons.Outlined.Book),
    Homepage(Icons.Outlined.GridView),
    Help(Icons.AutoMirrored.Outlined.HelpOutline),
    Core(Icons.Outlined.Bolt),
    Notifications(Icons.Outlined.NotificationsNone),
    Events(Icons.Outlined.ShowChart),
    Settings(Icons.Outlined.Settings),
    Logs(Icons.Outlined.Description),
    Globe(Icons.Outlined.Language),
    Key(Icons.Outlined.Key),
    Search(Icons.Outlined.Search),
    Check(Icons.Filled.Check),
    Close(Icons.Filled.Close),
    ChevronRight(Icons.Filled.ChevronRight),
    Back(Icons.Filled.ChevronLeft),
    Plus(Icons.Filled.Add),
    Article(Icons.AutoMirrored.Outlined.Article),
    Department(Icons.Outlined.Group),
    Timer(Icons.Outlined.Timer),
    Refresh(Icons.Outlined.Refresh),
    Send(Icons.AutoMirrored.Outlined.Send),
    Eye(Icons.Outlined.RemoveRedEye),
    History(Icons.Outlined.History),
    Info(Icons.Outlined.Info),
    Alert(Icons.Outlined.ErrorOutline),
    CheckCircle(Icons.Outlined.CheckCircle),
    Link(Icons.Outlined.Link),
    Sun(Icons.Outlined.LightMode),
    Shield(Icons.Outlined.Security),
    Nav(Icons.Outlined.Shuffle),
    Copy(Icons.Outlined.CopyAll),
}

/** Tint variants for the 34×34 list-row icon box. */
enum class IconTint { Primary, Secondary, Accent, Danger }
