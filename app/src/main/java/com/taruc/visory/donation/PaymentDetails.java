package com.taruc.visory.donation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.taruc.visory.R;
import com.taruc.visory.utils.LoggedUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PaymentDetails extends AppCompatActivity {
    TextView txtId, txtAmount, txtStatus;

    DatabaseReference databaseUserDonationDetails;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_donate_paymentdetails);

        databaseUserDonationDetails = FirebaseDatabase.getInstance().getReference("DonateDatabase");

        txtId = (TextView) findViewById(R.id.txtId);
        txtAmount = (TextView) findViewById(R.id.txtAmount);
        txtStatus = (TextView) findViewById(R.id.txtStatus);

        Intent intent = getIntent();

        try {
            JSONObject jsonObject = new JSONObject(intent.getStringExtra("PaymentDetails"));
            showDetails(jsonObject.getJSONObject("response"), intent.getStringExtra("PaymentAmount"));
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }
    }

    private void addDonationDetails(int payAmount) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String timeDate = dateFormat.format(date);

        LoggedUser user = new LoggedUser(this);
        String email = user.getUserEmail();

        String id = databaseUserDonationDetails.push().getKey();

        DonateDatabase donateDetails = new DonateDatabase(email, payAmount, timeDate);

        databaseUserDonationDetails.child(id).setValue(donateDetails);
    }

    private void showDetails(JSONObject response, String paymentAmount) {
        try {
            txtId.setText("Payment ID: " + response.getString("id"));
            txtStatus.setText("Status: " + response.getString("state"));
            txtAmount.setText("Amount: RM " + paymentAmount);
            int payAmount = Integer.parseInt(paymentAmount);
            addDonationDetails(payAmount);
            Toast.makeText(getApplicationContext(), "Thanks for your kindness. ", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Donate process is cancelled", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}