package com.example.smartfarmapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartfarmapp.activity.diary.DiaryTimelineActivity;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.model.DiaryFolder;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private Context context;
    private List<DiaryFolder> folderList;
    private OnFolderActionListener listener;

    public interface OnFolderActionListener {
        void onDeleteRequest(Long folderId, String folderName);
    }

    public FolderAdapter(Context context, List<DiaryFolder> folderList, OnFolderActionListener listener) {
        this.context = context;
        this.folderList = folderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryFolder folder = folderList.get(position);
        holder.tvFolderName.setText(folder.getName());

        // 클릭: 타임라인으로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DiaryTimelineActivity.class);
            intent.putExtra("folderId", folder.getId());
            intent.putExtra("folderName", folder.getName());
            context.startActivity(intent);
        });

        // 롱클릭: 삭제 요청
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDeleteRequest(folder.getId(), folder.getName());
            return true;
        });
    }

    @Override
    public int getItemCount() { return folderList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
        }
    }
}