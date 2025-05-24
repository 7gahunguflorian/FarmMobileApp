package com.example.farmmobileapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.activities.OrderFormActivity;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.utils.Constants;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final String TAG = "ProductAdapter";
    private Context context;
    private List<Product> products;
    private boolean isOwnerView;

    public interface OnProductActionListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
        void onOrderProduct(Product product);
    }

    private OnProductActionListener actionListener;

    public ProductAdapter(Context context, List<Product> products) {
        this(context, products, false);
    }

    public ProductAdapter(Context context, List<Product> products, boolean isOwnerView) {
        this.context = context;
        this.products = products;
        this.isOwnerView = isOwnerView;
    }

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.actionListener = listener;
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
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvFarmerName;
        TextView tvPrice;
        TextView tvQuantity;
        Button btnAction1;
        Button btnAction2;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvFarmerName = itemView.findViewById(R.id.tvFarmerName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnAction1 = itemView.findViewById(R.id.btnAction1);
            btnAction2 = itemView.findViewById(R.id.btnAction2);
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvFarmerName.setText(product.getFarmerName() != null ? product.getFarmerName() : "Unknown Farmer");
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));
            tvQuantity.setText(String.format(Locale.getDefault(), "%d available", product.getAvailableQuantity()));

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                String imageUrl = product.getImageUrl();
                if (!imageUrl.startsWith("http")) {
                    if (imageUrl.startsWith("/")) {
                        imageUrl = RetrofitClient.BASE_URL + imageUrl.substring(1);
                    } else {
                        imageUrl = RetrofitClient.BASE_URL + "images/" + imageUrl;
                    }
                }

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.placeholder_product);
            }

            if (isOwnerView) {
                btnAction1.setText("Edit");
                btnAction1.setVisibility(View.VISIBLE);
                btnAction1.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onEditProduct(product);
                    }
                });

                btnAction2.setText("Delete");
                btnAction2.setVisibility(View.VISIBLE);
                btnAction2.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onDeleteProduct(product);
                    }
                });
            } else {
                btnAction1.setText("Order Now");
                btnAction1.setVisibility(View.VISIBLE);
                btnAction1.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onOrderProduct(product);
                    } else {
                        Intent intent = new Intent(context, OrderFormActivity.class);
                        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId().toString());
                        context.startActivity(intent);
                    }
                });

                btnAction2.setVisibility(View.GONE);
            }
        }
    }
}