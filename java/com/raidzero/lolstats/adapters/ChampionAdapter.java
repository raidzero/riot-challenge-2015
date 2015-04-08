package com.raidzero.lolstats.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.data.Champion;

/**
 * Created by raidzero on 4/7/15.
 */
public class ChampionAdapter extends ArrayAdapter<Champion> {
    private static final String tag = "ChampionAdapter";
    private Context mContext;

    public ChampionAdapter(Context context, Champion[] champions) {
        super(context, 0);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Champion champion = getItem(position);

        // inflate a view if not resuing
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.champion_layout_bottom, parent, false);
        }

        // get views
        ImageView portraitView = (ImageView) convertView.findViewById(R.id.img_champion_portrait);
        TextView summonerName = (TextView) convertView.findViewById(R.id.txt_summoner_name);

        // load portrait image and set it
        String fgPath = mContext.getCacheDir() + champion.getChampionPortaitPath();
        Drawable portrait = Drawable.createFromPath(fgPath);

        portraitView.setImageDrawable(portrait);

        summonerName.setText(champion.summonerName);

        Log.d(tag, "Inflated view for " + champion.summonerName);
        return convertView;
    }

}
