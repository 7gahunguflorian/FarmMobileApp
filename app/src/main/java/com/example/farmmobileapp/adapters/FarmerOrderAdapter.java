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
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.utils.SessionManager;
import com.example.farmmobileapp.utils.ErrorHandler;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerOrderAdapter extends RecyclerView.Adapter<FarmerOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private ApiService apiService;
    private SessionManager sessionManager;
    private OnOrderStatusChangeListener statusChangeListener;

    public interface OnOrderStatusChangeListener {
        void onOrderStatusChanged(Order order, String newStatus);
    }

    public FarmerOrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public void setOnOrderStatusChangeListener(OnOrderStatusChangeListener listener) {
        this.statusChangeListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_farmer_order, parent, false);
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
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewOrderId;
        private TextView textViewClientName;
        private TextView textViewProductName;
        private TextView textViewQuantity;
        private TextView textViewTotalPrice;
        private TextView textViewStatus;
        private TextView textViewOrderDate;
        private Button buttonAccept;
        private Button buttonReject;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewClientName = itemView.findViewById(R.id.textViewClientName);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewTotalPrice = itemView.findViewById(R.id.textViewTotalPrice);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            buttonAccept = itemView.findViewById(R.id.buttonAccept);
            buttonReject = itemView.findViewById(R.id.buttonReject);
        }

        void bind(Order order) {
            textViewOrderId.setText(String.format("Order #%d", order.getId()));
            textViewClientName.setText(order.getClientName());
            textViewProductName.setText(order.getProductName());
            textViewQuantity.setText(String.format("Quantity: %.1f", order.getQuantity()));
            textViewTotalPrice.setText(String.format("Total: $%.2f", order.getTotalPrice()));
            textViewStatus.setText(order.getStatus());
            textViewOrderDate.setText(new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                .format(order.getOrderDate()));

            buttonAccept.setOnClickListener(v -> {
                if (statusChangeListener != null) {
                    statusChangeListener.onOrderStatusChanged(order, "CONFIRMED");
                }
            });

            buttonReject.setOnClickListener(v -> {
                if (statusChangeListener != null) {
                    statusChangeListener.onOrderStatusChanged(order, "CANCELLED");
                }
            });
        }
    }
}
