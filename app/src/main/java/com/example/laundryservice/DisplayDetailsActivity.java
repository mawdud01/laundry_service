package com.example.laundryservice;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class DisplayDetailsActivity extends AppCompatActivity {

    private Spinner customerSpinner;
    private TextView customerDetailsTextView, orderDetailsText,duepayment;
    private Button  deleteRecordButton,deleteCustomerButton;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_details);

        customerSpinner = findViewById(R.id.customerSpinner);
        customerDetailsTextView = findViewById(R.id.customerDetailsTextView);
        orderDetailsText = findViewById(R.id.orderDetailsText);
        deleteRecordButton = findViewById(R.id.deleteButton);
        deleteCustomerButton = findViewById(R.id.deleteCustomerButton);

        duepayment= findViewById(R.id.duepayment);




        database = openOrCreateDatabase("LaundryAppDB", MODE_PRIVATE, null);

        // Populate the spinner with customer names
        List<String> customerNames = fetchCustomerNames();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, customerNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customerSpinner.setAdapter(spinnerAdapter);

        customerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                showCustomerAndOrderDetails(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        deleteRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecord(); // Call the deleteRecord function
            }
        });


        deleteCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCustomer();
            }
        });
    }

    private List<String> fetchCustomerNames() {
        List<String> customerNames = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT name FROM Customers", null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                customerNames.add(cursor.getString(cursor.getColumnIndex("name")));
            }
            cursor.close();
        }

        return customerNames;
    }

    private void showCustomerAndOrderDetails(int position) {
        // Retrieve the selected customer name
        String selectedCustomerName = customerSpinner.getSelectedItem().toString();

        // Retrieve customer details
        Cursor customerCursor = database.rawQuery("SELECT * FROM Customers WHERE name = ?", new String[]{selectedCustomerName});
        if (customerCursor.moveToFirst()) {
            String customerAddress = customerCursor.getString(customerCursor.getColumnIndex("address"));
            String customerEmail = customerCursor.getString(customerCursor.getColumnIndex("email"));
            String creationDate = customerCursor.getString(customerCursor.getColumnIndex("creation_date"));
            String phone = customerCursor.getString(customerCursor.getColumnIndex("phone"));

            String customerDetails = "Name: " + selectedCustomerName + "\nAddress: " + customerAddress
                    + "\nEmail: " + customerEmail + "\nCustomer Add Date: " + creationDate + "\nPhone: " + phone;
            customerDetailsTextView.setText(customerDetails);

            // Retrieve order details for the selected customer
            Cursor orderCursor = database.rawQuery("SELECT * FROM Orders WHERE customer_name = ?", new String[]{selectedCustomerName});
            if (orderCursor.moveToFirst()) {
                String orderItems = orderCursor.getString(orderCursor.getColumnIndex("items"));
                double orderTotalPrice = orderCursor.getDouble(orderCursor.getColumnIndex("total_price"));
                double duePayment = orderCursor.getDouble(orderCursor.getColumnIndex("status")); // Retrieve due payment

                String orderDetails = "Items: " + orderItems + "\nTotal Price: " + orderTotalPrice +" BDT";
                orderDetailsText.setText(orderDetails);

                // Display the due payment
                String duePaymentText = "Due Payment: " + duePayment+ " BDT";
                duepayment.setText(duePaymentText);
            } else {
                orderDetailsText.setText("No orders found for this customer.");
            }

            orderCursor.close();
        }
        customerCursor.close();
    }



    private void deleteRecord() {
        String selectedCustomerName = customerSpinner.getSelectedItem().toString();

        // Delete  order records
        int orderDeleted = database.delete("Orders", "customer_name = ?", new String[]{selectedCustomerName});

        if (orderDeleted > 0) {
            Toast.makeText(DisplayDetailsActivity.this, "Order record deleted successfully.", Toast.LENGTH_SHORT).show();
            orderDetailsText.setText("");
        } else {
            Toast.makeText(DisplayDetailsActivity.this, "Error deleting order record.", Toast.LENGTH_SHORT).show();
        }
    }





    private void deleteCustomer() {
        String selectedCustomerName = customerSpinner.getSelectedItem().toString();

        if (selectedCustomerName.isEmpty()) {
            Toast.makeText(DisplayDetailsActivity.this, "Please select a customer to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = openOrCreateDatabase("LaundryAppDB", MODE_PRIVATE, null);

        // Delete associated order records
        int orderDeleted = db.delete("Orders", "customer_name = ?", new String[]{selectedCustomerName});

        // Delete the customer record
        int customerDeleted = db.delete("Customers", "name = ?", new String[]{selectedCustomerName});

        if (customerDeleted > 0) {
            // Toast message for successful deletion
            Toast.makeText(DisplayDetailsActivity.this, "Customer deleted successfully.", Toast.LENGTH_SHORT).show();

            // Clear or update the detailsTextView to indicate no selected customer
            customerDetailsTextView.setText("");

            // Update the customer list in the customerSpinner
            List<String> updatedCustomers = fetchCustomerNames();
            ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, updatedCustomers);
            customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            customerSpinner.setAdapter(customerAdapter);
        } else {
            Toast.makeText(DisplayDetailsActivity.this, "Error deleting customer. Customer name may not exist.", Toast.LENGTH_SHORT).show();
        }
    }


}
