import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	// maintain all the user names
	private static HashSet<String> names = new HashSet<String>();
	// maintain all the writers for broadcasting
	private static List<PrintWriter> writers = new ArrayList<PrintWriter>();
	private static List<Socket> sockets = new ArrayList<Socket>();
	// saves userName & password ; Format > key = userName, value = password;
	private static HashMap <String, String> credentials = new HashMap<String, String>();
	//
	private static HashMap<Socket, List<String>> socketToName = new HashMap<Socket, List<String>>();
	private static String credentialsFile = "UserCredentials.csv";
	
	public static void main(String[] args) {
		int portNumber = 9000;;
		
		if (args.length < 1) {
			System.out.println("Usage: $ java ChatServer <port number>");
			System.out.println("e.g.: $ java ChatServer 9000");
			return;
		}
		else
			portNumber = Integer.parseInt(args[0]);
			credentials.putAll(readCredentials(credentialsFile));
			
		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
			System.out.println("Server is listening on port " + portNumber);
			System.out.println("Server is waiting for clients to connect...");
			while (true) {
				new Handler(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			System.out.println("Exception when listening on port " + portNumber);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static HashMap <String, String> readCredentials(String credentialsFile) {
		File file = new File(credentialsFile);
		HashMap <String, String> credentials = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			System.out.println(line);
			while((line=br.readLine())!=null){
				System.out.println(line + " ");
				if(line.contains(",")) {
					String[] split = line.split(",");
					if(split.length == 2){
						credentials.put(split[0].trim(), split[1].trim());
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return credentials;
	}	

	private static void writeCredentials(String credentialsFile){
		File file = new File(credentialsFile);
		try {
			PrintWriter writer = new PrintWriter(file);
			writer.println("userName, password");
			for(String name: credentials.keySet()){
				writer.println(name + ", " + credentials.get(name));
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	static class Handler extends Thread {
		private Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;
		String name;
		
		@SuppressWarnings("unused")
		private Handler(){};
		
		Handler(Socket s) {
			clientSocket = s;
		}
		public void run() {
			try {
				
				if(!clientSocket.isConnected()){
					System.out.println("Connection not established with : " + clientSocket.getLocalAddress().getHostName());
					return;
				}
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				boolean match = true;
				while (true) {
					out.println("sign up with a unique name or logIn");
					name = in.readLine();
					System.out.println("Read Name : " + name);
					out.println("input password");
					String password = in.readLine();
					System.out.println("Read Password : " + password);
					if (name == null) {
						out.println("No userName given");
						out.flush();
						out.println("breaking connection");
						out.flush();
						closeALL();
						return;
						}
					synchronized(credentials) {
						if (!credentials.containsKey(name)) {
							credentials.put(name, password);
							addUser(name, password);
							if(!socketToName.containsKey(clientSocket)){
								socketToName.put(clientSocket, new ArrayList<String>());
							}
							socketToName.get(clientSocket).add(name);
							out.println("name " + name + " is accepted. Now you can chat.");
							break;
						}
						else {
							if(!password.equals(credentials.get(name))){
								match = false;
							}
							else{
								out.println(">>> >>> >>> Welcome back " + name);
							}
							break;
						}
					}
				}

				if(!match){
					out.println("password did not match for " + name);
					out.flush();
					out.println("breaking connection");
					out.flush();
					closeALL();
					return;
				}
				
				synchronized(sockets){
					if(clientSocket.isConnected()){
						sockets.add(clientSocket);
						writers.add(out);
						
					}
				}
				System.out.println(" >>> >>> >>> " + name + " has joined the chat <<< <<< <<< " + clientSocket);
				
				for(int i=0; i < sockets.size(); ){
					if(sockets.get(i).isConnected()){
						writers.get(i).println(" >>> >>> >>> " + name + " has joined the chat <<< <<< <<<" + clientSocket);
						writers.get(i).flush();
						i++;
					}
					else{
						sockets.remove(i);
						writers.remove(i);
						for(int j=0; j<sockets.size(); j++){
							if(sockets.get(j).isConnected()){
								writers.get(j).println(" <<< <<< <<< " + socketToName.get(sockets.get(i)) + " has left the chat >>> >>> >>> ");
								writers.get(j).flush();
							}
						}
					}
				}
				
				while (true) {
					String message = in.readLine();
					if (message == null)
						return;
					for (PrintWriter writer: writers) {
						writer.println(name + " : " + message);
						writer.flush();
					}
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} finally {
				if (name != null)
					names.remove(name);
				if (out != null)
					writers.remove(out);
				closeALL();
			}
		}

		private void addUser(String name2, String password) {
			File file = new File(credentialsFile);
			try {
				FileWriter fw = new FileWriter(file, true);
				fw.write(name2 + ", " + password + "\n");
				fw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void closeALL() {
			try {
				in.close();
				out.close();
				if(clientSocket.isConnected()) {
					clientSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}