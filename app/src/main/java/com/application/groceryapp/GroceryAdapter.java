package com.application.groceryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.GroceryViewholder> {

    LayoutInflater layoutInflater;
    List<GroceryDetails> groceryDetails;

    public GroceryAdapter(Context ctx, List<GroceryDetails> details){
        this.layoutInflater=LayoutInflater.from(ctx);
        this.groceryDetails=details;
    }

    @NonNull
    @Override
    public GroceryViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.groceryitem, parent, false);
        return new GroceryViewholder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GroceryViewholder holder, int position) {
        holder.itemName.setText(groceryDetails.get(position).getGroceryName());
        holder.itemPlace.setText(groceryDetails.get(position).getGroceryPlace());
        holder.itemPrice.setText("₹"+groceryDetails.get(position).getGroceryPrice());

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(groceryDetails.get(position).getGroceryTime()*1000);
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();
        holder.itemDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return groceryDetails.size();
    }

    public static class GroceryViewholder extends RecyclerView.ViewHolder {
        TextView itemName,itemPlace,itemPrice,itemDate;

        public GroceryViewholder(@NonNull View itemView) {

            super(itemView);
            itemName=itemView.findViewById(R.id.ItemNameView);
            itemPlace=itemView.findViewById(R.id.ItemPlace);
            itemPrice=itemView.findViewById(R.id.ItemPrice);
            itemDate=itemView.findViewById(R.id.ItemDate);
        }
    }
}
