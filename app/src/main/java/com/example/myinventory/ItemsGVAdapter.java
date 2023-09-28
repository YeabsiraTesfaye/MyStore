package com.example.myinventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.myinventory.ui.home.Items;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ItemsGVAdapter extends ArrayAdapter<Items> {
	public ItemsGVAdapter(@NonNull Context context, ArrayList<Items> itemsModelArrayList) {
		super(context, 0, itemsModelArrayList);
	}
	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

		View listitemView = convertView;
		if (listitemView == null) {
			listitemView = LayoutInflater.from(getContext()).inflate(R.layout.image_view, parent, false);
		}

		Items item = getItem(position);
		TextView courseTV = listitemView.findViewById(R.id.name);
		TextView desc = listitemView.findViewById(R.id.description);
		TextView price = listitemView.findViewById(R.id.price);
		ImageView courseIV = listitemView.findViewById(R.id.idIVItem);
		courseTV.setText(item.getName());
		desc.setText(item.getDescription());
		price.setText(item.getSell()+ " ETB");



		FirebaseDatabase firebaseDatabase
				= FirebaseDatabase.getInstance();
		DatabaseReference databaseReference
				= firebaseDatabase.getReference();
		DatabaseReference getImage
				= databaseReference.child("Items");
		getImage.addListenerForSingleValueEvent(
				new ValueEventListener() {
					@Override
					public void onDataChange(
							@NonNull DataSnapshot dataSnapshot)
					{
						if(item.getUri() != ""){
							Glide.with(getContext()).load(item.getUri()).into(courseIV);
						}else{
							Glide.with(getContext()).load(R.drawable.image).into(courseIV);
						}
					}
					@Override
					public void onCancelled(
							@NonNull DatabaseError databaseError)
					{
						Toast.makeText(getContext(), "Error Loading Image", Toast.LENGTH_SHORT).show();
					}
				});
		return listitemView;
	}
}
