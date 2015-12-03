import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class ChatClient {

	JFrame frame = new JFrame("Small Chat room");
	JTextField textField = new JTextField("input here", 40);
	JTextArea textArea = new JTextArea(20, 40);
	JScrollPane textScroll = new JScrollPane(textArea);
	
	PrintWriter out;
	BufferedReader in;
	
	static String serverIP;
	static int portNumber;
	
	public ChatClient() {
		textField.setEditable(false);
		textArea.setEditable(false);
		frame.getContentPane().add(textField, BorderLayout.SOUTH);
		frame.getContentPane().add(textScroll, BorderLayout.CENTER);
		frame.pack();
		
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println(textField.getText());
				textField.setText("");
			}
		});
	}
	
	private void doChating() {

		try (Socket clientSocket = new Socket(serverIP, portNumber)) {
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
			while (true) {
				if(clientSocket.isConnected()){
					String line = in.readLine();

					if (line.contains("a unique name or logIn")) {
						String name = JOptionPane.showInputDialog(frame, line);
						out.println(name);
					}
					else if (line.contains("password did not match")) {
						JOptionPane.showMessageDialog(frame, line);
						clientSocket.close();
						System.exit(1);
					}
					else if (line.equals("input password")) {
						String password = JOptionPane.showInputDialog(frame, line);
						out.println(password);
					}
					else if (line.contains("you can chat") || line.contains("Welcome back")) {
						textField.setEditable(true);
						textArea.append(line + "\n");
					}
					else
						textArea.append(line + "\n");
				
				/*
					if(line.equalsIgnoreCase("exit")) {
						System.out.println(" >>> >>> >>> ending chat");
						clientSocket.close();
						break;
					}
					System.out.println(line);

					System.out.flush();
					out.println(stdin.readLine());
					out.flush();
				*/
				}
				else {
					textArea.append("Connection to server lost\n");
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
	
	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("Usage: java ChatClient [ServerIP] [Port Number=9000]");
			return;
		}

		serverIP = args[0];
		portNumber = Integer.parseInt(args[1]);
		
		ChatClient client = new ChatClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		client.doChating();
	}
	
}
