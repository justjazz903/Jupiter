package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class RpcHelper {
	public static void writeJsonArray(HttpServletResponse response, JSONArray arr) throws IOException{
		response.setContentType("application/json");
		response.getWriter().print(arr);
	}
	
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException{
		response.setContentType("application/json");
		response.getWriter().print(obj);
	}
	
	public static JSONObject readJSONObject(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		try(BufferedReader reader = request.getReader()){
			String line = null;
			while((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return new JSONObject(sb.toString());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}
	
	public static Item parseFavoriteItem(JSONObject favoriteItem) throws JSONException{
		ItemBuilder builder = new ItemBuilder();
		builder.setItemId(favoriteItem.getString("item_id"))
			.setName(favoriteItem.getString("name"))
			.setRating(favoriteItem.getDouble("rating"))
			.setDistance(favoriteItem.getDouble("distance"))
			.setImageUrl(favoriteItem.getString("image_url"))
			.setUrl(favoriteItem.getString("url"))
			.setAddress(favoriteItem.getString("address"));
		Set<String> categories = new HashSet<>();
		if(!favoriteItem.isNull("categories")) {
			JSONArray arr = favoriteItem.getJSONArray("categories");
			for(int i = 0 ; i < arr.length(); i++) {
				categories.add(arr.getString(i));
			}
		}
		builder.setCategories(categories);
		return builder.build();
	}
}
