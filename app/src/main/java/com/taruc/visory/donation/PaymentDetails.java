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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaymentDetails extends AppCompatActivity {
    TextView txtId, txtAmount, txtStatus;

    DatabaseReference databaseUserDonationDetails;

    DatabaseReference databaseTotalDonation;
    double amt;
    FirebaseDatabase mDatabase;
    List<DonateDatabase> donateDatabases = new ArrayList<>();

    public interface DataStatus{
        void DataIsLoaded(List<DonateDatabase> donateDatabases, List<String> keys);
    }
    public PaymentDetails(){
        mDatabase = FirebaseDatabase.getInstance();
        databaseUserDonationDetails = mDatabase.getReference("DonateDatabase");
    }

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_donate_paymentdetails);

        databaseUserDonationDetails = FirebaseDatabase.getInstance().getReference("DonateDatabase");
        databaseTotalDonation = FirebaseDatabase.getInstance().getReference("TotalDonation");

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
        //TotalDonation totalDonation = new TotalDonation(payAmount);

        databaseUserDonationDetails.child(id).setValue(donateDetails);
        //databaseTotalDonation.child("TotalDonation").setValue(payAmount);
    }

    private void showDetails(JSONObject response, String paymentAmount) {
        try {
            txtId.setText("Payment ID: " + response.getString("id"));
            txtStatus.setText("Status: " + response.getString("state"));
            txtAmount.setText("Amount: RM " + paymentAmount);
            double payAmount = Double.parseDouble(paymentAmount);
            addDonationDetails(payAmount);
            Toast.makeText(getApplicationContext(), "Thanks for your kindness.", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Donate process is cancelled", Toast.LENGTH_LONG).show();
            finish();
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        List<DonateDatabase> donateDetails = null;
//        databaseUserDonationDetails.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
////                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
////                    DonateDatabase db = snapshot.getValue(DonateDatabase.class);
////                    amt = db.getAmount();
////                }
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    //getting artist
//                    DonateDatabase totalDonate = postSnapshot.getValue(DonateDatabase.class);
//                    //adding artist to the list
//                    donateDetails.add(totalDonate);
//                    amt += donateDetails.get(0).getAmount();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }
//
//    public void readDatabase(final DataStatus dataStatus){
//        databaseUserDonationDetails.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                donateDatabases.clear();
//                List<String> keys = new ArrayList<>();
//                for (DataSnapshot keyNode : dataSnapshot.getChildren()){
//                    keys.add(keyNode.getKey());
//                    DonateDatabase donateDatabase = keyNode.getValue(DonateDatabase.class);
//                    donateDatabases.add(donateDatabase);
//                }
//                dataStatus.DataIsLoaded(donateDatabases,keys);
//                amt = donateDatabases.get(3).getAmount();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

}