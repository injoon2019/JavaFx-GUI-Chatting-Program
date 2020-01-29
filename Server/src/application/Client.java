package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


//Functions that is necessary to connect server and client
public class Client {

	Socket socket;
	
	public Client(Socket socket) {
		this.socket= socket;
		receive();
	}
	
	//Ŭ���̾�Ʈ�κ��� �޽����� ���޹޴� �޼ҵ�
	public void receive() {
		
		//Ŭ���̾�Ʈ�� ���� ���� �� ������ ���� 
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];	//�ѹ��� 512����Ʈ ���� ����
						int length= in.read(buffer);
						while(length == -1) throw new IOException();
						System.out.println("[�޽��� ���� ���� ] "+ 
										socket.getRemoteSocketAddress()+": "+
										Thread.currentThread().getName());
						String message= new String(buffer, 0, length, "UTF-8");
						for(Client client: Main.clients) {
							client.send(message);  //�ٸ� Ŭ���̾�Ʈ�鿡�Ե� �״�� �����ִ� �� 
						}
					}
				}catch(Exception e) {
					try {
						System.out.println("[�޽��� ���� ����] "
								+socket.getRemoteSocketAddress()	//�޽����� ���� Ŭ���̾�Ʈ ���� �ּ� 
								+": "+Thread.currentThread());
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		
		//������� �����带 main�� ������ Ǯ�� �������ִ� �� 
		Main.threadPool.submit(thread);
	}
	
	//Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼ҵ�
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					//���ۿ� ��� ������ �������� Ŭ���̾�Ʈ�� ������ ���ִ� �� 
					out.write(buffer);
					out.flush();
				}catch(Exception e) {
					try {
						System.out.println("[�޽��� �۽� ����]"
								+socket.getRemoteSocketAddress()
								+": "+Thread.currentThread().getName());
						//������ �߻��ϸ� ���� �����ϴ� Ŭ���̾�Ʈ�� �����ִ� ��. (�ش� Ŭ����Ʈ�� ������ ���� ���̴ϱ�) 
						Main.clients.remove(Client.this);
						//������ ���� Ŭ���̾�Ʈ�� ������ �ݾ��ִ� �� 
						socket.close();	
					}catch(Exception ex) {}
				}
			}
		};
		Main.threadPool.submit(thread);	
	}
}
