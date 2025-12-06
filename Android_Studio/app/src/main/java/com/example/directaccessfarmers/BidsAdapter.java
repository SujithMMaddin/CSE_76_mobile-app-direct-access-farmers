package com.example.directaccessfarmers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BidsAdapter extends RecyclerView.Adapter<BidsAdapter.BidViewHolder> {

    private final List<Bid> bidList;

    public BidsAdapter(List<Bid> bidList) {
        this.bidList = bidList;
    }

    @NonNull
    @Override
    public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bid, parent, false);
        return new BidViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BidViewHolder holder, int position) {
        Bid bid = bidList.get(position);
        holder.tvBidderName.setText("Customer: " + bid.getCustomerName());
        holder.tvBidAmount.setText("Bid: ₹" + String.format("%.2f", bid.getBidValue()));
    }

    @Override
    public int getItemCount() {
        return (bidList != null) ? bidList.size() : 0;
    }

    static class BidViewHolder extends RecyclerView.ViewHolder {
        TextView tvBidderName, tvBidAmount;

        public BidViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBidderName = itemView.findViewById(R.id.tvCustomer);
            tvBidAmount = itemView.findViewById(R.id.tvBidValue);
        }
    }
}
