package com.salesiq.demoapp.listeners;

import android.net.Uri;
import android.util.Log;

import com.zoho.livechat.android.SIQVisitor;
import com.zoho.livechat.android.VisitorChat;
import com.zoho.livechat.android.listeners.SalesIQChatListener;
import com.zoho.livechat.android.listeners.SalesIQFAQListener;
import com.zoho.livechat.android.listeners.SalesIQListener;
import com.zoho.salesiqembed.ZohoSalesIQ;

public class SalesIQListeners {

    final private static String TAG = "mobilisten:listeners";

    public static void initialise() {

        /*
         * SalesIQListener would invoke callback methods for various actions performed by the visitors.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-event-handler-salesiq-listener-v4-2-0.html
         */
        ZohoSalesIQ.setListener(new SalesIQListener() {
            @Override
            public void handleSupportOpen() {
                Log.d(TAG, "SUPPORT OPEN EVENT CALLED");
            }

            @Override
            public void handleSupportClose() {
                Log.d(TAG, "SUPPORT CLOSE EVENT CALLED");
            }

            @Override
            public void handleOperatorsOnline() {
                Log.d(TAG, "OPERATORS ONLINE EVENT CALLED");
            }

            @Override
            public void handleOperatorsOffline() {
                Log.d(TAG, "OPERATORS OFFLINE EVENT CALLED");
            }

            @Override
            public void handleIPBlock() {
                Log.d(TAG, "BLOCK IP EVENT CALLED");
            }

            @Override
            public void handleTrigger(String triggerName, SIQVisitor visitor) {
                Log.d(TAG, "HANDLE TRIGGER EVENT CALLED, " + "Trigger Name: " + triggerName + ", Visitor Object - Visitor Name: " + visitor.getName());
            }
        });

        /*
         * SalesIQChatListener would invoke callback methods for various chat actions performed by the visitors.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-event-handler-salesiq-chat-listener-v4-2-0.html
         */
        ZohoSalesIQ.Chat.setListener(new SalesIQChatListener() {
            @Override
            public void handleChatViewOpen(String chatId) {
                Log.d(TAG, "CHAT VIEW OPEN EVENT CALLED, Chat ID: " + chatId);
            }

            @Override
            public void handleChatViewClose(String chatId) {
                Log.d(TAG, "CHAT VIEW CLOSED EVENT CALLED, Chat ID: " + chatId);
            }

            @Override
            public void handleChatOpened(VisitorChat visitorChat) {
                Log.d(TAG, "CHAT OPENED EVENT CALLED, VisitorChat Object: " + visitorChat.toString());
            }

            @Override
            public void handleChatClosed(VisitorChat visitorChat) {
                Log.d(TAG, "CHAT CLOSED EVENT CALLED, VisitorChat Object: " + visitorChat.toString());
            }

            @Override
            public void handleChatAttended(VisitorChat visitorChat) {
                Log.d(TAG, "CHAT ATTENDED EVENT CALLED, VisitorChat Object: " + visitorChat.toString());
            }

            @Override
            public void handleChatMissed(VisitorChat visitorChat) {
                Log.d(TAG, "CHAT MISSED EVENT CALLED, VisitorChat Object: " + visitorChat.toString());
            }

            @Override
            public void handleChatReOpened(VisitorChat visitorChat) {
                Log.d(TAG, "CHAT REOPENED EVENT CALLED, VisitorChat Object: " + visitorChat.toString());
            }

            @Override
            public void handleRating(VisitorChat visitorChat) {
                Log.d(TAG, "RATING SUBMITTED EVENT CALLED, VisitorChat Object: " + visitorChat.toString());
            }

            @Override
            public void handleFeedback(VisitorChat visitorChat) {
                Log.d(TAG, "FEEDBACK SUBMITTED EVENT CALLED, VisitorChat Object: " + visitorChat.toString());
            }

            @Override
            public void handleQueuePositionChange(VisitorChat visitorChat) {
                Log.d(TAG, "QUEUE POSITION CHANGE EVENT CALLED, VisitorChat Object: " + visitorChat.toString());
            }

            @Override
            public boolean handleUri(Uri uri, VisitorChat visitorChat) {
                Log.d(TAG, "URI INVOKED EVENT CALLED, URI: " + uri + ", VisitorChat Object: " + visitorChat.toString());
                return SalesIQChatListener.super.handleUri(uri, visitorChat);
            }
        });

        /*
         * SalesIQActionListener can be used to acknowledge the success or failure of callback methods to the SDK
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-chatactions-intro-v4-2-0.html
         */
        ZohoSalesIQ.ChatActions.setListener((customAction, listener) -> {
            Log.d(TAG, "CUSTOM ACTION INVOKED EVENT CALLED, SalesIQCustomAction : " + customAction.getClientActionName());

            if (customAction.getClientActionName().equals("book_ticket")) {
                //implement your own code here
                if (isPaymentComplete()) {
                    listener.onSuccess("Payment success!");
                } else {
                    listener.onFailure("Payment failed");
                }
            }
        });

        /*
         * Notification.setListener will be used to invoke the method onBadgeChange() when the count of the unread messages changes.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-tracking-notification-listener-v4-2-0.html
         */
        ZohoSalesIQ.Notification.setListener(count -> {
            //update badge count here
            Log.d(TAG, "NOTIFICATION BADGE COUNT CHANGE EVENT CALLED, Unread Messages Count: " + count);
        });

        /*
         * SalesIQFAQListener would invoke callback methods for various FAQ actions performed by the visitors.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-event-handler-salesiq-faq-listener-v4-2-0.html
         */
        ZohoSalesIQ.FAQ.setListener(new SalesIQFAQListener() {
            @Override
            public void handleArticleOpened(String article_id) {
                Log.d(TAG, "ARTICLE OPENED EVENT CALLED, Opened Article ID: " + article_id);
            }

            @Override
            public void handleArticleClosed(String article_id) {
                Log.d(TAG, "ARTICLE CLOSED EVENT CALLED, Closed Article ID: " + article_id);
            }

            @Override
            public void handleArticleLiked(String article_id) {
                Log.d(TAG, "ARTICLE LIKED EVENT CALLED, Liked Article ID: " + article_id);
            }

            @Override
            public void handleArticleDisliked(String article_id) {
                Log.d(TAG, "ARTICLE DISLIKED EVENT CALLED, Disliked Article ID: " + article_id);
            }
        });
    }

    public static boolean isPaymentComplete() {
        return true;
//        return false;
    }
}
