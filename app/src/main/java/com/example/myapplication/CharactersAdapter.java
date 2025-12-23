package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.CharacterDbHelper.IdName;

import java.util.ArrayList;
import java.util.List;

public class CharactersAdapter extends RecyclerView.Adapter<CharactersAdapter.VH> {

    public interface Callback {
        void onEdit(IdName item);
        void onDelete(IdName item);
        void onOpen(IdName item); // optional: open detail when row clicked
    }

    private final Callback cb;
    private final List<IdName> items = new ArrayList<>();

    public CharactersAdapter(Callback cb) { this.cb = cb; }

    public void setItems(List<IdName> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_character_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        IdName in = items.get(position);
        holder.tv.setText(in.name == null || in.name.isEmpty() ? ("#" + in.id) : in.name);

        holder.btnEdit.setOnClickListener(v -> {
            if (cb != null) cb.onEdit(in);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (cb != null) cb.onDelete(in);
        });

        holder.itemView.setOnClickListener(v -> {
            if (cb != null) cb.onOpen(in);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        Button btnEdit, btnDelete;
        VH(View v) {
            super(v);
            tv = v.findViewById(R.id.tvRowName);
            btnEdit = v.findViewById(R.id.btnEditRow);
            btnDelete = v.findViewById(R.id.btnDeleteRow);
        }
    }
}
