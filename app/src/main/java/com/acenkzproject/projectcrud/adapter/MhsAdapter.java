package com.acenkzproject.projectcrud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.acenkzproject.projectcrud.R;
import com.acenkzproject.projectcrud.model.ModelMhs;
import com.bumptech.glide.Glide;

import java.util.List;

public class MhsAdapter extends RecyclerView.Adapter<MhsAdapter.MhsViewholder> {

    private Context context;

    private List<ModelMhs> list;

    public OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    public MhsAdapter(Context context, List<ModelMhs> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MhsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new MhsViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MhsAdapter.MhsViewholder holder, int position) {
        holder.nim.setText(list.get(position).getNim());
        holder.nama.setText(list.get(position).getNama());

        Glide.with(context)
                .load(list.get(position).getAvatar())
                .circleCrop()
                .into(holder.avatar);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MhsViewholder extends RecyclerView.ViewHolder {

        TextView nim, nama;
        ImageView avatar;

        public MhsViewholder(@NonNull View itemView) {
            super(itemView);

            nim = itemView.findViewById(R.id.nim1);
            nama = itemView.findViewById(R.id.nama1);
            avatar = itemView.findViewById(R.id.img_avatar);

            itemView.setOnClickListener(v -> {
                if (onItemClickCallback != null) {
                    onItemClickCallback.onItemClicked(getLayoutPosition());
                }
            });
        }
    }


    public interface OnItemClickCallback {
        void onItemClicked(int pos);
    }
}
