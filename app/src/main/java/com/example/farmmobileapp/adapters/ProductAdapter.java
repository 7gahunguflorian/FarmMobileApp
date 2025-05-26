package com.example.farmmobileapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.utils.Constants;
import com.example.farmmobileapp.utils.ImageUtils;
import com.example.farmmobileapp.utils.SessionManager;
import com.example.farmmobileapp.network.RetrofitClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private Context context;
    private boolean showOrderButton;
    private OnProductActionListener listener;
    private OnOrderClickListener orderClickListener;

    public interface OnProductActionListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public interface OnOrderClickListener {
        void onOrderClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> products, boolean showOrderButton) {
        this.context = context;
        this.products = products;
        this.showOrderButton = showOrderButton;
    }

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.listener = listener;
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.orderClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView nameTextView;
        private TextView priceTextView;
        private TextView descriptionTextView;
        private TextView farmerNameTextView;
        private TextView availableQtyTextView;
        private Button orderButton;
        private ImageButton editButton;
        private ImageButton deleteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewProduct);
            nameTextView = itemView.findViewById(R.id.textViewProductName);
            priceTextView = itemView.findViewById(R.id.textViewPrice);
            descriptionTextView = itemView.findViewById(R.id.textViewDescription);
            farmerNameTextView = itemView.findViewById(R.id.textViewFarmerName);
            availableQtyTextView = itemView.findViewById(R.id.textViewAvailableQty);
            orderButton = itemView.findViewById(R.id.buttonOrder);
            editButton = itemView.findViewById(R.id.buttonEdit);
            deleteButton = itemView.findViewById(R.id.buttonDelete);

            // Set visibility based on showOrderButton flag
            orderButton.setVisibility(showOrderButton ? View.VISIBLE : View.GONE);
            editButton.setVisibility(showOrderButton ? View.GONE : View.VISIBLE);
            deleteButton.setVisibility(showOrderButton ? View.GONE : View.VISIBLE);
        }

        public void bind(Product product) {
            nameTextView.setText(product.getName());
            // Format price as integer with FBU currency
            priceTextView.setText(String.format(Locale.getDefault(), "%,d FBU", product.getPrice().intValue()));
            descriptionTextView.setText(product.getDescription());
            farmerNameTextView.setText("Farmer: " + product.getFarmerName());
            availableQtyTextView.setText(String.format(Locale.getDefault(), "Available: %d", product.getAvailableQuantity()));

            // Load image using Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                String imageUrl = product.getImageUrl();
                // Remove leading slash and 'images/' prefix if present
                if (imageUrl.startsWith("/")) {
                    imageUrl = imageUrl.substring(1);
                }
                if (imageUrl.startsWith("images/")) {
                    imageUrl = imageUrl.substring(7); // Remove "images/" prefix
                }
                // Construct full URL for product images without /api/
                imageUrl = RetrofitClient.getBaseUrl().replace("/api", "") + "images/" + imageUrl;

                // Get auth token from SessionManager
                String authToken = SessionManager.getInstance(context).getAuthToken();
                if (authToken != null) {
                    // Create GlideUrl with auth header
                    GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + authToken)
                            .build());

                    Glide.with(context)
                        .load(glideUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.placeholder_product);
                }
            } else {
                imageView.setImageResource(R.drawable.placeholder_product);
            }

            // Set click listeners
            if (showOrderButton) {
                orderButton.setOnClickListener(v -> {
                    if (orderClickListener != null) {
                        orderClickListener.onOrderClick(product);
                    }
                });
            } else {
                editButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditClick(product);
                    }
                });

                deleteButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClick(product);
                    }
                });
            }
        }
    }
}