package com.taruc.visory.donation;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.taruc.visory.R;

import java.util.List;

public class donateHistory extends AppCompatActivity {
    private RecyclerView mRecycleView;
    TextView lblEmpty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_history);
        mRecycleView = (RecyclerView) findViewById(R.id.recycleviewDonateHistory);
        new FirebaseDatabaseHelper().readDonateHistory(new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataIsLoaded(List<DonateDatabase> donateDatabaseList, List<String> keys) {
                findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
                new RecyclerView_Config().setConfig(mRecycleView, donateHistory.this,donateDatabaseList,keys);
                lblEmpty = (TextView) findViewById(R.id.lblEmpty);
                if(donateDatabaseList.isEmpty()){
                    lblEmpty.setText("Oops! Nobody donate yet.");
                }else{
                    lblEmpty.setText("");
                }
            }
        });
    }
}