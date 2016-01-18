package com.mtaas.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mtaas.Model.InstanceHandler;
import com.mtaas.Model.IntanceDetails;
import com.mtaas.Utilities.Dataproperties;

@WebServlet("/data")
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String LAUNCH = "launch";
	private static final String DELETE = "delete";
	private static final String LIST = "list";

	public Controller() {
		super();
	}

	@SuppressWarnings("static-access")
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		String type = request.getParameter("type");
		String action = request.getParameter("action");
		Dataproperties data = new Dataproperties();
		String tenantId = null;
		String tenantName, username, password, hostIp = null;
		tenantName = data.ret_data("stack.tenantName");
		username = data.ret_data("stack.username");
		password = data.ret_data("stack.password");

		if (type.equals("instance")) {
			String imageName = null, instanceName = null, countStr = null, flavorId = null;
			String regionName = (request.getParameter("regionName") != null) ? request
					.getParameter("regionName") : "US";
			IntanceDetails inst = new IntanceDetails();
			Hashtable tokTable = new Hashtable();
			String tokenId = null;

			if (!(action.startsWith("em_") || action.startsWith("phone_"))) {
				hostIp = InstanceHandler.getHostIp(regionName);
				if ((hostIp == null) || hostIp.equals(""))
					hostIp = data.ret_data("stack.hostIp");
				tokTable = inst.getTokenUsingCredentials(hostIp, tenantName,
						username, password);
				tenantId = (String) tokTable.get("tenantId");
				tokenId = (String) tokTable.get("tokenId");
				imageName = (request.getParameter("imageName") != null) ? request
						.getParameter("imageName") : data
						.ret_data("stack.imageName");
				instanceName = (request.getParameter("instanceName") != null) ? request
						.getParameter("instanceName") : data
						.ret_data("stack.serverName");
				countStr = (request.getParameter("count") != null) ? request
						.getParameter("count") : "1";
				flavorId = (request.getParameter("flavor") != null) ? request
						.getParameter("flavor") : data
						.ret_data("stack.flavorId");
			}

			if (action.equals(LAUNCH)) {
				int count = Integer.parseInt(countStr);
				String algo = request.getParameter("algo");

				for (int i = 0; i < count; i++) {
					if (!algo.equals("NONE")) {
						hostIp = InstanceHandler.getHostIpUsingAlgo(algo);
						if (hostIp == null) {
							InstanceHandler.getHostIp(regionName);
							tokTable = inst.getTokenUsingCredentials(hostIp,
									tenantName, username, password);
							tokenId = (String) tokTable.get("tokenId");
							tenantId = (String) tokTable.get("tenantId");
						}
					} else
						hostIp = InstanceHandler.getHostIp(regionName);
					String instNameStr = instanceName + "-" + String.valueOf(i);
					inst.createInstance(hostIp, tokenId, tenantId, flavorId,
							imageName, instNameStr);
				}
				ArrayList<String> hostIps = InstanceHandler.getAllHostIps();
				Iterator<String> itr = hostIps.iterator();
				while (itr.hasNext()) {
					String hostIpStr = (String) itr.next();
					inst.Instance(hostIpStr);
				}
			}

			if (action.equals(DELETE)) {
				hostIp = (request.getParameter("host") != null) ? request
						.getParameter("host") : hostIp;
				inst.deleteInstance(hostIp, tokenId, tenantId, instanceName);

				ArrayList<String> hostIps = InstanceHandler.getAllHostIps();
				Iterator<String> itr = hostIps.iterator();
				while (itr.hasNext()) {
					String hostIpStr = (String) itr.next();
					inst.Instance(hostIpStr);
				}
			}

			if (action.equals(LIST)) {
				inst.listing();
			}

		}

	}
}
