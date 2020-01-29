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
	
	//클라이언트로부터 메시지를 전달받는 메소드
	public void receive() {
		
		//클라이언트가 접속 했을 때 쓰레드 생성 
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];	//한번에 512바이트 전달 받음
						int length= in.read(buffer);
						while(length == -1) throw new IOException();
						System.out.println("[메시지 수신 성공 ] "+ 
										socket.getRemoteSocketAddress()+": "+
										Thread.currentThread().getName());
						String message= new String(buffer, 0, length, "UTF-8");
						for(Client client: Main.clients) {
							client.send(message);  //다른 클라이언트들에게도 그대로 보내주는 것 
						}
					}
				}catch(Exception e) {
					try {
						System.out.println("[메시지 수신 오류] "
								+socket.getRemoteSocketAddress()	//메시지를 보낸 클라이언트 소켓 주소 
								+": "+Thread.currentThread());
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		
		//만들어진 쓰레드를 main의 쓰레드 풀에 전달해주는 것 
		Main.threadPool.submit(thread);
	}
	
	//클라이언트에게 메시지를 전송하는 메소드
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					//버퍼에 담긴 내용을 서버에서 클라이언트로 전송을 해주는 것 
					out.write(buffer);
					out.flush();
				}catch(Exception e) {
					try {
						System.out.println("[메시지 송신 오류]"
								+socket.getRemoteSocketAddress()
								+": "+Thread.currentThread().getName());
						//오류가 발생하면 현재 존재하는 클라이언트를 지워주는 것. (해당 클라인트가 접속이 끊긴 것이니까) 
						Main.clients.remove(Client.this);
						//오류가 생긴 클라이언트의 소켓을 닫아주는 것 
						socket.close();	
					}catch(Exception ex) {}
				}
			}
		};
		Main.threadPool.submit(thread);	
	}
}
