package com.multimedia.centercontroller;

import java.util.HashMap;
import java.util.Map;

public class GroupInfo {

	public static Map<String, String> sGroupIdMap = new HashMap<String, String>();
	private static int GROUP_COUNT = 100;
	private static final String sGroupPrefix = "230.1.3.";
	static {
		for (int i = 1; i < GROUP_COUNT; i++) {

			sGroupIdMap.put("g" + i, sGroupPrefix + i);
		}
	}

	public static String getGroupId(String group) {
		if (sGroupIdMap.containsKey(group)) {
			return sGroupIdMap.get(group);
		} else {
			String tempId = sGroupPrefix + GROUP_COUNT;
			sGroupIdMap.put(group, tempId);
			GROUP_COUNT++;
			return tempId;
		}
	}

}
