package com.taruc.visory.donation;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taruc.visory.utils.LoggedUser;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseHelper {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReferenceDonateHistory;
    private List<DonateDatabase> donateDatabaseList = new ArrayList<>();

    public interface DataStatus {
        void DataIsLoaded(List<DonateDatabase> donateDatabaseList, List<String> keys);
    }

    public FirebaseDatabaseHelper() {
        mDatabase = FirebaseDatabase.getInstance();
        mReferenceDonateHistory = mDatabase.getReference("DonateDatabase");
    }

    public void readDonateHistory(final DataStatus dataStatus) {
        mReferenceDonateHistory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                donateDatabaseList.clear();
                List<String> keys = new ArrayList<>();
                for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                    keys.add(keyNode.getKey());
                    DonateDatabase donateDatabase = keyNode.getValue(DonateDatabase.class);

                    if(donateDatabase.getEmail() == user.getUserEmail()) {
                        donateDatabaseList.add(donateDatabase);
                    }
                }
                dataStatus.DataIsLoaded(donateDatabaseList, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}