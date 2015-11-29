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
				if(clientSocket.isConnected()){
					String line = in.readLine();
					if(line.equalsIgnoreCase("exit")) {
						System.out.println(" >>> >>> >>> ending chat");
						clientSocket.close();
						break;
						}
					System.out.println(line);
					System.out.flush();
					out.println(stdin.readLine());
					out.flush();
				}
				else {
					System.out.println("Connection to server lost");
					break;
					}
			}
			clientSocket.close();
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
