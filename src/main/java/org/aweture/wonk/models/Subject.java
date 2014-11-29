package org.aweture.wonk.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class Subject extends HashMap<String, String> {
	
	private static Subject instance = new Subject();
	
	private Subject() {
		put("LQ", "LionsQuest");
		put("Bi", "Biologie");
		put("E", "Englisch");
		put("MFö", "Mathe (Förder.)");
		put("D", "Deutsch");
		put("Mu", "Musik");
		put("Spo", "Sport");
		put("L2", "Latein");
		put("L3", "Latein");
		put("L2/3", "Latein");
		put("F2", "Französisch");
		put("F3", "Französisch");
		put("F2/3", "Französisch");
		put("Ek", "Erdkunde");
		put("G", "Geschichte");
		put("Ph", "Physik");
		put("Rel", "Religion");
		put("kRel", "kath. Religion");
		put("Ku", "Kunst");
		put("Ch", "Chemie");
		put("WP", "Wirtschaft/Politik");
		put("Phil", "Philosophie");
		put("M", "Mathe");
		put("KfD", "Deutsch");
		put("KfM", "Mathe");
		put("KfE", "Englisch");
		put("MZ", "Musik");
	}
	
	public static String getFullName(String key) {
		String name = instance.get(key);
		return name != null ? name : key;
	}
	
	public static List<String> getAsList() {
		List<String> subjects = new ArrayList<String>();
		Set<String> keys = instance.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String current = instance.get(iterator.next());
			if (!subjects.contains(current))
			subjects.add(current);
		}
		subjects.add("Freistunde");
		return subjects;
	}

}
