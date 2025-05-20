package com.example.farmmobileapp.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmmobileapp.R;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private FirebaseFirestore db;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Format order ID
        holder.tvOrderId.setText("Order #" + order.getId().substring(0, 5));

        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvOrderDate.setText(dateFormat.format(order.getCreatedAt()));

        // Set product details
        holder.tvProductName.setText(order.getProductName());
        holder.tvQuantity.setText(order.getQuantity() + " units");
        holder.tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalPrice()));

        // Set delivery location
        holder.tvDeliveryLocation.setText("Delivery Location: " + order.getDeliveryLocation());

        // Set status
        holder.tvStatus.setText("Status: " + order.getStatus());

        // Set cancel button visibility based on status
        if (order.getStatus().equals(Constants.ORDER_STATUS_PENDING) && order.isRecentOrder()) {
            holder.btnCancelOrder.setVisibility(View.VISIBLE);
            holder.btnCancelOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelOrder(order, holder.getAdapterPosition());
                }
            });
        } else {
            holder.btnCancelOrder.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void cancelOrder(Order order, final int position) {
        // Show confirmation dialog
        // For simplicity, we'll directly cancel the order here
        db.collection(Constants.COLLECTION_ORDERS)
                .document(order.getId())
                .update("status", Constants.ORDER_STATUS_CANCELLED)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            orderList.get(position).setStatus(Constants.ORDER_STATUS_CANCELLED);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to cancel order: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void updateData(List<Order> newOrders) {
        this.orderList = newOrders;
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId;
        TextView tvOrderDate;
        TextView tvProductName;
        TextView tvQuantity;
        TextView tvTotal;
        TextView tvDeliveryLocation;
        TextView tvStatus;
        Button btnCancelOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.textViewOrderId);
            tvOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            tvProductName = itemView.findViewById(R.id.textViewProductName);
            tvQuantity = itemView.findViewById(R.id.textViewQuantity);
            tvTotal = itemView.findViewById(R.id.textViewTotal);
            tvDeliveryLocation = itemView.findViewById(R.id.textViewDeliveryLocation);
            tvStatus = itemView.findViewById(R.id.textViewStatus);
            btnCancelOrder = itemView.findViewById(R.id.buttonCancelOrder);
        }
    }
}