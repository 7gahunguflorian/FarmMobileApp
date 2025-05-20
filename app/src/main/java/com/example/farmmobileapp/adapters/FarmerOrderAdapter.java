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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FarmerOrderAdapter extends RecyclerView.Adapter<FarmerOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private FirebaseFirestore db;

    public FarmerOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_farmer_order, parent, false);
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

        // Set client information
        holder.tvClientName.setText("Client: " + order.getClientName());
        holder.tvDeliveryLocation.setText("Delivery to: " + order.getDeliveryLocation());

        // Set status
        holder.tvStatus.setText("Status: " + order.getStatus());

        // Set button visibility based on status
        if (order.getStatus().equals(Constants.ORDER_STATUS_PENDING)) {
            holder.btnAcceptOrder.setVisibility(View.VISIBLE);
            holder.btnAcceptOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    markOrderAsDelivered(order, holder.getAdapterPosition());
                }
            });
        } else {
            holder.btnAcceptOrder.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void markOrderAsDelivered(Order order, final int position) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", Constants.ORDER_STATUS_DELIVERED);

        db.collection(Constants.COLLECTION_ORDERS)
                .document(order.getId())
                .update(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            orderList.get(position).setStatus(Constants.ORDER_STATUS_DELIVERED);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Order marked as delivered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to update order: " + task.getException().getMessage(),
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
        TextView tvClientName;
        TextView tvDeliveryLocation;
        TextView tvStatus;
        Button btnAcceptOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.textViewOrderId);
            tvOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            tvProductName = itemView.findViewById(R.id.textViewProductName);
            tvQuantity = itemView.findViewById(R.id.textViewQuantity);
            tvTotal = itemView.findViewById(R.id.textViewTotal);
            tvClientName = itemView.findViewById(R.id.textViewClientName);
            tvDeliveryLocation = itemView.findViewById(R.id.textViewDeliveryLocation);
            tvStatus = itemView.findViewById(R.id.textViewStatus);
            btnAcceptOrder = itemView.findViewById(R.id.buttonAcceptOrder);
        }
    }
}
