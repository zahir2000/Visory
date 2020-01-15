package com.taruc.visory.donation;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.taruc.visory.R;

import java.util.List;

class RecyclerView_Config {
    private Context mContext;
    private donateHistoryAdapter mdonateHistoryAdapter;

    public void setConfig(RecyclerView recyclerView, Context context, List<DonateDatabase> donateDatabaseList, List<String> keys) {
        mContext = context;
        mdonateHistoryAdapter = new donateHistoryAdapter(donateDatabaseList, keys);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mdonateHistoryAdapter);
    }

    class donateHistoryView extends RecyclerView.ViewHolder {
        private TextView mAmount, mAmountImage,mWho;
        private TextView mTimeDate;

        private String key;

        public donateHistoryView(ViewGroup parent) {
            super(LayoutInflater.from(mContext).inflate(R.layout.donate_history_list, parent, false));

            mAmountImage = (TextView) itemView.findViewById(R.id.lblDonateAmountImage);
            mAmount = (TextView) itemView.findViewById(R.id.lblRMDonated);
            mTimeDate = (TextView) itemView.findViewById(R.id.lblTimeDateDonate);
            mWho = (TextView) itemView.findViewById(R.id.lblYouHaveDonated);
        }

        public void bind(DonateDatabase donateDatabase, String key) {
            mAmountImage.setText("RM " + Integer.toString(donateDatabase.getAmount()));
            if (donateDatabase.getAmount() < 5) {
                mAmountImage.setBackgroundColor(Color.parseColor("#88A9DF"));
                //mAmountImage.setBackgroundResource(R.drawable.dollar_signs_background_rm1);
            } else if (donateDatabase.getAmount() >= 5 && donateDatabase.getAmount() < 10) {
                mAmountImage.setBackgroundColor((Color.parseColor("#73AE76")));
                //mAmountImage.setBackgroundResource(R.drawable.dollar_signs_background_rm5);
            }else if (donateDatabase.getAmount() >= 10 && donateDatabase.getAmount() < 20){
                mAmountImage.setBackgroundColor((Color.parseColor("#D07A79")));
                //mAmountImage.setBackgroundResource(R.drawable.dollar_signs_background_rm10);
            }else if (donateDatabase.getAmount() >= 20 && donateDatabase.getAmount() < 50){
                mAmountImage.setBackgroundColor((Color.parseColor("#D58712")));
                //mAmountImage.setBackgroundResource(R.drawable.dollar_signs_background_rm20);
            }else if(donateDatabase.getAmount() >= 50 && donateDatabase.getAmount() < 100){
                mAmountImage.setBackgroundColor((Color.parseColor("#98D2DE")));
                //mAmountImage.setBackgroundResource(R.drawable.dollar_signs_background_rm50);
            }else{
                mAmountImage.setBackgroundColor((Color.parseColor("#7D7095")));
                //mAmountImage.setBackgroundResource(R.drawable.dollar_signs_background_rm100);
            }
            mAmount.setText("RM " + Integer.toString(donateDatabase.getAmount()));
            mTimeDate.setText(donateDatabase.getDateTime());
            mWho.setText(donateDatabase.getEmail() + " have donated");
            this.key = key;
        }
    }

    class donateHistoryAdapter extends RecyclerView.Adapter<donateHistoryView> {
        private List<DonateDatabase> donateDatabaseList;
        private List<String> mKeys;

        public donateHistoryAdapter(List<DonateDatabase> donateDatabaseList, List<String> mKeys) {
            this.donateDatabaseList = donateDatabaseList;
            this.mKeys = mKeys;
        }

        @Override
        public donateHistoryView onCreateViewHolder(ViewGroup parent, int viewType) {
            return new donateHistoryView(parent);
        }

        @Override
        public void onBindViewHolder(donateHistoryView holder, int position) {
            holder.bind(donateDatabaseList.get(position), mKeys.get(position));
        }

        @Override
        public int getItemCount() {
            return donateDatabaseList.size();
        }
    }
}