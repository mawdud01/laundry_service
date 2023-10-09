package com.example.laundryservice;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddCustomerActivity extends AppCompatActivity {
    private EditText editTextName, editTextAddress, editTextEmail, editTextCreationDate, editTextPhone;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextCreationDate = findViewById(R.id.editTextCreationDate);
        editTextPhone = findViewById(R.id.editTextPhone);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String address = editTextAddress.getText().toString();
                String email = editTextEmail.getText().toString();
                String creationDate = editTextCreationDate.getText().toString();
                String phone = editTextPhone.getText().toString();

                // Check if the email is valid or not
                if (!email.contains("@")) {
                    Toast.makeText(AddCustomerActivity.this, "Invalid email address.", Toast.LENGTH_SHORT).show();
                    return;
                }


                // Check if the phone number has exactly 11 digits
                if (phone.length() != 11) {
                    Toast.makeText(AddCustomerActivity.this, "Phone number must be 11 digits long.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase db = openOrCreateDatabase("LaundryAppDB", MODE_PRIVATE, null);
                db.execSQL("CREATE TABLE IF NOT EXISTS Customers(name TEXT UNIQUE, address TEXT, email TEXT, creation_date TEXT, phone TEXT)");

                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("address", address);
                values.put("email", email);
                values.put("creation_date", creationDate);
                values.put("phone", phone);

                // Check if a customer with the same name
                Cursor cursor = db.rawQuery("SELECT * FROM Customers WHERE name=?", new String[]{name});
                if (cursor.getCount() > 0) {
                    Toast.makeText(AddCustomerActivity.this, "Customer with the same name already exists. Enter a different username.", Toast.LENGTH_SHORT).show();
                } else {
                    // No customer with the same name, insert the new customer
                    long result = db.insert("Customers", null, values);

                    if (result != -1) {
                        Toast.makeText(AddCustomerActivity.this, "Customer added successfully.", Toast.LENGTH_SHORT).show();
                        // Clear input fields
                        clearInputFields();
                    } else {
                        Toast.makeText(AddCustomerActivity.this, "Error adding customer.", Toast.LENGTH_SHORT).show();
                    }
                }
                cursor.close();
            }
        });

    }

    private void clearInputFields() {
        editTextName.setText("");
        editTextAddress.setText("");
        editTextEmail.setText("");
        editTextCreationDate.setText("");
        editTextPhone.setText("");
    }



    public void showDatePickerDialog(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        editTextCreationDate.setText(selectedDate);
                    }
                },
                // Set the initial date (optional)
                2023, 0, 1
        );

        // Show the DatePickerDialog
        datePickerDialog.show();
    }
}

