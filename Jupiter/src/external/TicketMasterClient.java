package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;


public class TicketMasterClient {
	private static final String HOST = "https://app.ticketmaster.com";
	private static final String PATH = "/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "event";
	private static final int DEFAULT_RADIUS = 50;
	private static final String API_KEY = "9ty4AjKyWlZGW4F1NZn9CAnJFIjO39DQ";
	
	public List<Item> search(double lat, double lon, String keyword) {
		if(keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		//assemble query
		String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radius=%s",
				API_KEY, lat, lon, keyword, DEFAULT_RADIUS);
		String url = HOST + PATH + "?" + query;
		StringBuilder responseBuilder = new StringBuilder();
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			
			int responseCode = connection.getResponseCode();
			
			if(responseCode != 200) {
				return new ArrayList<Item>();
			}
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			String line;
			while((line = reader.readLine()) != null) {
				responseBuilder.append(line);
			}
			
			reader.close();
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		try {
			JSONObject obj = new JSONObject(responseBuilder.toString());
			if(!obj.isNull("_embedded")) {
				JSONObject embedded = obj.getJSONObject("_embedded");
				return getItemList(embedded.getJSONArray("events"));
			}
		}
		catch(JSONException e) {
			e.printStackTrace();
		}
		return new ArrayList<Item>();
	}
	
	private List<Item> getItemList(JSONArray events) throws JSONException{
		List<Item> itemList = new ArrayList<>();
		
		for(int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			
			if(!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			
			if(!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			
			builder.setAddress(getAddress(event))
			.setCategories(getCategories(event))
			.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build());
		}
		return itemList;
	}
	
	private String getAddress(JSONObject event) throws JSONException{
		if(!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if(!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for(int i = 0; i < venues.length(); i++) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder sb = new StringBuilder();
					if(!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							sb.append(address.getString("line1"));
						}
						
						if (!address.isNull("line2")) {
							sb.append(",");
							sb.append(address.getString("line2"));
						}
						
						if (!address.isNull("line3")) {
							sb.append(",");
							sb.append(address.getString("line3"));
						}
					}
					if(!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						sb.append(",");
						sb.append(city.getString("name"));
					}
					String res = sb.toString();
					if(!res.isEmpty()) {
						return res;
					}
				}
			}
		}
		return "";
	}
	
	private String getImageUrl(JSONObject event) throws JSONException{
		if(!event.isNull("images")) {
			JSONArray arr = event.getJSONArray("images");
			for(int i = 0; i < arr.length(); i++) {
				JSONObject img = arr.getJSONObject(i);
				if(!img.isNull("url")) {
					return img.getString("url");
				}
			}
		}
		return "";
	}
	
	private Set<String> getCategories(JSONObject event) throws JSONException {		
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}

	
//	public static void main(String[] args) {
//		TicketMasterClient client = new TicketMasterClient();
//		List<Item> events = client.search(37.38, -122.08, null);
//
//		for (Item event : events) {
//			System.out.println(event.toJSONObject());
//		}	
//	}

}
