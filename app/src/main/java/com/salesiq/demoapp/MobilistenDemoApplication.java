package com.salesiq.demoapp;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.salesiq.demoapp.listeners.SalesIQListeners;
import com.zoho.commons.ChatComponent;
import com.zoho.commons.Fonts;
import com.zoho.commons.InitConfig;
import com.zoho.livechat.android.R;
import com.zoho.livechat.android.listeners.InitListener;
import com.zoho.salesiqembed.ZohoSalesIQ;

public class MobilistenDemoApplication extends Application {
    final private static String TAG = "mobilisten:application";

    private static final MutableLiveData<Result> mobilistenInitializationStateMutableLiveData = new MutableLiveData<>();
    public static LiveData<Result> mobilistenInitializationStateLiveData = mobilistenInitializationStateMutableLiveData;

    /*
     * Follow the steps mentioned in https://www.zoho.com/salesiq/help/developer-guides/android-mobile-sdk-installation-2.0.html to
     * get the app key and access key for your application from the SalesIQ portal.
     */
    String zohoSalesIQAppKey = <your_app_key>,zohoSalesIQAccessKey = <your_access_key>;
    @Override
    public void onCreate() {

        super.onCreate();

        /*
         * You can implement custom fonts throughout the SDK using this implementation.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-mobile-sdk-siq-theme-customization-fonts-2-0.html
         */
        InitConfig initConfig = new InitConfig();
        initConfig.setFont(Fonts.REGULAR, "fonts/Lato-Regular.ttf");
        initConfig.setFont(Fonts.MEDIUM, "fonts/Lato-Bold.ttf");

        /*
         * The SalesIQ SDK provides various event callbacks that developers can use to perform customized actions.
         */
        SalesIQListeners.initialise();

        /*
         * This API is used to initialize the SalesIQ SDK with the completion callback.
         */
        ZohoSalesIQ.init(this, zohoSalesIQAppKey, zohoSalesIQAccessKey, initConfig, new InitListener() {
            @Override
            public void onInitSuccess() {
                Log.d(TAG, "INITIALISATION SUCCESS");

                ZohoSalesIQ.showLauncher(true); //This API can be used to control the visibility of the launcher

                /*
                 * SalesIQ SDK follows the system theme by default. You can opt-out of this option by setting the value to false.
                 * Refer res/values/styles.xml and https://www.zoho.com/salesiq/help/developer-guides/android-mobile-sdk-theme-customization-2.0.html
                 */
                ZohoSalesIQ.syncThemeWithOS(true);

                /*
                 *  You can also apply your custom theme to the Mobilisten by using the setTheme() API.
                 *  To apply your custom theme, make sure to set the syncThemeWithOS() to "false".
                 *  Refer res/values/styles.xml and https://www.zoho.com/salesiq/help/developer-guides/android-sdk-set-theme-v6-0-0.html
                 *
                 *  ZohoSalesIQ.syncThemeWithOS(false);
                 *  ZohoSalesIQ.setTheme(com.salesiq.demoapp.R.style.CustomTheme);
                 */

                /*
                 * The Chat.setVisibility API lets you toggle the function of various components.
                 * The sample code below is used to enable/disable the pre-chat form before starting a chat
                 * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-chat-visibility-v4-2-0.html
                 */
                ZohoSalesIQ.Chat.setVisibility(ChatComponent.prechatForm, true);

                ZohoSalesIQ.Chat.showOperatorImageInLauncher(true); //This API can be used to set the operator image in the launcher

                /*
                 * This API will set offline banner in chat window when the brand is offline.
                 * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-chat-show-offline-message-v4-2-0.html
                 */
                ZohoSalesIQ.Chat.showOfflineMessage(true);

                /*
                 * Chat Actions allows you to define custom actions that can be invoked when a user clicks on them inside the chat.
                 * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-chatactions-intro-v4-2-0.html
                 */
                ZohoSalesIQ.ChatActions.register("book_ticket"); //This API allows you to register any custom action that you have created.
                ZohoSalesIQ.ChatActions.setTimeout(5000); //This API allows you to set the timeout value until which loader needs to be shown. Value in milliseconds

                ZohoSalesIQ.Notification.setIcon(R.drawable.ic_salesiq_icon); // This API allows you to set an icon for the SalesIQ SDK notifications.

                /*
                 * You can use this API to enable/disable the conversation history option in the SalesIQ SDK.
                 * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-conversation-visibility-v4-2-0.html
                 */
                ZohoSalesIQ.Conversation.setVisibility(true);
                ZohoSalesIQ.Conversation.setTitle("Mobilisten Live Support"); // This API is used to set title in the header of the conversation history section

                /*
                  You can use this API to show/hide the Knowledgebase option in the SalesIQ SDK.
                  Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-KnowledgeBase-set-visibility-v6-0-0.html
                 */
                ZohoSalesIQ.KnowledgeBase.setVisibility(ZohoSalesIQ.ResourceType.Articles, true);

                mobilistenInitializationStateMutableLiveData.postValue(new Result(Result.MobilistenInitStatus.SUCCESS, "SUCCESS"));
            }

            @Override
            public void onInitError(int errorCode, String errorMessage) {
                Log.d(TAG, "Error while initialising Mobilisten, Code: " + errorCode + " , Message: " + errorMessage);
                mobilistenInitializationStateMutableLiveData.postValue(new Result(Result.MobilistenInitStatus.FAILURE, errorMessage));
            }
        });
    }
}
