package com.sh3m.recipebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class RecipeAdapter extends BaseAdapter {

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe);
    }

    private final Context context;
    private List<Recipe> recipes;
    private final OnItemClickListener listener;

    public RecipeAdapter(Context context, List<Recipe> recipes, OnItemClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return recipes.size(); }
    @Override public Recipe getItem(int pos) { return recipes.get(pos); }
    @Override public long getItemId(int pos) { return recipes.get(pos).id; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Recipe recipe = recipes.get(position);
        holder.tvName.setText(recipe.name);

        if (recipe.description != null && !recipe.description.isEmpty()) {
            holder.tvDescription.setText(recipe.description);
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        holder.tvMeta.setText(
                context.getString(R.string.ingredients_count,
                        recipe.ingredients.size(), recipe.steps.size())
        );

        if (recipe.imagePath != null && !recipe.imagePath.isEmpty()) {
            File f = new File(recipe.imagePath);
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bmp != null) {
                    holder.imgThumbnail.setImageBitmap(bmp);
                } else {
                    holder.imgThumbnail.setImageResource(R.drawable.ic_recipe_placeholder);
                }
            } else {
                holder.imgThumbnail.setImageResource(R.drawable.ic_recipe_placeholder);
            }
        } else {
            holder.imgThumbnail.setImageResource(R.drawable.ic_recipe_placeholder);
        }

        final Recipe r = recipe;
        convertView.setOnClickListener(v -> listener.onItemClick(r));
        return convertView;
    }

    static class ViewHolder {
        ImageView imgThumbnail;
        TextView tvName, tvDescription, tvMeta;

        ViewHolder(View v) {
            imgThumbnail = (ImageView) v.findViewById(R.id.imgThumbnail);
            tvName = (TextView) v.findViewById(R.id.tvName);
            tvDescription = (TextView) v.findViewById(R.id.tvDescription);
            tvMeta = (TextView) v.findViewById(R.id.tvMeta);
        }
    }
}
