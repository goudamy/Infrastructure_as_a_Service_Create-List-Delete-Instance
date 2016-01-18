package com.mtaas.Model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mtaas.Utilities.Dataproperties;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

public class IntanceDetails {
	static StringBuffer returnData = null;

	public static HttpResponse get(String hostUrl, String restEndpointUrl,
			String token) {
		HttpResponse httpResponse = null;

		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			String url = hostUrl + "/" + restEndpointUrl;
			HttpGet request = new HttpGet(url);
			if (token != null) {
				request.addHeader("X-Auth-Token", token);
			}
			httpResponse = httpClient.execute(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpResponse;
	}

	public static HttpResponse post(String hostUrl, String restEndpointUrl,
			String entity, String token) {
		HttpResponse httpResponse = null;

		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			String url = hostUrl + "/" + restEndpointUrl;
			HttpPost request = new HttpPost(url);

			request.addHeader("content-type", "application/json");
			if (token != null) {
				request.addHeader("X-Auth-Token", token);
			}

			if (entity != null) {
				StringEntity params = new StringEntity(entity);
				request.setEntity(params);
			}
			httpResponse = httpClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpResponse;
	}

	public static HttpResponse delete(String hostUrl, String restEndpointUrl,
			String instanceId, String token) {
		HttpResponse httpResponse = null;

		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			String url = hostUrl + "/" + restEndpointUrl + "/" + instanceId;
			HttpDelete request = new HttpDelete(url);
			if (token != null) {
				request.addHeader("X-Auth-Token", token);
			}
			httpResponse = httpClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpResponse;
	}

	public void createInstance(String hostIp, String tokenId, String tenantId,
			String flavorId, String imageName, String serverName) {
		String hostUrl = "http://" + hostIp + ":8774";
		String endPointUrl = "v2/" + tenantId + "/servers";

		HttpResponse resp = null;
		endPointUrl = "v2/" + tenantId + "/flavors";
		resp = get(hostUrl, endPointUrl, tokenId);
		// printResponse(resp);

		Hashtable tokTable = getFlavorRef(resp, flavorId);
		String flavorRef = (String) tokTable.get("flavorRef");

		endPointUrl = "v2/" + tenantId + "/images";
		resp = get(hostUrl, endPointUrl, tokenId);

		tokTable = getImageRef(resp, imageName);
		String imageRef = (String) tokTable.get("imageRef");

		String entity = "{" + "\"server\": {" + "\"name\": \"" + serverName
				+ "\"," + "\"imageRef\": \"" + imageRef + "\","
				+ "\"flavorRef\": \"" + flavorRef + "\"," + "\"metadata\": { "
				+ "\"My Server Name\": \"ApacheTest\" " + "}}}";

		endPointUrl = "v2/" + tenantId + "/servers";
		resp = post(hostUrl, endPointUrl, entity, tokenId);
		printResponse(resp);
	}

	public static Hashtable deleteInstance(String hostIp, String tokenId,
			String tenantId, String serverName) {

		String hostUrl = "http://" + hostIp + ":8774";
		String endPointUrl = "v2/" + tenantId + "/servers";
		HttpResponse httpResponse = get(hostUrl, endPointUrl, tokenId);

		String name = null;
		String instanceId = null;
		HttpEntity entity = httpResponse.getEntity();
		byte[] buffer = new byte[1024];
		if (entity != null) {
			try {
				InputStream inputStream = entity.getContent();
				BufferedInputStream bis = new BufferedInputStream(inputStream);

				String str = "";
				int bytesRead = 0;
				while ((bytesRead = bis.read(buffer)) != -1) {
					str += new String(buffer, 0, bytesRead);
				}
				JSONParser parser = new JSONParser();
				JSONObject obj = (JSONObject) parser.parse(str);
				JSONArray instArray = (JSONArray) obj.get("servers");

				for (int i = 0; i < instArray.size(); i++) {
					JSONObject flavorObj = (JSONObject) instArray.get(i);
					name = (String) flavorObj.get("name");
					if (name.equals(serverName)) {
						instanceId = (String) flavorObj.get("id");
						delete(hostUrl, endPointUrl, instanceId, tokenId);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Hashtable getFlavorRef(HttpResponse httpResponse,
			String flavorId) {
		HttpEntity entity = httpResponse.getEntity();
		byte[] buffer = new byte[1024];
		if (entity != null) {
			try {
				InputStream inputStream = entity.getContent();
				BufferedInputStream bis = new BufferedInputStream(inputStream);

				String str = "";
				int bytesRead = 0;
				while ((bytesRead = bis.read(buffer)) != -1) {
					str += new String(buffer, 0, bytesRead);
				}

				JSONParser parser = new JSONParser();

				JSONObject obj = (JSONObject) parser.parse(str);
				JSONArray flavorArray = (JSONArray) obj.get("flavors");

				String flavorRef = null;

				for (int i = 0; i < flavorArray.size(); i++) {
					JSONObject flavorObj = (JSONObject) flavorArray.get(i);
					String flavorIdTmp = (String) flavorObj.get("id");
					if (flavorIdTmp.equals(flavorId)) {
						JSONArray linkArray = (JSONArray) flavorObj
								.get("links");
						JSONObject linkObj = (JSONObject) linkArray.get(0);
						flavorRef = (String) linkObj.get("href");
						break;
					}
				}

				Hashtable tokTable = new Hashtable();

				if (flavorRef != null) {
					tokTable.put("flavorRef", flavorRef);
				}

				return tokTable;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Hashtable getImageRef(HttpResponse httpResponse,
			String imageName) {
		HttpEntity entity = httpResponse.getEntity();
		byte[] buffer = new byte[1024];
		if (entity != null) {
			try {
				InputStream inputStream = entity.getContent();
				BufferedInputStream bis = new BufferedInputStream(inputStream);

				String str = "";
				int bytesRead = 0;
				while ((bytesRead = bis.read(buffer)) != -1) {
					str += new String(buffer, 0, bytesRead);
				}

				JSONParser parser = new JSONParser();

				JSONObject obj = (JSONObject) parser.parse(str);
				JSONArray imageArray = (JSONArray) obj.get("images");
				String imageRef = null;

				for (int i = 0; i < imageArray.size(); i++) {
					JSONObject imageObj = (JSONObject) imageArray.get(i);
					String imageIdTmp = (String) imageObj.get("name");
					if (imageIdTmp.equals(imageName)) {
						JSONArray linkArray = (JSONArray) imageObj.get("links");
						JSONObject linkObj = (JSONObject) linkArray.get(0);
						imageRef = (String) linkObj.get("href");
						break;
					}
				}

				Hashtable tokTable = new Hashtable();

				if (imageRef != null) {
					tokTable.put("imageRef", imageRef);
				}

				return tokTable;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Hashtable getTokenUsingCredentials(String hostIp,
			String tenantName, String username, String password) {
		String entity = "{" + "\"auth\": {" + "\"tenantName\": \"" + tenantName
				+ "\"," + "\"passwordCredentials\": {" + "\"username\": \""
				+ username + "\"," + "\"password\": \"" + password + "\""
				+ "}}}";

		String hostUrl = "http://" + hostIp + ":5000";
		String endPointUrl = "v2.0/tokens";
		HttpResponse resp = post(hostUrl, endPointUrl, entity, null);
		Hashtable tokTable = new Hashtable();
		if (resp != null)
			tokTable = getToken(resp);
		return tokTable;
	}

	public static Hashtable getDetails(HttpResponse httpResponse, String url1)
			throws IOException {
		String flavorRef = null;
		String id = null;
		String created;
		String updated;
		String status;
		String tenantId;
		String instanceName;
		String zone;
		String userId;
		String name;
		String flavor;
		String image;

		Dataproperties data = new Dataproperties();
		String url = data.ret_data("mysql1.connect");
		String driver = data.ret_data("mysql1.driver");
		String userName = data.ret_data("mysql1.userName");
		String password = data.ret_data("mysql1.password");

		HttpEntity entity = httpResponse.getEntity();
		byte[] buffer = new byte[1024];
		if (entity != null) {
			try {
				InputStream inputStream = entity.getContent();
				BufferedInputStream bis = new BufferedInputStream(inputStream);

				String str = "";
				int bytesRead = 0;
				while ((bytesRead = bis.read(buffer)) != -1) {
					str += new String(buffer, 0, bytesRead);
				}

				JSONParser parser = new JSONParser();
				JSONObject obj = (JSONObject) parser.parse(str);
				JSONArray flavorArray = (JSONArray) obj.get("servers");

				for (int i = 0; i < flavorArray.size(); i++) {
					JSONObject flavorObj = (JSONObject) flavorArray.get(i);
					status = (String) flavorObj.get("status");
					updated = (String) flavorObj.get("updated");
					JSONObject ip = (JSONObject) flavorObj.get("addresses");
					if (ip != null) {
						JSONArray ip1 = (JSONArray) ip.get("private");
						if (ip1 != null) {
							for (int j = 0; j < ip1.size(); j++) {
								JSONObject ip2 = (JSONObject) ip1.get(j);
								id = (String) ip2.get("addr");
								// System.out.println(id);
							}
						}
					}
					// id = (String) flavorObj.get("id");
					created = (String) flavorObj.get("created");
					tenantId = (String) flavorObj.get("tenant_id");
					instanceName = (String) flavorObj
							.get("OS-EXT-SRV-ATTR:instance_name");
					zone = (String) flavorObj
							.get("OS-EXT-AZ:availability_zone");
					userId = (String) flavorObj.get("user_id");
					name = (String) flavorObj.get("name");
					JSONObject imagedet = (JSONObject) flavorObj.get("image");
					image = (String) imagedet.get("id");
					JSONObject imagedet1 = (JSONObject) flavorObj.get("flavor");
					flavor = (String) imagedet1.get("id");

					Connection conn = null;

					try {

						Class.forName(driver).newInstance();

						conn = (Connection) DriverManager.getConnection(url,
								userName, password);

						PreparedStatement pst = conn
								.prepareStatement("SELECT * FROM instance_image where id =? and host = ?");

						pst.setString(1, image);
						pst.setString(2, url1);
						ResultSet rs1 = pst.executeQuery();

						if (rs1.next()) {
							image = rs1.getString("name");

						}
						pst.close();
						PreparedStatement pst1 = conn
								.prepareStatement("SELECT * FROM instance_flavor where id =? and host = ?");

						pst1.setString(1, flavor);
						pst1.setString(2, url1);
						ResultSet rs2 = pst1.executeQuery();

						if (rs2.next()) {
							flavor = rs2.getString("name");

						}
						// status = "Active";
						pst1.close();
						// System.out.println(flavor+url1);
						PreparedStatement ps = ((java.sql.Connection) conn)
								.prepareStatement("insert into instance_list(host,name,image,ip,flavor,status,zone,created) values (?,?,?,?,?,?,?,?)");
						ps.setString(1, url1);
						ps.setString(2, name);
						ps.setString(3, image);
						ps.setString(4, id);
						ps.setString(5, flavor);
						ps.setString(6, status);
						ps.setString(7, zone);
						ps.setString(8, created);

						ps.execute();
						ps.close();

					} catch (Exception e) {
						// System.out.println(e);
					} finally {
						try {
							conn.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							AbandonedConnectionCleanupThread.shutdown();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Hashtable getToken(HttpResponse httpResponse) {
		HttpEntity entity = httpResponse.getEntity();
		byte[] buffer = new byte[1024];
		if (entity != null) {
			try {
				InputStream inputStream = entity.getContent();
				BufferedInputStream bis = new BufferedInputStream(inputStream);

				String str = "";
				int bytesRead = 0;
				while ((bytesRead = bis.read(buffer)) != -1) {
					str += new String(buffer, 0, bytesRead);
				}

				JSONParser parser = new JSONParser();

				JSONObject obj = (JSONObject) parser.parse(str);
				JSONObject access = (JSONObject) obj.get("access");
				JSONObject token = (JSONObject) access.get("token");
				String tokenId = (String) token.get("id");
				JSONObject tenant = (JSONObject) token.get("tenant");
				String tenantId = (String) tenant.get("id");

				// String id = .get("id");

				Hashtable tokTable = new Hashtable();
				tokTable.put("tokenId", tokenId);
				tokTable.put("tenantId", tenantId);

				return tokTable;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static void Instance(String hostip) throws IOException {

		deleting(hostip);

		String entity = "{" + "\"auth\": {" + "\"tenantName\": \"admin\","
				+ "\"passwordCredentials\": {" + "\"username\": \"admin\","
				+ "\"password\": \"test1234\"" + "}}}";
		// String hostip ="52.11.10.120";
		String url = "http://" + hostip;
		String url1 = url + ":5000";
		HttpResponse resp = post(url1, "v2.0/tokens", entity, null);
		String tokenId = "";
		String tenantId = "";
		System.out.println(resp.toString());
		System.out.println(hostip);
		if (resp != null) {
			Hashtable tokTable = getToken(resp);
			tokenId = (String) tokTable.get("tokenId");
			tenantId = (String) tokTable.get("tenantId");
		} else {
			return;
		}

		String hostUrl = url + ":8774";
		String endPointUrl = "v2/" + tenantId + "/servers/detail?all_tenants=1";
		resp = get(hostUrl, endPointUrl, tokenId);
		getDetails(resp, hostip);

	}

	public static void deleting(String hostip) throws IOException {

		Dataproperties data = new Dataproperties();
		String url = data.ret_data("mysql1.connect");
		String driver = data.ret_data("mysql1.driver");
		String userName = data.ret_data("mysql1.userName");
		String password = data.ret_data("mysql1.password");
		Connection conn = null;

		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException e) {

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}

		try {
			conn = (Connection) DriverManager.getConnection(url, userName,
					password);

			System.out.println("Connection created");
			PreparedStatement pst1 = conn
					.prepareStatement("SET SQL_SAFE_UPDATES=0;");
			pst1.execute();
			PreparedStatement pst = conn
					.prepareStatement("Delete FROM instance_list where host =?");

			pst.setString(1, hostip);
			pst.executeUpdate();

			pst.close();

		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {

				e.printStackTrace();
			}
			try {
				AbandonedConnectionCleanupThread.shutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static void printResponse(HttpResponse httpResponse) {
		HttpEntity entity = httpResponse.getEntity();
		byte[] buffer = new byte[1024];
		if (entity != null) {
			try {
				InputStream inputStream = entity.getContent();
				int bytesRead = 0;
				BufferedInputStream bis = new BufferedInputStream(inputStream);
				String chunk = "";
				while ((bytesRead = bis.read(buffer)) != -1) {
					chunk = new String(buffer, 0, bytesRead);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String listing() throws IOException {

		Dataproperties data = new Dataproperties();
		String id;
		String host;
		String created;
		String image;
		String status;
		String tenantId;
		String instanceName;
		String zone;
		String userId;
		String name;
		String flavor;

		String url = data.ret_data("mysql1.connect");
		String driver = data.ret_data("mysql1.driver");
		String userName = data.ret_data("mysql1.userName");
		String password = data.ret_data("mysql1.password");
		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Connection conn = null;
		try {
			conn = (Connection) DriverManager.getConnection(url, userName,
					password);
			PreparedStatement pst = conn
					.prepareStatement("SELECT * FROM instance_list");

			ResultSet rs = pst.executeQuery();

			returnData = new StringBuffer("{\"topic\":{");
			returnData.append("\"details\":[");
			int flag = 0;
			while (rs.next()) {
				host = rs.getString("host");
				name = rs.getString("name");
				image = rs.getString("image");
				id = rs.getString("ip");
				flavor = rs.getString("flavor");
				status = rs.getString("status");
				zone = rs.getString("zone");
				created = rs.getString("created");

				if (flag == 0) {
					returnData.append("{\"id\":\"" + id + "\",\"created\":\""
							+ created + "\",\"image\":\"" + image
							+ "\",\"status\":\"" + status + "\",\"zone\":\""
							+ zone + "\",\"name\":\"" + name
							+ "\",\"flavor\":\"" + flavor + "\",\"host\":\""
							+ host + "\"}");
					flag = 1;
				} else {
					returnData.append(",{\"id\":\"" + id + "\",\"created\":\""
							+ created + "\",\"image\":\"" + image
							+ "\",\"status\":\"" + status + "\",\"zone\":\""
							+ zone + "\",\"name\":\"" + name
							+ "\",\"flavor\":\"" + flavor + "\",\"host\":\""
							+ host + "\"}");
				}
			}
			returnData.append("]}}");
			pst.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				AbandonedConnectionCleanupThread.shutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return generateJSONData();

	}

	public static String generateJSONData() {

		return returnData.toString();

	}

}
