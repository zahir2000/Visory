package com.taruc.visory.donation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    double amt = 0.0;

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

    private void addDonationDetails(double payAmount) {
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
            double payAmount = Double.parseDouble(paymentAmount);
            addDonationDetails(payAmount);
            Toast.makeText(getApplicationContext(), "Thanks for your kindness. ", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Donate process is cancelled", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseDatabase.getInstance().getReference("DonateDatabase")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            double dbAmount = Double.parseDouble(String.valueOf(snapshot.child("amount").getValue()));
                            addMe(dbAmount);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void addMe(double amttt) {
        //Toast.makeText(this, String.valueOf(amttt), Toast.LENGTH_LONG).show();
        amt = amt + amttt;
    }
}