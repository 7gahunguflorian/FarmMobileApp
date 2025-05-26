package com.example.farmmobileapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.utils.Constants;
import com.example.farmmobileapp.utils.ErrorHandler;
import com.example.farmmobileapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private ApiService apiService;
    private SessionManager sessionManager;
    private boolean isFarmer;
    private OnOrderStatusChangeListener listener;

    public interface OnOrderStatusChangeListener {
        void onStatusChange(Order order, String newStatus);
    }

    public OrderAdapter(Context context, List<Order> orders, boolean isFarmer) {
        this.context = context;
        this.orders = orders;
        this.isFarmer = isFarmer;
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public void setOnOrderStatusChangeListener(OnOrderStatusChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewProductName;
        private TextView textViewQuantity;
        private TextView textViewTotalPrice;
        private TextView textViewDeliveryLocation;
        private TextView textViewStatus;
        private TextView textViewDate;
        private TextView textViewClientName;
        private TextView textViewFarmerName;
        private Button buttonAccept;
        private Button buttonReject;
        private Button buttonComplete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewTotalPrice = itemView.findViewById(R.id.textViewTotalPrice);
            textViewDeliveryLocation = itemView.findViewById(R.id.textViewDeliveryLocation);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewClientName = itemView.findViewById(R.id.textViewClientName);
            textViewFarmerName = itemView.findViewById(R.id.textViewFarmerName);
            buttonAccept = itemView.findViewById(R.id.buttonAccept);
            buttonReject = itemView.findViewById(R.id.buttonReject);
            buttonComplete = itemView.findViewById(R.id.buttonComplete);

            // Set visibility of buttons based on user type
            if (isFarmer) {
                buttonAccept.setVisibility(View.VISIBLE);
                buttonReject.setVisibility(View.VISIBLE);
                buttonComplete.setVisibility(View.VISIBLE);
            } else {
                buttonAccept.setVisibility(View.GONE);
                buttonReject.setVisibility(View.GONE);
                buttonComplete.setVisibility(View.GONE);
            }
        }

        public void bind(Order order) {
            // Set product details
            if (order.getProduct() != null) {
                textViewProductName.setText(order.getProduct().getName());
                if (order.getProduct().getImageUrl() != null && !order.getProduct().getImageUrl().isEmpty()) {
                    Glide.with(context)
                        .load(order.getProduct().getImageUrl())
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(imageViewProduct);
                } else {
                    imageViewProduct.setImageResource(R.drawable.placeholder_product);
                }
            }

            // Set order details
            textViewQuantity.setText(String.format("Quantity: %.1f", order.getQuantity()));
            textViewTotalPrice.setText(String.format("Total: $%.2f", order.getTotalPrice()));
            textViewDeliveryLocation.setText("Delivery: " + order.getDeliveryLocation());
            
            // Set status with color
            String status = order.getStatus();
            textViewStatus.setText("Status: " + status);
            switch (status) {
                case "PENDING":
                    textViewStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light));
                    break;
                case "ACCEPTED":
                    textViewStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
                    break;
                case "REJECTED":
                    textViewStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
                    break;
                case "COMPLETED":
                    textViewStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_light));
                    break;
            }

            // Set date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            if (order.getOrderDate() != null) {
                textViewDate.setText("Ordered: " + dateFormat.format(order.getOrderDate()));
            } else if (order.getCreatedAt() != null) {
                textViewDate.setText("Ordered: " + dateFormat.format(order.getCreatedAt()));
            }

            // Set user names
            if (order.getClient() != null) {
                textViewClientName.setText("Client: " + order.getClient().getUsername());
            }
            if (order.getFarmer() != null) {
                textViewFarmerName.setText("Farmer: " + order.getFarmer().getUsername());
            }

            // Set button states based on order status
            if (isFarmer) {
                switch (order.getStatus()) {
                    case "PENDING":
                        buttonAccept.setVisibility(View.VISIBLE);
                        buttonReject.setVisibility(View.VISIBLE);
                        buttonComplete.setVisibility(View.GONE);
                        break;
                    case "ACCEPTED":
                        buttonAccept.setVisibility(View.GONE);
                        buttonReject.setVisibility(View.GONE);
                        buttonComplete.setVisibility(View.VISIBLE);
                        break;
                    default:
                        buttonAccept.setVisibility(View.GONE);
                        buttonReject.setVisibility(View.GONE);
                        buttonComplete.setVisibility(View.GONE);
                        break;
                }

                // Set click listeners for buttons
                buttonAccept.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusChange(order, "ACCEPTED");
                    }
                });

                buttonReject.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusChange(order, "REJECTED");
                    }
                });

                buttonComplete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusChange(order, "COMPLETED");
                    }
                });
            }
        }
    }
}