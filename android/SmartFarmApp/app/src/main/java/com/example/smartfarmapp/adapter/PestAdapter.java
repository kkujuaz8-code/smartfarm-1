package com.example.smartfarmapp.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.activity.search.DetailActivity;
import com.example.smartfarmapp.model.PestResponse;
import java.util.List;

public class PestAdapter extends RecyclerView.Adapter<PestAdapter.PestViewHolder> {
    private List<PestResponse> pestList;
    private static final String TAG = "PestAdapter_Log";

    public PestAdapter(List<PestResponse> pestList) { this.pestList = pestList; }

    @NonNull
    @Override
    public PestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pest, parent, false);
        return new PestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PestViewHolder holder, int position) {
        PestResponse item = pestList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCrop.setText(item.getDescription());

        String imageUrl = item.getImgUrl();

        // 🌟 이미지 URL 보안 처리 (http -> https)
        if (imageUrl != null && imageUrl.startsWith("http://")) {
            imageUrl = imageUrl.replace("http://", "https://");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // 🌟 [핵심 추가] 병해충 항목 클릭 시 상세 페이지로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);

            // 상세 페이지로 넘길 데이터들
            intent.putExtra("name", item.getName());
            intent.putExtra("desc", item.getDescription()); // 증상/설명
            intent.putExtra("imgUrl", item.getImgUrl());

            // 🐛 병해충 전용 데이터 (나중에 DB 수동 입력 후 연동)
            // 식물 데이터(water 등)와 구분하기 위해 solution만 보냅니다.
            intent.putExtra("solution", item.getSolution());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return pestList != null ? pestList.size() : 0; }

    static class PestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCrop;
        ImageView ivImage;
        public PestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPestName);
            tvCrop = itemView.findViewById(R.id.tvCropName);
            ivImage = itemView.findViewById(R.id.ivPestImage);
        }
    }
}