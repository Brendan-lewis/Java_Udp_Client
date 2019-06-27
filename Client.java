package com;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class Client {

	public static void main(String[] args) {
		WindowClient2 win=new WindowClient2();

	}

}

class WindowClient2 extends JFrame implements ActionListener,Runnable{
	JButton connect,say;
	JTextField ipText,portText,sayText;
	JEditorPane editorPane;
	Thread thread;
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	public WindowClient2() {
		portText=new JTextField(10);
		ipText=new JTextField(10);
		sayText=new JTextField(35);
		
		setTitle("客户端");
		
		editorPane=new JEditorPane();
		
		connect=new JButton("Connect");
		say=new JButton("Say");
		say.setEnabled(false);
		
		ipText.setText("127.0.0.1");
		portText.setText("4000");
		FlowLayout leftLayout=new FlowLayout();
		leftLayout.setAlignment(FlowLayout.LEFT);
		
		JPanel addressPanel=new JPanel();
		addressPanel.setLayout(leftLayout);
		addressPanel.setBorder(BorderFactory.createTitledBorder( "客户机设置"));
		addressPanel.add(new JLabel("Server IP："));
		addressPanel.add(ipText);
		addressPanel.add(new JLabel("Server Port："));
		addressPanel.add(portText);
		addressPanel.add(connect);
		this.add(addressPanel,BorderLayout.NORTH);
		
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
		
		thread=new Thread(this);
		socket=new Socket();
		connect.addActionListener(this);
		say.addActionListener(this);
		
	}
	
	public void run() {
		String rcv;
		while (true) {//接收服务端发来的信息
			try {
				rcv=in.readUTF();
				appendEdit(rcv);
			}
			catch (IOException e) {
				appendEdit("disconnect!");
				connect.setEnabled(true);
				socket=new Socket();
				break;
			}
			
		}
		
	}
	
	public synchronized void appendEdit(String s) {
		editorPane.setText(editorPane.getText()+'\n'+s);
		
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==connect) {//设置响应Connect按钮
			try {
				if(socket.isConnected()) {
				}
				else {
					InetAddress address=InetAddress.getByName(ipText.getText());
					InetSocketAddress socketAddress=new InetSocketAddress(address,Integer.parseInt(portText.getText()));
					socket.connect(socketAddress);//连接服务端
					in=new DataInputStream(socket.getInputStream());
					out=new DataOutputStream(socket.getOutputStream());
					say.setEnabled(true);
					if(!(thread.isAlive()))
						thread=new Thread(this);
					thread.start();//启动本对象线程以接收信息
					editorPane.setText("Connect to server…");
					connect.setEnabled(false);
				}
			}
			catch (Exception e1) {
				editorPane.setText("无法连接");
				socket=new Socket();
			}
		}
		if(e.getSource()==say) {//设置Say按钮
			String sayStr=sayText.getText();
			try{
				out.writeUTF(sayStr);//向服务端发送信息
			}
			catch (IOException e2) {
				editorPane.setText(editorPane.getText()+"\n"+"无法发送信息!");
			}
			appendEdit(sayStr);
			sayText.setText("");
		}
	}
	
}
