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
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.utils.Constants;
import com.example.farmmobileapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerOrderAdapter extends RecyclerView.Adapter<FarmerOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private ApiService apiService;
    private SessionManager sessionManager;

    public FarmerOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
        this.sessionManager = SessionManager.getInstance(context);
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

        // Format order ID - handle both String and Long IDs
        String orderIdDisplay = "Order #";
        if (order.getId() != null) {
            orderIdDisplay += order.getId().toString().substring(0, Math.min(5, order.getId().toString().length()));
        } else {
            orderIdDisplay += "N/A";
        }
        holder.tvOrderId.setText(orderIdDisplay);

        // Format date
        if (order.getCreatedAt() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.tvOrderDate.setText(dateFormat.format(order.getCreatedAt()));
        } else {
            holder.tvOrderDate.setText("Date not available");
        }

        // Set product details
        holder.tvProductName.setText(order.getProductName() != null ? order.getProductName() : "Unknown Product");
        holder.tvQuantity.setText(order.getQuantity() + " units");
        holder.tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalPrice()));

        // Set client information
        holder.tvClientName.setText("Client: " +
                (order.getClientName() != null ? order.getClientName() : "Unknown Client"));
        holder.tvDeliveryLocation.setText("Delivery to: " +
                (order.getDeliveryLocation() != null ? order.getDeliveryLocation() : "Not specified"));

        // Set status
        holder.tvStatus.setText("Status: " +
                (order.getStatus() != null ? order.getStatus() : "Unknown"));

        // Set button visibility based on status
        if (Constants.ORDER_STATUS_PENDING.equals(order.getStatus())) {
            holder.btnAcceptOrder.setVisibility(View.VISIBLE);
            holder.btnAcceptOrder.setText("Mark as Delivered");
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
        Order orderUpdate = new Order();
        orderUpdate.setStatus(Constants.ORDER_STATUS_DELIVERED);

        apiService.updateOrderStatus(sessionManager.getAuthHeaderValue(), order.getId(), orderUpdate)
                .enqueue(new Callback<Order>() {
                    @Override
                    public void onResponse(Call<Order> call, Response<Order> response) {
                        if (response.isSuccessful()) {
                            orderList.get(position).setStatus(Constants.ORDER_STATUS_DELIVERED);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Order marked as delivered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to update order: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Order> call, Throwable t) {
                        Toast.makeText(context, "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
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
