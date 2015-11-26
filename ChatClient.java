import java.io.*;
import java.net.*;

public class ChatClient {

	public static void main(String[] args) {
	
		if (args.length < 2) {
			System.out.println("Usage: java ChatClient [ServerIP] [Port Number=9000]");
			return;
		}
		
		String serverIP = args[0];
		int portNumber = Integer.parseInt(args[1]);
		
		try (
			Socket clientSocket = new Socket(serverIP, portNumber);
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		) {
			while (true) {
				System.out.println(in.readLine());
				out.println(stdin.readLine());
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about server " + serverIP);
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.err.println("Couldn't connect to " + serverIP);
			e.printStackTrace();
			return;
		} 
	}

}

