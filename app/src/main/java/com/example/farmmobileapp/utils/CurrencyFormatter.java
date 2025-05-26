package com.example.farmmobileapp.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    private static final Locale BURUNDI_LOCALE = new Locale("fr", "BI");

    public static String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(BURUNDI_LOCALE);
        return formatter.format(price).replace("BIF", "FBU");
    }

    public static String formatPrice(String price) {
        try {
            double priceValue = Double.parseDouble(price);
            return formatPrice(priceValue);
        } catch (NumberFormatException e) {
            return "0 FBU";
        }
    }
}