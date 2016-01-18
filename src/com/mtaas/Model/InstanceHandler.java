package com.mtaas.Model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import com.mtaas.Utilities.Dataproperties;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

public class InstanceHandler {

	private static int roundRobinInstIndex = 0;
	
	public static String getHostIp(String regionName) throws IOException {

		Dataproperties data = new Dataproperties();
		String url = data.ret_data("mysql1.connect");
		String driver = data.ret_data("mysql1.driver");	
		String userName = data.ret_data("mysql1.userName");
		String password = data.ret_data("mysql1.password");

		String returnData = null;
		Connection conn = null;

		try {
			Class.forName(driver).newInstance();

			conn = (Connection) DriverManager
					.getConnection(url,userName,password);
			
			conn = (Connection) DriverManager.getConnection(url,
					userName, password);
			PreparedStatement pst = conn
					.prepareStatement("select ip from region where regionName=?");
			pst.setString(1, regionName);
			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				returnData = rs.getString(1);
			}
			pst.close();

		} catch (Exception e) {
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

		return returnData;
	}
	
	public static ArrayList<String> getAllHostIps() throws IOException {
		
		Dataproperties data = new Dataproperties();
		String url = data.ret_data("mysql1.connect");
		String driver = data.ret_data("mysql1.driver");	
		String userName = data.ret_data("mysql1.userName");
		String password = data.ret_data("mysql1.password");
		
		ArrayList<String> hostIps = new ArrayList<String>();

		Connection conn = null;

		try {
			Class.forName(driver).newInstance();

			conn = (Connection) DriverManager
					.getConnection(url,userName,password);

			conn = (Connection) DriverManager.getConnection(url,
					userName, password);
			PreparedStatement pst = conn
					.prepareStatement("select distinct ip from region");

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				String hostIp = rs.getString(1);
				hostIps.add(hostIp);

			}
			pst.close();

		} catch (Exception e) {
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

		return hostIps;
	}
	
	public static String getAllHostIpsOfInstances() throws IOException {

		Dataproperties data = new Dataproperties();
		String url = data.ret_data("mysql1.connect");
		String driver = data.ret_data("mysql1.driver");	
		String userName = data.ret_data("mysql1.userName");
		String password = data.ret_data("mysql1.password");
		
		String hostIps = "";

		Connection conn = null;

		try {
			Class.forName(driver).newInstance();

			conn = (Connection) DriverManager
					.getConnection(url,userName,password);

			conn = (Connection) DriverManager.getConnection(url,
					userName, password);
			PreparedStatement pst = conn
					.prepareStatement("select region.regionName, count(instance_list.host) from instance_list inner join region on instance_list.host=region.ip group by instance_list.host");

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				hostIps += rs.getString(1) +":"+ rs.getString(2)+",";

			}
			pst.close();

		} catch (Exception e) {
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

		return hostIps;
	}
	
	public static String getHostIp(int index) throws IOException {

		Dataproperties data = new Dataproperties();
		String url = data.ret_data("mysql1.connect");
		String driver = data.ret_data("mysql1.driver");	
		String userName = data.ret_data("mysql1.userName");
		String password = data.ret_data("mysql1.password");

		Connection conn = null;

		String hostIp = null;
		
		try {
			Class.forName(driver).newInstance();

			conn = (Connection) DriverManager
					.getConnection(url,userName,password);

			conn = (Connection) DriverManager.getConnection(url,
					userName, password);
			PreparedStatement pst = conn
					.prepareStatement("select distinct ip from region");

			ResultSet rs = pst.executeQuery();
			
			int i = 1;
			while (rs.next()) {
				if(i == index)
					hostIp = rs.getString("ip");
				++i;
			}

			
			pst.close();

		} catch (Exception e) {
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

		return hostIp;
	}
	
	public static String getHostIpUsingAlgo(String algo)
	{
		String hostIp = null;
		int instIndex = 0;
		if(algo.equals("HONEYBEE"))
		{
			instIndex = randInt(1, 4);

		}
		else if(algo.equals("TOKENRING"))
		{
			++roundRobinInstIndex;
			if(roundRobinInstIndex >4)
				roundRobinInstIndex = 1;
			instIndex = roundRobinInstIndex;
		}

		try {
			hostIp = getHostIp(instIndex);
			} catch (IOException e) {
			e.printStackTrace();
		}
		return hostIp;
	}

	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
	

		
}
