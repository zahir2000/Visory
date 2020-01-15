package com.taruc.visory.donation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.taruc.visory.R;

import org.json.JSONException;

import java.math.BigDecimal;

public class paypalmain extends AppCompatActivity {

    public static final int PAYPAL_REQUEST_CODE = 7171;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    Button btnDonate;
    EditText txtDonate;
    //RadioButton radRM1,radRM2,radRM5,radRM10;
    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupAmountDonateRadioButton);

    String amount = "";

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

        btnDonate = (Button) findViewById(R.id.btnDonate);
        txtDonate = (EditText) findViewById(R.id.txtDonate);
//        radRM1 = (RadioButton) findViewById(R.id.radRM1);
//        radRM2 = (RadioButton) findViewById(R.id.radRM2);
//        radRM5 = (RadioButton) findViewById(R.id.radRM5);
//        radRM10 = (RadioButton) findViewById(R.id.radRM10);

//        int one  = 1;
//        if(radRM1.isChecked()){
//            txtDonate.append(String.valueOf(one));
//        }else if(radRM2.isChecked()){
//            txtDonate.setText();
//        }else if(radRM5.isChecked()){
//            txtDonate.setText(String.valueOf(5));
//        }else if(radRM10.isChecked()){
//            txtDonate.setText(String.valueOf(10));
//        }


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                switch (checkedId) {
                    case R.id.radRM1:
                        txtDonate.setText("1");
                        break;
                    case R.id.radRM2:
                        txtDonate.setText("2");
                        break;
                    case R.id.radRM5:
                        txtDonate.setText("5");
                        break;
                    case R.id.radRM10:
                        txtDonate.setText("10");
                        break;
                }
            }
        });

        btnDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtDonate.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please fill in the amount you want to donate.", Toast.LENGTH_LONG).show();
                } else {
                    processPayment();
                }
            }
        });
    }

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
}