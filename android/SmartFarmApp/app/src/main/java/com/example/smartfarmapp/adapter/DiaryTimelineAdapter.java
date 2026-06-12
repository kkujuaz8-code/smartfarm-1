package com.example.smartfarmapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // 🌟 이미지 로딩 라이브러리
import com.example.smartfarmapp.activity.diary.DiaryActivity;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.model.DiaryEntry;

import java.util.List;

public class DiaryTimelineAdapter extends RecyclerView.Adapter<DiaryTimelineAdapter.ViewHolder> {

    private Context context;
    private List<DiaryEntry> diaryList;

    public DiaryTimelineAdapter(Context context, List<DiaryEntry> diaryList) {
        this.context = context;
        this.diaryList = diaryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diary_timeline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryEntry entry = diaryList.get(position);

        // 1. 텍스트 설정
        holder.tvDateHeader.setText(entry.getDate());
        holder.tvTitle.setText(entry.getTitle());
        holder.tvContent.setText(entry.getContent());

        // 2. 날짜 헤더 (같은 날짜면 숨김)
        if (position > 0 && entry.getDate().equals(diaryList.get(position - 1).getDate())) {
            holder.layoutDateHeader.setVisibility(View.GONE);
        } else {
            holder.layoutDateHeader.setVisibility(View.VISIBLE);
        }

        // 3. 🌟 [핵심] 이미지 처리 (URL이 있으면 보여주고, 없으면 숨김)
        if (entry.getImageUrl() != null && !entry.getImageUrl().isEmpty()) {
            holder.ivItemPhoto.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(entry.getImageUrl()) // DB에 있는 주소 로딩
                    .centerCrop()
                    .into(holder.ivItemPhoto);
        } else {
            holder.ivItemPhoto.setVisibility(View.GONE); // 없으면 숨기기
        }

        // 4. 클릭 시 상세/수정 화면으로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DiaryActivity.class);
            intent.putExtra("id", entry.getId());
            intent.putExtra("title", entry.getTitle());
            intent.putExtra("content", entry.getContent());
            intent.putExtra("date", entry.getDate());
            intent.putExtra("folderId", entry.getFolderId());

            // 🌟 중요: 수정 화면으로 갈 때 이미지 주소도 꼭 챙겨가야 함!
            intent.putExtra("imageUrl", entry.getImageUrl());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutDateHeader;
        TextView tvDateHeader, tvTitle, tvContent;
        ImageView ivItemPhoto; // 🌟 이미지뷰 추가

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutDateHeader = itemView.findViewById(R.id.layoutDateHeader);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            ivItemPhoto = itemView.findViewById(R.id.ivItemPhoto); // 🌟 연결
        }
    }
}