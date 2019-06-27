package com;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class Server {

	public static void main(String[] args) {
		WindowServer server=new WindowServer();
	}

}
class WindowServer extends JFrame implements ActionListener,Runnable{
	JTextField portText,sayText;
	JButton start,say;
	JEditorPane editorPane;
	Thread thread;
	Vector<ServerThread> servers;
	ServerSocket serverSocket;
	public WindowServer() {
		portText=new JTextField(35);
		sayText=new JTextField(35);
		start=new JButton("Start");
		say=new JButton("Say");
		editorPane=new JEditorPane();
		servers=new Vector<ServerThread>();

		setTitle("服务器");
		
		say.setEnabled(false);
		
		portText.setText("4000");
		
		start.addActionListener(this);
		say.addActionListener(this);
		FlowLayout leftLayout=new FlowLayout();
		leftLayout.setAlignment(FlowLayout.LEFT);
		
		
		JPanel portPanel=new JPanel();
		portPanel.setBorder(BorderFactory.createTitledBorder( "服务器设置"));
		portPanel.setLayout(leftLayout);
		portPanel.add(new JLabel("Port"));
		portPanel.add(portText);
		portPanel.add(start);
		this.add(portPanel,BorderLayout.NORTH);

		editorPane.setEditable(false);
		JScrollPane editScroll=new JScrollPane(editorPane);
		this.add(editScroll,BorderLayout.CENTER);


		JPanel sayPanel=new JPanel();
		sayPanel.setLayout(leftLayout);
		sayPanel.add(new JLabel("Say"));
		sayPanel.add(sayText);
		sayPanel.add(say);
		this.add(sayPanel,BorderLayout.SOUTH);
		
		this.setVisible(true);
		this.setBounds(400,200,500,400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==start) {//设置响应Start按钮
			try {
				serverSocket=new ServerSocket(Integer.parseInt(portText.getText()));
				thread=new Thread(this);
				thread.start();//启动本对象线程以连接客户端
				editorPane.setText("Server starting…");
				start.setEnabled(false);
				say.setEnabled(true);
			}
			catch (Exception e1) {
				editorPane.setText("端口输入有误");
			}
		}
		if(e.getSource()==say) {//设置响应Say按钮
			for(ServerThread x:servers) {//向每个客户端发送信息
				x.sendMessage(sayText.getText());
			}
			appendEdit(sayText.getText());
			sayText.setText("");
		}
	}
	
	public synchronized void appendEdit(String s) {
		editorPane.setText(editorPane.getText()+'\n'+s);
	}

	public void run() {
		while(true) {
			try {
				for(int i=0;i<servers.size();i++) {//删除容器中已断开的ServerThread线程
					if(servers.elementAt(i).socket.isClosed()) {
						servers.remove(i);
						System.out.print("delete one");
					}
				}
				ServerThread t=new ServerThread(serverSocket.accept(), this);//获取新的客户端并为之分配服务器线程
				appendEdit("Client connected…");
				t.start();//启动服务器线程
				servers.add(t);
			}
			catch (IOException e) {
				appendEdit("上一个服务线程已终止");
			}
		}
	}
	
}

class ServerThread extends Thread{
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	WindowServer server;
	public ServerThread(Socket socket,WindowServer server) {
		this.socket=socket;
		this.server=server;
		try {
			in=new DataInputStream(socket.getInputStream());
			out=new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException e) {
		}
	}
	public void sendMessage(String s) {//向客户端发送信息
		try {
			out.writeUTF(s);
		} 
		catch (IOException e) {
			try {
				socket.close();
			} 
			catch (IOException e1) {
			}
		}
	}
	public void run() {//接收客户端发来的信息
		while (true) {
			try {
				String rcv=in.readUTF();
				server.appendEdit(rcv);
			} 
			catch (IOException e) {
				server.appendEdit("一个客户端断开连接");
				try {
					socket.close();
				} 
				catch (IOException e1) {
				}
				return;
			}
		}
	}
}