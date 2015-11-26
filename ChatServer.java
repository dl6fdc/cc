import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	// maintain all the user names
	private static HashSet<String> names = new HashSet<String>();
	// maintain all the writers for broadcasting
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

	public static void main(String[] args) {
		int portNumber;
		
		if (args.length > 1) {
			System.out.println("Usage: java ChatServer <port number=9000>");
			System.exit(1);
		}
		
		if (args.length == 1)
			portNumber = Integer.parseInt(args[0]);
		else
			portNumber = 9000;
		
		
		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
			System.out.println("Server is listening on port " + portNumber);
			
			while (true) {
				new Handler(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			System.out.println("Exception when listening on port " + portNumber);
			System.out.println(e.getMessage());
		}
 
	}
	
	private static class Handler extends Thread {
		private Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;
		String name;
		
		Handler(Socket s) {
			clientSocket = s;
		}
		
		public void run() {
			try {
			
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);
			
				while (true) {
					out.println("sign up a unique name");
					name = in.readLine();
					//System.out.println(name);
					if (name == null)
						return;
					synchronized(names) {
						if (!names.contains(name)) {
							names.add(name);
							break;
						}
					}
				}
				
				out.println("name " + name + " is accepted. Now you can talk.");
				writers.add(out);
				
				while (true) {
					String message = in.readLine();
					if (message == null)
						return;
					for (PrintWriter writer: writers) {
						writer.println(name + ": " + message);
					}
				
				}
			
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} finally {
				if (name != null)
					names.remove(name);
				if (out != null)
					writers.remove(out);
				try {
					clientSocket.close();
				} catch (IOException e) {
				}
			}
		}	
	}

}

