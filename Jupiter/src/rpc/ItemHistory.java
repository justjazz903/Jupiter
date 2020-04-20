package rpc;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import DB.MySQLConnection;
import entity.Item;
import entity.Item.ItemBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
    	
    	// allow access only if session exists
    	HttpSession session = request.getSession(false);
   		if (session == null) {
   		response.setStatus(403);
    		return;
    	}

		String userId = request.getParameter("user_id");
		JSONArray array = new JSONArray();

		MySQLConnection connection = new MySQLConnection();
		Set<Item> items = connection.getFavoriteItems(userId);
		connection.close();
		
		for (Item item : items) {
			JSONObject obj = item.toJSONObject();
			try {
				obj.append("favorite", true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			array.put(obj);
		}
		RpcHelper.writeJsonArray(response, array);
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
   		if (session == null) {
   		response.setStatus(403);
    		return;
    	}
		JSONObject input = RpcHelper.readJSONObject(request);
		try {
			String userId = input.getString("user_id");
			Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));
			
			MySQLConnection conn = new MySQLConnection();
			conn.setFavoriteItems(userId, item);
			conn.close();
			
			RpcHelper.writeJsonObject(response,new JSONObject().put("result", "SUCCESS"));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
   		if (session == null) {
   		response.setStatus(403);
    		return;
    	}
		JSONObject input = RpcHelper.readJSONObject(request);
		try {
			String userId = input.getString("user_id");
			String itemId = input.getJSONObject("favorite").getString("item_id");

			MySQLConnection connection = new MySQLConnection();
			connection.unsetFavoriteItems(userId, itemId);
			connection.close();
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
