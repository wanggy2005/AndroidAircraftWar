package edu.hitsz.score;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.hitsz.R;

public class ScoreAdapter extends BaseAdapter {

    private final Context context;
    private final List<ScoreItem> dataList;
    private final LayoutInflater inflater;
    private int selectedPosition = -1;

    public ScoreAdapter(Context context, List<ScoreItem> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public ScoreItem getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return dataList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_score, parent, false);
            holder = new ViewHolder();
            holder.tvRank = convertView.findViewById(R.id.tvRank);
            holder.tvName = convertView.findViewById(R.id.tvName);
            holder.tvScore = convertView.findViewById(R.id.tvScore);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScoreItem item = getItem(position);
        holder.tvRank.setText(getRankIcon(position));
        holder.tvName.setText(item.getName());
        holder.tvScore.setText(item.getScore() + "分");
        holder.tvRank.setTextColor(getRankColor(position));
        holder.tvName.setTextColor(getRankColor(position));
        holder.tvScore.setTextColor(getRankColor(position));

        if (position == selectedPosition) {
            convertView.setBackgroundColor(Color.parseColor("#33FFFFFF"));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    public void refreshData(List<ScoreItem> newData) {
        dataList.clear();
        dataList.addAll(newData);
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    private String getRankIcon(int position) {
        switch (position) {
            case 0:
                return "🥇";
            case 1:
                return "🥈";
            case 2:
                return "🥉";
            default:
                return String.valueOf(position + 1);
        }
    }

    private int getRankColor(int position) {
        if (position == 0) {
            return Color.parseColor("#FFD700");
        }
        if (position == 1) {
            return Color.parseColor("#C0C0C0");
        }
        if (position == 2) {
            return Color.parseColor("#CD7F32");
        }
        return Color.parseColor("#B8B8D1");
    }

    private static class ViewHolder {
        TextView tvRank;
        TextView tvName;
        TextView tvScore;
    }
}
