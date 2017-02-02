package Users;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

public class User extends Frame {
	public static String path = (System.getProperty("user.dir")).replace("\\", "/");
	public static String local_address;
	public static int count = 0;
	public static int threadCount = 0;
	public static int timeout = 10000; // setTimeout 10S
	public static String target_http = "https://www.google.com.tw/";
	public static String proxies_file = "[gatherproxy.com]proxies_2017_01_12.txt";

	public static void main(String[] args) {
		User us = new User();
		print_info();
		us.readFile();
		System.out.println("-----------finish-----------");
	}

	public User() {
		init();
	}

	public static void print_info() {
		System.out.println("file_path:" + path);
		System.out.println("local_address:" + local_address);
	}

	public void readFile() {
		Scanner linReader;
		try {
			linReader = new Scanner(new File(path + "/data/" + proxies_file));
			while (linReader.hasNext()) {
				String line = linReader.nextLine();
				String[] ip_port = line.split(":");
				String ip = ip_port[0];
				String port = ip_port[1];

				// while (threadCount >= 20);
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, Integer.parseInt(port)));
				new ProxyThread(proxy).start();
				System.out.println(line);
				System.out.println("connect......threadCount:" + threadCount);
			}
			linReader.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

	}

	private String getHtml(String address, Proxy proxy) {
		StringBuffer html = new StringBuffer();
		String result = null;
		String output_file;
		HttpURLConnection connection;
		try {
			URL url = new URL(address);

			if (proxy == null) {
				connection = (HttpURLConnection) url.openConnection();
			} else {
				connection = (HttpURLConnection) url.openConnection(proxy);
			}
			//
			connection.setConnectTimeout(timeout); // setTimeout
			connection.setReadTimeout(timeout);
			// connection.setUseCaches(false);

			connection.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 7.0; NT 5.1; GTB5; .NET CLR 2.0.50727; CIBA)");

			if (connection.getResponseCode() == 302) {
				getHtml(new URL(connection.getHeaderField("location")).toString(), proxy);
			}

			if (connection.getResponseCode() == 200) {
				printResult(true);
			}

			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());

			try {
				String inputLine;
				byte[] buf = new byte[4096];
				int bytesRead = 0;
				while (bytesRead >= 0) {
					inputLine = new String(buf, 0, bytesRead, "UTF-8");
					html.append(inputLine);
					bytesRead = in.read(buf);
					inputLine = null;
				}
				buf = null;
			} finally {
				// write ip and port to output file
				output_file = (InetAddress.getByName(url.getHost()).getHostAddress());
				output_file(output_file);
				in.close();
				connection = null;
				url = null;
			}

			result = html.toString();

		} catch (Exception e) {
			printResult(false);
			return null;

		} finally {
			html = null;
		}
		return result;
	}

	private synchronized void printResult(boolean result) {
		if (result == true) {
			count++;
			System.out.println("--------connect success---------" + "count:" + count);
		} else {
			System.out.println("--------connect fail---------");
		}
	}

	private void output_file(String s) {
		if (s != null) {
			// record can be used proxies to txt
			File log = new File("log.txt");
			try {
				if (log.exists() == false) {
					System.out.println("We had to make a new file.");
					log.createNewFile();
				}
				s = s.substring(1, s.length());
				PrintWriter out = new PrintWriter(new FileWriter(log, true));
				out.println(s);
				out.close();
			} catch (IOException e) {
				System.out.println("COULD NOT LOG!!");
			}
		}
	}

	public void init() {
		URL url;
		String host;
		InetAddress address;
		try {

			url = new URL("http://yahoo.com");
			host = url.getHost();
			address = InetAddress.getByName(host);
			local_address = address.getHostAddress();

		} catch (UnknownHostException | MalformedURLException e) {
			e.printStackTrace();
		}
	}

	class ProxyThread extends Thread {
		Proxy proxy;

		public ProxyThread(Proxy proxy) {
			threadCount++;
			this.proxy = proxy;
		}

		@Override
		public void run() {
			getHtml(target_http, proxy);
			threadCount--;
		}
	}
}

class AdapterDemo extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
}