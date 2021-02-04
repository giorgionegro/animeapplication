package com.anime.AnimeIndexer;

import java.util.ArrayList;
import java.util.List;

public class GlobalVariable {
    public final CharSequence[] Entryvalues;
    public final List references;
    public final CharSequence[] Entry;
    public GlobalVariable(List<List> sresult) {
        List Entry = new ArrayList<String>();
        List Values = new ArrayList<String>();
        List references = new ArrayList();
        Entry.clear();
        Values.clear();
        references.clear();
        for (List l : sresult) {
            Entry.add(l.get(0));
            Values.add(l.get(1));
            try {
                references.add(l.get(2));
            } catch (Exception e) {
                references.add("");
            }
        }


        this.Entry = (CharSequence[]) Entry.toArray(new CharSequence[Entry.size()]);
        this.Entryvalues = (CharSequence[]) Values.toArray(new CharSequence[Values.size()]);
        this.references = references;
    }

    public String get_references_by_entry(String entry) {
        int j = 0;
        for (CharSequence i : Entryvalues) {
            String s = i.toString();
            if (s.equals(entry)) {
                try {
                    return (String) references.get(j);
                } catch (Exception e) {
                    return null;
                }


            }
            j++;


        }
        return null;


    }



}
