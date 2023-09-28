package com.example.myinventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myinventory.ui.home.Items;

import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {
	// creating variables for our ArrayList and context
	private final ArrayList<Items> ItemsArrayList;
	private final Context context;

	// creating constructor for our adapter class
	public RVAdapter(ArrayList<Items> ItemsArrayList, Context context) {
		this.ItemsArrayList = ItemsArrayList;
		this.context = context;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		// passing our layout file for displaying our card item
		return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.image_view, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		// setting data to our text views from our modal class.
		Items item= ItemsArrayList.get(position);
		holder.name.setText(item.getName());
		holder.description.setText(item.getDescription());
		holder.quantity.setText(item.getQuantity());
		Glide.with(context).load(item.getUri()).into(holder.image);

	}

	@Override
	public int getItemCount() {
		// returning the size of our array list.
		return ItemsArrayList.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		// creating variables for our text views.
		private final TextView name, description, quantity;
		ImageView image;
		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			// initializing our text views.
			name = itemView.findViewById(R.id.name);
			description = itemView.findViewById(R.id.description);
			quantity = itemView.findViewById(R.id.quantity);
			image = itemView.findViewById(R.id.idIVItem);
		}
	}
}
