package com.example.smartfarmapp.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.activity.search.DetailActivity;
import com.example.smartfarmapp.model.PlantResponse;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<PlantResponse> plantList;

    public PlantAdapter(List<PlantResponse> plantList) {
        this.plantList = plantList;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantResponse item = plantList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvScName.setText(item.getDescription());

        // 🌟 [중요] 이미지 URL 전처리 로직
        String rawImageUrl = item.getImgUrl();
        String finalImageUrl = "";

        if (rawImageUrl != null && !rawImageUrl.isEmpty()) {
            // 1. 여러 개가 섞여 들어올 경우 (| 구분자), 첫 번째 URL만 사용
            if (rawImageUrl.contains("|")) {
                finalImageUrl = rawImageUrl.split("\\|")[0];
            } else {
                finalImageUrl = rawImageUrl;
            }

            // 2. 보안 차단 방지를 위해 http -> https 변환
            if (finalImageUrl.startsWith("http://")) {
                finalImageUrl = finalImageUrl.replace("http://", "https://");
            }
        }

        // Glide를 통한 이미지 로드
        if (!finalImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(finalImageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground) // 로딩 중 이미지
                    .error(R.drawable.ic_launcher_foreground)       // 에러 시 이미지
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // 🌟 항목 클릭 시 상세 페이지로 이동
        final String imageToSend = finalImageUrl; // 익명 클래스 사용을 위한 final 변수
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);

            intent.putExtra("name", item.getName());
            intent.putExtra("desc", item.getDescription());
            // 상세 페이지에서도 바로 쓸 수 있게 전처리가 끝난 URL을 보냅니다.
            intent.putExtra("imgUrl", imageToSend);

            intent.putExtra("water", item.getWaterCycle());
            intent.putExtra("repot", item.getRepotCycle());
            intent.putExtra("sunlight", item.getSunlight());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return plantList != null ? plantList.size() : 0; }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvScName;
        ImageView ivImage;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPlantName);
            tvScName = itemView.findViewById(R.id.tvScName);
            ivImage = itemView.findViewById(R.id.ivPlantImage);
        }
    }
}