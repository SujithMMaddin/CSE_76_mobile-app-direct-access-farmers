package com.example.directaccessfarmers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CropAdapterCustomer extends RecyclerView.Adapter<CropAdapterCustomer.CropViewHolder> {

    private final Context context;
    private final List<Crop> cropList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public CropAdapterCustomer(Context context, List<Crop> cropList) {
        this.context = context;
        this.cropList = cropList;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_crop_customer, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        Crop crop = cropList.get(position);

        holder.tvCropName.setText(nonNull(crop.getName(), "Unnamed"));
        holder.tvPrice.setText("₹" + crop.getPricePerKg() + " /kg");
        holder.tvLocation.setText("📍 " + nonNull(crop.getLocation(), "Unknown"));
        holder.tvQuantity.setText("Qty: " + crop.getQuantityKg() + " kg");

        if (crop.getFarmerName() != null && !crop.getFarmerName().isEmpty())
            holder.tvFarmerName.setText("👨‍🌾 " + crop.getFarmerName());
        else
            holder.tvFarmerName.setText("👨‍🌾 Unknown Farmer");

        if (crop.getFarmerPhone() != null && !crop.getFarmerPhone().isEmpty())
            holder.tvFarmerPhone.setText("📞 " + crop.getFarmerPhone());
        else
            holder.tvFarmerPhone.setText("📞 N/A");

        // Image loading - support both remote URLs and local file paths
        String imagePath = crop.getImageUrl();
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http") || imagePath.startsWith("https")) {
                    Glide.with(context).load(imagePath).into(holder.imageCrop);
                } else {
                    // treat as local file path
                    File f = new File(imagePath);
                    if (f.exists()) {
                        Glide.with(context).load(f).into(holder.imageCrop);
                    } else {
                        holder.imageCrop.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            } else {
                holder.imageCrop.setImageResource(R.drawable.ic_launcher_background);
            }
        } catch (Exception ex) {
            holder.imageCrop.setImageResource(R.drawable.ic_launcher_background);
        }

        // Click to open CropDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            if (crop.getId() != null && !crop.getId().isEmpty()) {
                Intent intent = new Intent(context, CropDetailsActivity.class);
                intent.putExtra("cropId", crop.getId());
                // If context is not an Activity, add FLAG_ACTIVITY_NEW_TASK
                if (!(context instanceof android.app.Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Invalid crop data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    public void cleanup() {
        // kept for compatibility (no timers here)
    }

    private String nonNull(String s, String fallback) {
        return (s == null || s.isEmpty()) ? fallback : s;
    }

    public static class CropViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCrop;
        TextView tvCropName, tvPrice, tvQuantity, tvLocation, tvFarmerName, tvFarmerPhone;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCrop = itemView.findViewById(R.id.imageCrop);
            tvCropName = itemView.findViewById(R.id.tvCropName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvFarmerName = itemView.findViewById(R.id.tvFarmerName);
            tvFarmerPhone = itemView.findViewById(R.id.tvFarmerPhone);
        }
    }
}
