package com.example.farmmobileapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.farmmobileapp.activities.OrderFormActivity;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.utils.Constants;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends BaseAdapter {

    private Context context;
    private List<Product> productList;
    private LayoutInflater inflater;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_product, parent, false);
            holder = new ViewHolder();
            holder.imgProduct = convertView.findViewById(R.id.imgProduct);
            holder.tvProductName = convertView.findViewById(R.id.tvProductName);
            holder.tvFarmerName = convertView.findViewById(R.id.tvFarmerName);
            holder.tvPrice = convertView.findViewById(R.id.tvPrice);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.btnPlaceOrder = convertView.findViewById(R.id.btnPlaceOrder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Product product = productList.get(position);

        // Set product details
        holder.tvProductName.setText(product.getName());
        holder.tvFarmerName.setText(product.getFarmerName() != null ? product.getFarmerName() : "Unknown Farmer");
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));
        holder.tvQuantity.setText(String.format(Locale.getDefault(), "%d available", product.getAvailableQuantity()));

        // Load image using Glide
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            String imageUrl = product.getImageUrl();
            if (!imageUrl.startsWith("http")) {
                imageUrl = RetrofitClient.BASE_URL + "images/" + imageUrl;
            }

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.placeholder_product);
        }

        // Set place order button click listener
        holder.btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OrderFormActivity.class);
                intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId().toString());
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvFarmerName;
        TextView tvPrice;
        TextView tvQuantity;
        Button btnPlaceOrder;
    }
}