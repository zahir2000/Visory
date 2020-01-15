package com.taruc.visory.donation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.taruc.visory.R;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentDetails extends AppCompatActivity {
    TextView txtId, txtAmount, txtStatus;



    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_donate_paymentdetails);

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

    private void showDetails(JSONObject response, String paymentAmount) {
        try {
            txtId.setText("Payment ID: " + response.getString("id"));
            txtStatus.setText("Status: " + response.getString("state"));
            txtAmount.setText("Amount: RM " + paymentAmount);
//            int payAmount = Integer.parseInt(paymentAmount);
//            addDonationDetails(payAmount);
            Toast.makeText(getApplicationContext(), "Thanks for your kindness. ", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Donate process is cancelled", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}