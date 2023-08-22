package com.salesiq.demoapp.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.salesiq.demoapp.MobilistenUtil;
import com.salesiq.demoapp.R;
import com.zoho.livechat.android.VisitorChat;
import com.zoho.livechat.android.listeners.ConversationListener;
import com.zoho.livechat.android.listeners.OperatorImageListener;
import com.zoho.livechat.android.modules.knowledgebase.ui.entities.Resource;
import com.zoho.livechat.android.modules.knowledgebase.ui.listeners.ResourcesListener;
import com.zoho.salesiqembed.ZohoSalesIQ;

import java.util.ArrayList;
import java.util.List;

public class SalesIQDataActivity extends AppCompatActivity {

    final private static String TAG = "mobilisten:salesiq-data";

    private TextInputEditText chatIdInput, attenderIdInput;
    private Button openChatViewButton, endChatSessionButton, getAllArticlesButton;
    private ImageButton fetchAttenderImageButton;
    private ImageView operatorImageView;

    private void initializeViews() {
        chatIdInput = findViewById(R.id.chat_id_input);
        openChatViewButton = findViewById(R.id.open_chat_using_id_button);
        endChatSessionButton = findViewById(R.id.close_chat_using_id_button);
        fetchAttenderImageButton = findViewById(R.id.fetch_operator_image_button);
        attenderIdInput = findViewById(R.id.attender_id_input);
        operatorImageView = findViewById(R.id.operator_imageview);
        getAllArticlesButton = findViewById(R.id.get_all_articles_button);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesiq_data);
        initializeViews();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String[] conversationTypeArray = getResources().getStringArray(R.array.conversation_type_array);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, conversationTypeArray);
        AutoCompleteTextView conversationTypeSpinner = findViewById(R.id.conversation_type_auto_complete_text_view);
        conversationTypeSpinner.setAdapter(arrayAdapter);

        /*
         * This API is used to open a specific chat when the chatID is provided.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-chat-open-v4-2-0.html
         */
        openChatViewButton.setOnClickListener(v -> {
            String chatId = chatIdInput.getText().toString().trim();
            if (!chatId.isEmpty()) {
                ZohoSalesIQ.Chat.open(chatId);
            } else {
                requestFocusAndShowKeyBoard(chatIdInput);
                Toast.makeText(SalesIQDataActivity.this, "Enter a valid chat ID in the input", Toast.LENGTH_SHORT).show();
            }
        });

        /*
         * This API can be used to end a support chat that is currently in an open state.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-chat-end-chat-v4-2-0.html
         */
        endChatSessionButton.setOnClickListener(v -> {
            String chatId = chatIdInput.getText().toString().trim();
            if (!chatId.isEmpty()) {
                ZohoSalesIQ.Chat.endChat(chatId);
            } else {
                requestFocusAndShowKeyBoard(chatIdInput);
                Toast.makeText(SalesIQDataActivity.this, "Enter a valid chat ID in the input", Toast.LENGTH_SHORT).show();
            }
        });

        /*
         * This API can be used to fetch the image of the attender of the chat.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-chat-fetch-attender-image-v4-2-0.html
         */
        fetchAttenderImageButton.setOnClickListener(v -> {
            String attenderId = attenderIdInput.getText().toString().trim();

            ZohoSalesIQ.Chat.fetchAttenderImage(attenderId, true, new OperatorImageListener() {
                @Override
                public void onSuccess(Drawable operatorImage) {
                    operatorImageView.setImageDrawable(operatorImage);
                }

                @Override
                public void onFailure(int code, String message) {
                    Log.d(TAG, "Error while fetching attender image, Code: " + code + " , Message: " + message);
                    Toast.makeText(SalesIQDataActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        conversationTypeSpinner.setOnItemClickListener((parent, view, newPosition, id) -> {

            /*
             * This API is used to get a list of all the conversations of a visitor.
             * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-chat-get-list-v4-2-0.html
             */
            ZohoSalesIQ.Chat.getList(MobilistenUtil.getConversationType(newPosition), new ConversationListener() {
                @Override
                public void onSuccess(ArrayList<VisitorChat> chats) {
                    if (chats.isEmpty()) {
                        Log.d(TAG, "The list is empty");
                    }

                    for (VisitorChat chat : chats) {
                        Log.d(TAG, chat.toString());
                    }

                    Toast.makeText(SalesIQDataActivity.this, "Check logs", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int code, String message) {
                    Log.d(TAG, "Error while getting chat objects, Code: " + code + " , Message: " + message);
                    Toast.makeText(SalesIQDataActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        /*
         * This API can be used to fetch a list of articles from the SalesIQ knowledge base.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-KnowledgeBase-get-resources-v6-0-0.html
         */

        //To get a specific resource list, use the below parameters as filters.
        String department_id = null; //The list of resource departments can be fetched using ZohoSalesIQ.KnowledgeBase.getResourceDepartments() API.
        String category_id = null; //The list of resource categories can be fetched using ZohoSalesIQ.KnowledgeBase.getCategories() API.
        String search_key = null; //Providing a search key will return a list of all resources with titles that contain the search key.
        int page = 1; //Specify the number of pages. (Default value is 1)
        int limit = 99; //Specify the number of articles to be fetched for a page. (Default & maximum value is 99)

        getAllArticlesButton.setOnClickListener(v ->  ZohoSalesIQ.KnowledgeBase.getResources(ZohoSalesIQ.ResourceType.Articles, department_id, category_id, search_key, page, limit, new ResourcesListener() {
            @Override
            public void onSuccess(@NonNull List<Resource> resources, boolean moreDataAvailable) {
                if (resources.isEmpty()) {
                    Log.d(TAG, "The resources list is empty");
                }

                for (Resource resource : resources) {
                    Log.d(TAG, resource.toString());
                }
            }

            @Override
            public void onFailure(int code, @Nullable String message) {
                Log.d(TAG, "Error while getting article objects, Code: " + code + " , Message: " + message);
                Toast.makeText(SalesIQDataActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }));

        attenderIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchAttenderImageButton.setVisibility((s.length() > 0) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        conversationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ((TextView) parent.getChildAt(0)).setTextColor(android.R.attr.textColorSecondary);
            }
        });
    }

    private void requestFocusAndShowKeyBoard(View view) {
        view.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        attenderIdInput.clearFocus();
        chatIdInput.clearFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * This API lets you set an apt title for each and every screen in your application,
         * thus making it easy for you to track down the trail of your visitors when they navigate through the screens of your mobile app.
         * It will be visible within the Activity section in the chat window.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-tracking-title-v4-2-0.html
         */
        ZohoSalesIQ.Tracking.setPageTitle("SalesIQ Data Activity");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}