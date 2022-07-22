import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import jsonModifyer.createJson;
import SearchTools.wordSearcher;

public class ServerThread extends Thread {

	private Socket s;
	private String terms, description;
	private Search search = new Search();
	private wordSearcher ws = new wordSearcher();
	private createJson json = new createJson();

	// takes the socket which the server accepted
	public ServerThread(Socket s) {
		this.s = s;
	}

	@Override
	public void run() {

		System.out.println("thread:" + this.getName() + " started...");

		if (s.isConnected()) {

			try {
				// get input and output stream
				OutputStream out = s.getOutputStream();
				BufferedReader buffIn = new BufferedReader(new InputStreamReader(s.getInputStream()));

				// read the requst from the client
				StringBuilder request = new StringBuilder();
				String line = buffIn.readLine();

				// checks for null otherwise we can get Exceptions
				if (line == null)
					return;

				// if the request has options in it its a preflight request and should responed
				// in that mather
				if (ws.hasWordInIt(line, "options")) {
					sendPreflightRes(out);
				} else {
					// gets the params and sets the search terms values as such
					getParams(line);

					// loop to build the full request
					while (!line.isEmpty()) {

						request.append(line + "\r\n");
						line = buffIn.readLine();
						// checks for the user agent which we need to trick google to think the server
						// not a bot/spammer
						if (line.length() > 10)
							if (line.substring(0, 10).toLowerCase().trim().equals("user-agent")) {
								search.setUserAgent(line.substring(11));
								// System.out.println(line.substring(11));
							}
					}
					// if the user agent or search terms are empty responde with 500 code
					if (search.getUserAgent() == null || search.getTerms() == null) {

						// System.err.println("error in GET request or incomplete request: " + request);
						sendResponse(out, "<html><body><h1>server error</h1></body></html>", 500);

					} else {
						// start the search
						search.start();
						// get the results
						String results = search.getResults();
						// for building the json response
						String[] val = new String[] { search.getOriginality(), "0", "0" };
						String[] name = new String[] { "org", "prod", "pur" };
						// send response and build a json responde from the result values
						sendResponse(out, json.StringToJson(val, "results", name), 200);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
	}

	public void sendResponse(OutputStream out, String message, int resCode) {

		byte[] rawResponse;
		StringBuilder response = new StringBuilder();

		// response header |
		// v
		response.append("HTTP/1.1 " + resCode + " " + "\r\n");
		response.append("Date: " + (new java.util.Date()).toString() + " \r\n");
		response.append("Server: " + System.getProperty("os.name") + " jvm "
				+ System.getProperty("java.runtime.version") + " \r\n");
		response.append("Content-Type: application/json" + "\r\n");
		response.append("Access-Control-Allow-Origin: *" + " \r\n");
		response.append("Connection: keep-alive" + " \r\n");
		// end of header
		response.append("\r\n");
		// start of response data/body
		response.append(message);
		try {
			// turn the response to bytes so that it can be sent
			rawResponse = response.toString().getBytes("UTF-8");
			out.write(rawResponse);

			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendPreflightRes(OutputStream out) {

		byte[] rawResponse;
		StringBuilder response = new StringBuilder();

		// preflight response header |
		//                           v
		response.append("HTTP/1.1 204 No Content" + " \r\n");
		response.append("Date: " + (new java.util.Date()).toString() + " \r\n");
		response.append("Server: " + System.getProperty("os.name") + " jvm "
				+ System.getProperty("java.runtime.version") + " \r\n");
		response.append("Allow: OPTIONS, GET" + " \r\n");
		response.append("Access-Control-Allow-Origin: *" + " \r\n");
		response.append("Access-Control-Allow-Methods: GET,OPTIONS" + " \r\n");
		response.append("Connection: keep-alive " + "\r\n");
		response.append("Keep-Alive: timeout=2, max=100" + " \r\n");
		response.append("Access-Control-Max-Age: 86400" + " \r\n");
		response.append("Vary: Accept-Encoding, Origin" + " \r\n");
		// end of header
		response.append("\r\n");

		try {
			// turn the response to bytes so that it can be sent
			rawResponse = response.toString().getBytes("UTF-8");
			out.write(rawResponse);
			out.flush();
			out.close();
			s.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getParams(String line) {

		// split the string between every space
		String[] splitted = line.split("\\s+");

		// ? is the start of query params
		if (splitted[1].charAt(1) == '?') {

			// removes the '/?' from the start
			splitted[1] = splitted[1].substring(2, splitted[1].length());

			// split if the params have multible querys
			String[] temp = splitted[1].split("&");

			for (int j = 0; j < temp.length; j++) {

				String[] params;

				params = temp[j].split("=");

				if (params[0].toLowerCase().equals("search")) {

					search.setTerms(params[1]);
				}
			}
		}
	}
}
