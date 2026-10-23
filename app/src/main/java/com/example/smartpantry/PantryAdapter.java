package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// The adapter is the "bridge" between the data (a List of PantryItem)
// and the RecyclerView on screen. RecyclerView reuses row views as you
// scroll, and the adapter fills each row with the right item's data.
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    // Lets MainActivity decide what happens on tap / long-press
    public interface Listener {
        void onItemClick(PantryItem item);
        void onItemLongClick(PantryItem item);
    }

    private List<PantryItem> items;
    private final Listener listener;

    public PantryAdapter(List<PantryItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    // Replace the data and tell the RecyclerView to redraw
    public void setItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    // 1) Called when a NEW row view is needed: inflate item_pantry.xml
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(row);
    }

    // 2) Called to fill a row with the data at 'position'
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.txtName.setText(item.getName());
        holder.txtDetails.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());

        // Expiry is optional: hide the line when empty
        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.trim().isEmpty()) {
            holder.txtExpiry.setVisibility(View.GONE);
        } else {
            holder.txtExpiry.setVisibility(View.VISIBLE);
            holder.txtExpiry.setText("Expires: " + expiry);
        }

        holder.itemView.setOnClickListener(view -> listener.onItemClick(item));
        holder.itemView.setOnLongClickListener(view -> {
            listener.onItemLongClick(item);
            return true; // true = we handled the long press
        });
    }

    // 3) How many rows in total
    @Override
    public int getItemCount() {
        return items.size();
    }

    // Shows 3 instead of 3.0, but keeps 0.5 as 0.5
    private String formatQuantity(double q) {
        if (q == Math.floor(q)) {
            return String.valueOf((long) q);
        }
        return String.valueOf(q);
    }

    // The ViewHolder keeps references to a row's views so we don't
    // call findViewById again every time the row is reused.
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDetails, txtExpiry;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            txtExpiry = itemView.findViewById(R.id.txtExpiry);
        }
    }
}