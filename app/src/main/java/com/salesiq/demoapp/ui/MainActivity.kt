package com.salesiq.demoapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.salesiq.demoapp.MobilistenDemoApplication
import com.salesiq.demoapp.Result

import com.salesiq.demoapp.databinding.ActivityMainBinding
import com.zoho.commons.LauncherModes
import com.zoho.commons.LauncherProperties
import com.zoho.livechat.android.ZohoLiveChat
import com.zoho.livechat.android.config.DeviceConfig
import com.zoho.livechat.android.listeners.RegisterListener
import com.zoho.salesiqembed.ZohoSalesIQ
import java.util.*

class MainActivity : AppCompatActivity() {
    private var activityMainBinding: ActivityMainBinding? = null
    private val binding get() = activityMainBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobilistenDemoApplication.mobilistenInitializationStateLiveData.observe(this) { mobilistenState: Result ->
            with(binding) {
                if (mobilistenState.status == Result.MobilistenInitStatus.SUCCESS) {
                    openSalesiqButton.isEnabled = true
                    visitorDetailsPageButton.isEnabled = true
                    failureText.visibility = View.GONE
                } else {
                    failureText.visibility = View.VISIBLE
                    failureText.text =
                        mobilistenState.data.uppercase(Locale.getDefault())
                }
            }
        }

        with(binding) {

            /*
             *  This API is used to open the SalesIQ SDK from the custom launcher view
             */
            salesiqImageView.setOnClickListener { ZohoLiveChat.Chat.show() }
            openSalesiqButton.setOnClickListener { ZohoLiveChat.Chat.show() }

            loginButton.setOnClickListener {
                val visitorID = visitorIdInput.text.toString().trim { it <= ' ' }

                /*
                 * This API allows you to register a visitor using a unique ID with the SalesIQ SDK.
                 * If your application has login and logout life cycles, you can enroll your visitor and
                 * their activities in the SDK will be synchronized across multiple platforms.
                 * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-regsiter-visitor-v4-2-0.html
                 */
                if (MobilistenDemoApplication.mobilistenInitializationStateLiveData.value?.status == Result.MobilistenInitStatus.SUCCESS) {
                    ZohoSalesIQ.registerVisitor(visitorID, object : RegisterListener {
                        override fun onSuccess() {
                            Toast.makeText(
                                this@MainActivity,
                                "Registering Visitor as ' $visitorID '...",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        override fun onFailure(code: Int, message: String) {
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                            Log.d(
                                TAG,
                                "Error while registering visitor, Code: $code , Message: $message"
                            )
                        }
                    })
                } else {
                    Log.d(
                        TAG,
                        "Mobilisten has not been initialized yet"
                    )
                }
            }

            logoutButton.setOnClickListener {
                /*
                 * This API allows you to unregister a user once they are registered using the .registerVisitor() API.
                 * If your application has login and logout life cycles, you can unregister a visitor during a session logout
                 * which would clear any data that the SDK may hold such as past conversations had by the registered user.
                 * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-unregsiter-visitor-v4-2-0.html
                 */
                ZohoSalesIQ.unregisterVisitor(applicationContext)
            }

            /*
             * You can use this API to customize the Launcher Button including the mode, positions, and icon as you wish.
             * Refer https://www.zoho.com/salesiq/help/developer-guides/android-launcher-button-customization-v4-2-0.html
             */
            launcherPositionStaticButton.setOnClickListener {
                val launcherProperties =
                    LauncherProperties(LauncherModes.STATIC) // The button sticks at the screen's bottom right corner.
                launcherProperties.icon = AppCompatResources.getDrawable(
                    this@MainActivity,
                    com.zoho.livechat.android.R.drawable.salesiq_target
                ) // You can set an icon for the SalesIQ launcher.
                ZohoSalesIQ.setLauncherProperties(launcherProperties) // Use this API to set the LauncherProperties
            }

            launcherPositionFloatingButton.setOnClickListener {
                val launcherProperties =
                    LauncherProperties(LauncherModes.FLOATING) // The button is movable within the application screen.
                launcherProperties.setDirection(LauncherProperties.Horizontal.LEFT) // This sets the launcher icon to the left side of the screen
                launcherProperties.setY(DeviceConfig.getDeviceHeight() / 2) // You can also set the 'y' position by mentioning the pixel values
                ZohoSalesIQ.setLauncherProperties(launcherProperties) // Use this API to set the LauncherProperties
            }

            /*
             * You can use this API to show/hide the launcher in the SalesIQ SDK.
             */
            showLauncherButton.setOnClickListener { ZohoSalesIQ.showLauncher(true) }
            hideLauncherButton.setOnClickListener { ZohoSalesIQ.showLauncher(false) }

            /*
             * This API lets you configure the language preference for the embedded chat widget.
             * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-chat-language-v4-2-0.html
             */
            englishLanguageTextview.setOnClickListener {
                ZohoLiveChat.Chat.setLanguage("en")
                Toast.makeText(this@MainActivity, "English language is set", Toast.LENGTH_SHORT)
                    .show()
            }
            frenchLanguageTextview.setOnClickListener {
                ZohoLiveChat.Chat.setLanguage("fr")
                Toast.makeText(this@MainActivity, "French language is set", Toast.LENGTH_SHORT)
                    .show()
            }
            japaneseLanguageTextview.setOnClickListener {
                ZohoLiveChat.Chat.setLanguage("ja")
                Toast.makeText(this@MainActivity, "Japanese language is set", Toast.LENGTH_SHORT)
                    .show()
            }

            /*
             * This API would let you track specified custom actions performed by the visitors in your mobile application.
             * The actions will be visible within the Activity section in the chat window.
             *
             * You can also use this API to open the chat window and trigger a new chat based on the performed custom action
             * by setting the shouldOpenChatWindow parameter to true. By default, the shouldOpenChatWindow parameter is set to false.
             * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-tracking-custom-actions-v4-2-0.html
             */
            trackVisitorActivityButton.setOnClickListener {
                ZohoSalesIQ.Tracking.setCustomAction("Added To Cart")
//              ZohoSalesIQ.Tracking.setCustomAction("Added To Cart", true)
            }

            visitorDetailsPageButton.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, VisitorInfoActivity::class.java)
                )
            }
            getSalesiqDataButton.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, SalesIQDataActivity::class.java)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        /*
         * This API lets you set an apt title for each and every screen in your application,
         * thus making it easy for you to track down the trail of your visitors when they navigate through the screens of your mobile app.
         * It will be visible within the Activity section in the chat window.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-tracking-title-v4-2-0.html
         */
        ZohoSalesIQ.Tracking.setPageTitle("Home Activity")
    }

    override fun onPause() {
        super.onPause()
        binding.visitorIdInput.clearFocus()
    }

    companion object {
        private const val TAG = "mobilisten:main"
    }
}