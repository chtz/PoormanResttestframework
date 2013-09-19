package ch.furthermore.poorman.resttestframework.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonHelper {
	public static String prettyPrint(String json) {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(json);
			return gson.toJson(je);
		}
		catch (Exception e) {
			return null;
		}
	}
}
