package com.example.laundryservice;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {
    private Spinner customerSpinner;
    private CheckBox tShirtsCheckBox, jeansCheckBox, dressShirtsCheckBox, jacketsCheckBox, sweatersCheckBox,
            bedSheetsCheckBox, pillowcasesCheckBox, towelsCheckBox, blanketsCheckBox;
    private EditText totalPriceEditText, discount,paymentstatus;
    private Button placeOrderButton;

    // Database-related variables
    private SQLiteDatabase database;
    private static final String DATABASE_NAME = "LaundryAppDB"; // Database name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        // Initialize database
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);

        // Create the "Orders" table if it doesn't exist
        createOrdersTable();

        // Initialize UI elements
        customerSpinner = findViewById(R.id.customerSpinner);
        tShirtsCheckBox = findViewById(R.id.tShirtsCheckBox);
        jeansCheckBox = findViewById(R.id.jeansCheckBox);
        dressShirtsCheckBox = findViewById(R.id.dressShirtsCheckBox);
        jacketsCheckBox = findViewById(R.id.jacketsCheckBox);
        sweatersCheckBox = findViewById(R.id.sweatersCheckBox);
        bedSheetsCheckBox = findViewById(R.id.bedSheetsCheckBox);
        pillowcasesCheckBox = findViewById(R.id.pillowcasesCheckBox);
        towelsCheckBox = findViewById(R.id.towelsCheckBox);
        blanketsCheckBox = findViewById(R.id.blanketsCheckBox);
        totalPriceEditText = findViewById(R.id.totalPriceEditText);
        placeOrderButton = findViewById(R.id.placeOrderButton);
        Button totalamount = findViewById(R.id.totalamount);
        discount = findViewById(R.id.discount);
        paymentstatus=findViewById(R.id.paymentstatus);

        // Populate the customer spinner by fetching customer names from the "Customers" table
        List<String> customers = fetchCustomerNames();
        ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, customers);
        customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customerSpinner.setAdapter(customerAdapter);

        // Calculate and display the total amount when the "Total Amount" button is clicked
        totalamount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double total = 0.0;

                if (tShirtsCheckBox.isChecked()) {
                    total += calculateItemPrice("T_Shirts");
                }
                if (jeansCheckBox.isChecked()) {
                    total += calculateItemPrice("Jeans");
                }
                if (dressShirtsCheckBox.isChecked()) {
                    total += calculateItemPrice("Dress_Shirts");
                }
                if (jacketsCheckBox.isChecked()) {
                    total += calculateItemPrice("Jackets");
                }
                if (sweatersCheckBox.isChecked()) {
                    total += calculateItemPrice("Sweaters");
                }
                if (bedSheetsCheckBox.isChecked()) {
                    total += calculateItemPrice("Bed_Sheets");
                }
                if (pillowcasesCheckBox.isChecked()) {
                    total += calculateItemPrice("Pillowcases");
                }
                if (towelsCheckBox.isChecked()) {
                    total += calculateItemPrice("Towels");
                }
                if (blanketsCheckBox.isChecked()) {
                    total += calculateItemPrice("Blankets");
                }







                // Retrieve the discount from the discount EditText
                String discountText = discount.getText().toString();
                double discountAmount = 0.0; // Default to no discount if the input is empty

                if (!discountText.isEmpty()) {
                    discountAmount = Double.parseDouble(discountText);
                }

                // Calculate the total amount after discount
                double afterDiscount = total - (total * discountAmount / 100.0);

                // Display the total amount after discount in the totalPriceEditText
                totalPriceEditText.setText(String.valueOf(afterDiscount));
            }
        });

        // Place the order when the "Place Order" button is clicked
        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if no items are selected
                if (!tShirtsCheckBox.isChecked() && !jeansCheckBox.isChecked() && !dressShirtsCheckBox.isChecked() &&
                        !jacketsCheckBox.isChecked() && !sweatersCheckBox.isChecked() && !bedSheetsCheckBox.isChecked() &&
                        !pillowcasesCheckBox.isChecked() && !towelsCheckBox.isChecked() && !blanketsCheckBox.isChecked()) {
                    Toast.makeText(AddProductActivity.this, "Please select at least one item.", Toast.LENGTH_SHORT).show();
                    return;
                }


                // Check if the total amount is calculated
                String totalAmountText2 = totalPriceEditText.getText().toString().trim();
                if (totalAmountText2.isEmpty()) {
                    Toast.makeText(AddProductActivity.this, "Please calculate the total amount first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Retrieve the selected customer name
                String selectedCustomer = customerSpinner.getSelectedItem().toString();

                // Retrieve the advance payment from the paymentstatus EditText
                String advancePaymentText = paymentstatus.getText().toString();
                double advancePayment = 0.0;

                if (!advancePaymentText.isEmpty()) {
                    advancePayment = Double.parseDouble(advancePaymentText);
                }

                // Calculate the due amount as totalAmount - advancePayment
                String totalAmountText = totalPriceEditText.getText().toString();
                double totalAmount = Double.parseDouble(totalAmountText);
                double dueAmount = totalAmount - advancePayment;

                // Check if an order already exists for the selected customer
                Cursor orderCursor = database.rawQuery("SELECT * FROM Orders WHERE customer_name = ?", new String[]{selectedCustomer});
                if (orderCursor.moveToFirst()) {
                    // If an order exists, update it
                    ContentValues updateValues = new ContentValues();
                    updateValues.put("items", buildItemsString());
                    updateValues.put("total_price", totalAmount);
                    updateValues.put("status", dueAmount);

                    long updateResult = database.update("Orders", updateValues, "customer_name = ?", new String[]{selectedCustomer});

                    if (updateResult != -1) {
                        Toast.makeText(AddProductActivity.this, "Order updated successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Error updating order.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If no order exists, insert a new one
                    ContentValues insertValues = new ContentValues();
                    insertValues.put("customer_name", selectedCustomer);
                    insertValues.put("items", buildItemsString());
                    insertValues.put("total_price", totalAmount);
                    insertValues.put("status", dueAmount); // Insert the due amount

                    long insertResult = database.insert("Orders", null, insertValues);

                    if (insertResult != -1) {
                        Toast.makeText(AddProductActivity.this, "Order placed successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Error placing order.", Toast.LENGTH_SHORT).show();
                    }
                }

                // Close the cursor
                orderCursor.close();
            }
        });

    }

    private void createOrdersTable() {
        database.execSQL("CREATE TABLE IF NOT EXISTS Orders(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_name TEXT, " +
                "items TEXT, " +
                "total_price REAL, " +
                "status REAL" +
                ")");
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

    private double calculateItemPrice(String itemName) {
        // Replace this with your actual item price retrieval logic
        double itemPrice = 0.0;


        if ("T_Shirts".equals(itemName)) {
            itemPrice = 50.0;
        } else if ("Jeans".equals(itemName)) {
            itemPrice = 60.0;
        } else if ("Dress_Shirts".equals(itemName)) {
            itemPrice = 55.0;
        } else if ("Jackets".equals(itemName)) {
            itemPrice = 70.0;
        } else if ("Sweaters".equals(itemName)) {
            itemPrice = 45.0;
        } else if ("Bed_Sheets".equals(itemName)) {
            itemPrice = 30.0;
        } else if ("Pillowcases".equals(itemName)) {
            itemPrice = 30.0;
        } else if ("Towels".equals(itemName)) {
            itemPrice = 50.0;
        } else if ("Blankets".equals(itemName)) {
            itemPrice = 100.0;
        }

        return itemPrice;
    }

    // Build a comma-separated string of selected items
    private String buildItemsString() {
        StringBuilder itemsBuilder = new StringBuilder();
        if (tShirtsCheckBox.isChecked()) {
            itemsBuilder.append("T_Shirts, ");
        }
        if (jeansCheckBox.isChecked()) {
            itemsBuilder.append("Jeans, ");
        }
        if (dressShirtsCheckBox.isChecked()) {
            itemsBuilder.append("Dress_Shirts, ");
        }
        if (jacketsCheckBox.isChecked()) {
            itemsBuilder.append("Jackets, ");
        }
        if (sweatersCheckBox.isChecked()) {
            itemsBuilder.append("Sweaters, ");
        }
        if (bedSheetsCheckBox.isChecked()) {
            itemsBuilder.append("Bed_Sheets, ");
        }
        if (pillowcasesCheckBox.isChecked()) {
            itemsBuilder.append("Pillowcases, ");
        }
        if (towelsCheckBox.isChecked()) {
            itemsBuilder.append("Towels, ");
        }
        if (blanketsCheckBox.isChecked()) {
            itemsBuilder.append("Blankets, ");
        }
        // Remove the trailing ", " if items are selected
        if (itemsBuilder.length() > 0) {
            itemsBuilder.setLength(itemsBuilder.length() - 2);
        }
        return itemsBuilder.toString();
    }
}
