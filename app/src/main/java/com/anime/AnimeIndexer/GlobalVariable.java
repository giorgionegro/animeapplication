package com.anime.AnimeIndexer;

import java.util.ArrayList;
import java.util.List;

public class GlobalVariable {
    public final CharSequence[] Entryvalues;
    public final List<String> references;
    public final CharSequence[] Entry;
    public GlobalVariable(List<List<String>> sresult) {
        List<String> Entry = new ArrayList<>();
        List<String> Values = new ArrayList<>();
        List<String> references = new ArrayList<>();
        Entry.clear();
        Values.clear();
        references.clear();
        for (List<String> l : sresult) {
            Entry.add(l.get(0));
            Values.add(l.get(1));
            try {
                references.add(l.get(2));
            } catch (Exception e) {
                references.add("");
            }
        }


        this.Entry = Entry.toArray(new CharSequence[0]);
        this.Entryvalues = Values.toArray(new CharSequence[0]);
        this.references = references;
    }

    public String get_references_by_entry(String entry) {
        int j = 0;
        for (CharSequence i : Entryvalues) {
            String s = i.toString();
            if (s.equals(entry)) {
                try {
                    return references.get(j);
                } catch (Exception e) {
                    return null;
                }


            }
            j++;


        }
        return null;


    }



}
