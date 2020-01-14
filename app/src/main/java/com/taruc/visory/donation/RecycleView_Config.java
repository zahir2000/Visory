package com.taruc.visory.donation;

import android.content.Context;
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
        private TextView mAmount, mAmountImage;
        private TextView mTimeDate;

        private String key;

        public donateHistoryView(ViewGroup parent) {
            super(LayoutInflater.from(mContext).inflate(R.layout.donate_history_list, parent, false));

            mAmountImage = (TextView) itemView.findViewById(R.id.lblDonateAmountImage);
            mAmount = (TextView) itemView.findViewById(R.id.lblRMDonated);
            mTimeDate = (TextView) itemView.findViewById(R.id.lblTimeDateDonate);
        }

        public void bind(DonateDatabase donateDatabase, String key) {
            mAmountImage.setText("RM " + Integer.toString(donateDatabase.getAmount()));
            if (donateDatabase.getAmount() < 5) {
                mAmountImage.setBackgroundColor(3101573);
            } else if (donateDatabase.getAmount() >= 5 && donateDatabase.getAmount() < 10) {
                mAmountImage.setBackgroundColor(7581302);
            }else if (donateDatabase.getAmount() >= 10 && donateDatabase.getAmount() < 20){
                mAmountImage.setBackgroundColor(13662841);
            }else if (donateDatabase.getAmount() >= 20 && donateDatabase.getAmount() < 50){
                mAmountImage.setBackgroundColor(13993746);
            }else if(donateDatabase.getAmount() >= 50 && donateDatabase.getAmount() < 100){
                mAmountImage.setBackgroundColor(10015454);
            }else{
                mAmountImage.setBackgroundColor(8220821);
            }
            mAmount.setText("RM " + Integer.toString(donateDatabase.getAmount()));
            mTimeDate.setText(donateDatabase.getDateTime());
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