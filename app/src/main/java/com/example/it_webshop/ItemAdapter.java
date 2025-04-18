package com.example.it_webshop;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<Item> mItemData;
    private ArrayList<Item> mItemDataall;
    private Context mContext;
    private static final String LOG_TAG = ItemAdapter.class.getName();

    private int lastpos = -1;

    ItemAdapter(Context context, ArrayList<Item> itemsData){
        this.mItemData = itemsData;
        this.mItemDataall = itemsData;
        this.mContext = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_items, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        Item currentItem = mItemData.get(position);

        holder.bindTo(currentItem);

        if(holder.getAdapterPosition() > lastpos) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_from_left);
            holder.itemView.startAnimation(animation);
            lastpos = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mItemData.size();
    }

    @Override
    public Filter getFilter() {
        return shoppingFilter;
    }
    private Filter shoppingFilter = new Filter(){

        @Override
        protected FilterResults performFiltering(CharSequence charsequence) {
            ArrayList<Item> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(charsequence == null || charsequence.length() == 0){
                results.count = mItemDataall.size();
                results.values = mItemDataall;
            }else{
                String filterPattern = charsequence.toString().toLowerCase().trim();

                for(Item item: mItemDataall){
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mItemData = (ArrayList)results.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTitleText;
        private TextView mDescText;
        private TextView mPriceText;
        private ImageView mItemImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitleText = itemView.findViewById(R.id.itemTitle);
            mDescText = itemView.findViewById(R.id.subTitle);
            mPriceText = itemView.findViewById(R.id.price);
            mItemImg = itemView.findViewById(R.id.itemImage);

        }

        public void bindTo(Item currentItem) {
            mTitleText.setText(currentItem.getName());
            mDescText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice());

            Glide.with(mContext).load(currentItem.getImageResource()).into(mItemImg);
            itemView.findViewById(R.id.add_to_cart).setOnClickListener(v -> {
                Log.d(LOG_TAG, "Add to cart clicked");
                ((ShoppingActivity)mContext).updateAlertIcon(currentItem);
            });
            itemView.findViewById(R.id.delete).setOnClickListener(v -> {
                Log.d(LOG_TAG, "Delete button clicked");
                ((ShoppingActivity)mContext).deleteItem(currentItem);
            });
        }
    }
}


