package com.taruc.visory.donation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.taruc.visory.R;
import com.taruc.visory.utils.LoggedUser;

import org.json.JSONException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class paypalmain extends AppCompatActivity {

    public static final int PAYPAL_REQUEST_CODE = 7171;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    Button btnDonate;
    EditText txtDonate;
    String amount = "";
    RadioGroup rg1, rg2;

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_payment);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        btnDonate = findViewById(R.id.btnDonate);
        txtDonate = findViewById(R.id.txtDonate);

        //Radio Buttons
        rg1 = findViewById(R.id.radGroup1to3);
        rg2 = findViewById(R.id.radGroup4to6);
        rg1.clearCheck();
        rg2.clearCheck();
        rg1.setOnCheckedChangeListener(listener1);
        rg2.setOnCheckedChangeListener(listener2);

        btnDonate.setOnClickListener(view -> {
            if (txtDonate.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Please fill in the amount you want to donate.", Toast.LENGTH_LONG).show();
            } else {
                processPayment();
            }
        });
    }

    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg2.setOnCheckedChangeListener(null); // remove the listener before clearing so we don't throw that stackoverflow exception(like Vladimir Volodin pointed out)
                rg2.clearCheck(); // clear the second RadioGroup!
                rg2.setOnCheckedChangeListener(listener2); //reset the listener
            }

            switch (checkedId) {
                case R.id.radRM1:
                    txtDonate.setText("1");
                    break;
                case R.id.radRM5:
                    txtDonate.setText("5");
                    break;
                case R.id.radRM10:
                    txtDonate.setText("10");
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg1.setOnCheckedChangeListener(null);
                rg1.clearCheck();
                rg1.setOnCheckedChangeListener(listener1);
            }

            switch (checkedId) {
                case R.id.radRM20:
                    txtDonate.setText("20");
                    break;
                case R.id.radRM50:
                    txtDonate.setText("50");
                    break;
                case R.id.radRM100:
                    txtDonate.setText("100");
                    break;
            }
        }
    };

    private void processPayment() {
        amount = txtDonate.getText().toString();
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(String.valueOf(amount)), "MYR", "Donate for Charity", PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        addDonationDetails(amount);
                        startActivity(new Intent(this, PaymentDetails.class)
                                .putExtra("PaymentDetails", paymentDetails)
                                .putExtra("PaymentAmount", amount)
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                        finish();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
        }
    }

    DatabaseReference databaseUserDonationDetails;
    private void addDonationDetails(String payAmount) {
        databaseUserDonationDetails = FirebaseDatabase.getInstance().getReference("DonateDatabase");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String timeDate = dateFormat.format(date);

        LoggedUser user = new LoggedUser(this);
        String email = user.getUserEmail();

        String id = databaseUserDonationDetails.push().getKey();

        int paymentAmount = Integer.parseInt(payAmount);
        DonateDatabase donateDetails = new DonateDatabase(email, paymentAmount, timeDate);

        databaseUserDonationDetails.child(id).setValue(donateDetails);
    }
}