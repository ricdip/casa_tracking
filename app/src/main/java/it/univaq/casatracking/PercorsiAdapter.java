package it.univaq.casatracking;


import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class PercorsiAdapter extends RecyclerView.Adapter<PercorsiAdapter.ViewHolder> {

    private JSONArray data;

    public PercorsiAdapter(JSONArray data){
        this.data = data;
        if(this.data == null)
            this.data = new JSONArray();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject item = data.optJSONObject(position);

        if(item == null)
            return;

        String title_string = item.optString("nome", "- - -");
        holder.title.setText(title_string);

        String subtitle_string = "Durata: " + item.optString("tempo", "- - -");
        holder.subtitle.setText(subtitle_string);
    }

    @Override
    public int getItemCount() {
        return data.length();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_scegli_percorso, parent, false);
        return new ViewHolder(view);
    }

    /* ViewHolder class */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, subtitle;

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.percorsi_title);
            subtitle = itemView.findViewById(R.id.percorsi_subtitle);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            JSONObject percorso = data.optJSONObject(getAdapterPosition());
            Intent intent = new Intent(v.getContext(), POIActivity.class);
            intent.putExtra("percorso", percorso.toString());

            v.getContext().startActivity(intent);
        }
    }

}
