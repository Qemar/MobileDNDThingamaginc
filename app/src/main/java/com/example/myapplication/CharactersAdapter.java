// app/src/main/java/com/example/myapplication/CharactersAdapter.java
package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.CharacterDbHelper.IdName;

import java.util.ArrayList;
import java.util.List;

public class CharactersAdapter extends RecyclerView.Adapter<CharactersAdapter.VH> {

    public interface Callback { void onClick(IdName idName); }

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
        holder.itemView.setOnClickListener(v -> cb.onClick(in));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(View v) { super(v); tv = v.findViewById(R.id.tvRowName); }
    }
}
