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

		setTitle("������");
		
		say.setEnabled(false);
		
		portText.setText("4000");
		
		start.addActionListener(this);
		say.addActionListener(this);
		FlowLayout leftLayout=new FlowLayout();
		leftLayout.setAlignment(FlowLayout.LEFT);
		
		
		JPanel portPanel=new JPanel();
		portPanel.setBorder(BorderFactory.createTitledBorder( "����������"));
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
		if(e.getSource()==start) {//������ӦStart��ť
			try {
				serverSocket=new ServerSocket(Integer.parseInt(portText.getText()));
				thread=new Thread(this);
				thread.start();//�����������߳������ӿͻ���
				editorPane.setText("Server starting��");
				start.setEnabled(false);
				say.setEnabled(true);
			}
			catch (Exception e1) {
				editorPane.setText("�˿���������");
			}
		}
		if(e.getSource()==say) {//������ӦSay��ť
			for(ServerThread x:servers) {//��ÿ���ͻ��˷�����Ϣ
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
				for(int i=0;i<servers.size();i++) {//ɾ���������ѶϿ���ServerThread�߳�
					if(servers.elementAt(i).socket.isClosed()) {
						servers.remove(i);
						System.out.print("delete one");
					}
				}
				ServerThread t=new ServerThread(serverSocket.accept(), this);//��ȡ�µĿͻ��˲�Ϊ֮����������߳�
				appendEdit("Client connected��");
				t.start();//�����������߳�
				servers.add(t);
			}
			catch (IOException e) {
				appendEdit("��һ�������߳�����ֹ");
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
	public void sendMessage(String s) {//��ͻ��˷�����Ϣ
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
	public void run() {//���տͻ��˷�������Ϣ
		while (true) {
			try {
				String rcv=in.readUTF();
				server.appendEdit(rcv);
			} 
			catch (IOException e) {
				server.appendEdit("һ���ͻ��˶Ͽ�����");
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