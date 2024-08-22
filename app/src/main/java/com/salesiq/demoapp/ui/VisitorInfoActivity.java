package com.salesiq.demoapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.salesiq.demoapp.R;
import com.zoho.livechat.android.SIQDepartment;
import com.zoho.livechat.android.SIQVisitorLocation;
import com.zoho.livechat.android.listeners.DepartmentListener;
import com.zoho.salesiqembed.ZohoSalesIQ;

import java.util.ArrayList;

public class VisitorInfoActivity extends AppCompatActivity {

    final private static String TAG = "mobilisten:visitor-info";

    private TextInputEditText visitorNameInput, visitorEmailInput, visitorPhoneInput;
    private EditText visitorQuestionInput, infoKeyInput, infoValueInput;
    private Button startChatButton, setVisitorLocationButton, setVisitorDetailsButton;
    public static ArrayList<String> departmentsArray = new ArrayList<>();
    String name, email, phone, question, infoKey, infoValue;
    static String department;
    AutoCompleteTextView departmentSpinner;
    ArrayAdapter<String> arrayAdapter;

    private void initializeViews() {
        visitorNameInput = findViewById(R.id.visitor_name_input);
        visitorEmailInput = findViewById(R.id.visitor_email_input);
        visitorPhoneInput = findViewById(R.id.visitor_phone_input);
        visitorQuestionInput = findViewById(R.id.visitor_question_input);
        infoKeyInput = findViewById(R.id.set_info_key_input);
        infoValueInput = findViewById(R.id.set_info_value_input);
        setVisitorDetailsButton = findViewById(R.id.set_visitor_details_button);
        setVisitorLocationButton = findViewById(R.id.set_visitor_location_button);
        startChatButton = findViewById(R.id.start_chat_button);

        /*
         * This API allows you to get a list of all the departments that have been associated with the brand in which Mobilisten is configured.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-chat-getdepartment-v4-2-0.html
         */
        ZohoSalesIQ.Chat.getDepartments(new DepartmentListener() {
            @Override
            public void onSuccess(ArrayList<SIQDepartment> departments) {
                departmentsArray.clear();
                for (SIQDepartment department : departments) {
                    departmentsArray.add(department.name);
                    Log.d(TAG, department.name + " " + department.available);
                }
                arrayAdapter.notifyDataSetChanged();
                departmentSpinner.setEnabled(true);
            }

            @Override
            public void onFailure(int code, String message) {
                Log.d(TAG, "Error while getting departments: $code, message: $message");
            }
        });

        departmentSpinner = findViewById(R.id.department_auto_complete_text_view);
        arrayAdapter = new ArrayAdapter<>(VisitorInfoActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, departmentsArray);
        departmentSpinner.setAdapter(arrayAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_info);
        initializeViews();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        /*
         * This API allows you to set a secondary location for a visitor.
         * The secondary location set using this API will be visible within the visitor information page under the Secondary Location section.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-visitor-location-v4-2-0.html
         */
        setVisitorLocationButton.setOnClickListener(v -> {
            ZohoSalesIQ.Visitor.setLocation(new SIQVisitorLocation(
                    "IN",
                    12.8311,
                    80.0494,
                    "India",
                    "Chennai",
                    "Tamil Nadu",
                    "603202"
            ));
        });

        departmentSpinner.setOnItemClickListener((parent, view, newPosition, id) -> {
            department = departmentsArray.get(newPosition);
            /*
             * This API lets you set a default department to which you would like to route all the chat requests placed by the visitors.
             */
            ZohoSalesIQ.Chat.setDepartment(department);
        });

        setVisitorDetailsButton.setOnClickListener(v -> {
            name = visitorNameInput.getText().toString().trim();
            email = visitorEmailInput.getText().toString().trim();
            phone = visitorPhoneInput.getText().toString().trim();

            ZohoSalesIQ.Visitor.setName(name); // This API lets you set the name of the visitor
            ZohoSalesIQ.Visitor.setEmail(email); // This API lets you set the email of the visitor
            ZohoSalesIQ.Visitor.setContactNumber(phone); // This API lets you set the contact number of the visitor

            infoKey = (infoKeyInput.getText().toString().trim().length() > 0) ? infoKeyInput.getText().toString().trim() : null;
            infoValue = (infoValueInput.getText().toString().trim().length() > 0) ? infoValueInput.getText().toString().trim() : null;

            /*
             * This API would let you add additional information about the visitors and display it to the operators of your firm
             * in the pane right beside their chat windows while conversing with the visitors.
             * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-visitor-info-v4-2-0.html
             */
            ZohoSalesIQ.Visitor.addInfo(infoKey, infoValue);
        });

        startChatButton.setOnClickListener(v -> {
            question = visitorQuestionInput.getText().toString().trim();
            if (department != null && !question.isEmpty())
            {
                ZohoSalesIQ.Visitor.startChat(question); // This API can be used to automatically initiate a chat with the given message
            }
            else
            {
                Toast.makeText(VisitorInfoActivity.this, "Please don't leave the mandatory fields empty", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        visitorNameInput.clearFocus();
        visitorEmailInput.clearFocus();
        visitorPhoneInput.clearFocus();
        visitorQuestionInput.clearFocus();
        departmentSpinner.clearFocus();
        infoKeyInput.clearFocus();
        infoValueInput.clearFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (department != null) {
            departmentSpinner.setText(department, false);
        }

        /*
         * This API lets you set an apt title for each and every screen in your application,
         * thus making it easy for you to track down the trail of your visitors when they navigate through the screens of your mobile app.
         * It will be visible within the Activity section in the chat window.
         * Refer https://www.zoho.com/salesiq/help/developer-guides/android-sdk-tracking-title-v4-2-0.html
         */
        ZohoSalesIQ.Tracking.setPageTitle("Visitor Info Activity");
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