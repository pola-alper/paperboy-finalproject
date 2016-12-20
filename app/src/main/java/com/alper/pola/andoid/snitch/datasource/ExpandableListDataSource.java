package com.alper.pola.andoid.snitch.datasource;

import android.content.Context;


import com.alper.pola.andoid.snitch.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by msahakyan on 22/10/15.
 */
public class ExpandableListDataSource {

    /**
     * Returns fake data of films
     *
     * @param context
     * @return
     */
    public static Map<String, List<String>> getData(Context context) {
        Map<String, List<String>> expandableListData = new TreeMap<>();

        List<String> filmGenres = Arrays.asList(context.getResources().getStringArray(R.array.film_genre));

        List<String> actionFilms = Arrays.asList(context.getResources().getStringArray(R.array.actionFilms));


        expandableListData.put(filmGenres.get(0), actionFilms);
        expandableListData.put(filmGenres.get(1), Collections.<String>emptyList());
        expandableListData.put(filmGenres.get(2), Collections.<String>emptyList());
        expandableListData.put(filmGenres.get(3), Collections.<String>emptyList());
        expandableListData.put(filmGenres.get(4), Collections.<String>emptyList());





        return expandableListData;
    }
}
