package entertech.websocket;
import java.io.IOException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.*;  
import javax.websocket.server.ServerEndpoint; 
import java.nio.ByteBuffer;
import java.net.URI;
@ServerEndpoint("/ws")
public class WSServer 
{
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。  
    private static int onlineCount = 0;  

    private static ConcurrentHashMap<Session, WSServer> ssMap= new ConcurrentHashMap<Session, WSServer>();
    
    //与某个客户端的连接会话，需要通过它来给客户端发送数据  
    private Session session;  
    
    private WebSocketClient webSocketClient = new WebSocketClient(URI.create("ws://47.103.77.211:8000/ws/algorithm/v0.1/"),new Draft_6455(),null,10000){

		@Override
		public void onOpen(ServerHandshake handshakedata) {
			   System.out.println("情感云连接成功！");  
				try {
					sendMessage("情感云连接成功！");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		@Override
		public void onMessage(String message) {
			
		}

		@Override
		public void onMessage(ByteBuffer message) {
			   System.out.println("从情感云接受数据："+message);  
			try {
				sendMessage(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		@Override
		public void onClose(int code, String reason, boolean remote) {
			   System.out.println("情感云断开连接："+code+":"+reason);  
		}

		@Override
		public void onError(Exception ex) {
			 System.out.println("情感云连接错误："+ex.toString());  
		}
    	
    };

    /** 
     * 连接建立成功调用的方法 
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据 
     */  
    @OnOpen  
    public void onOpen(Session session){  
        this.session = session;  
        ssMap.put(session, this);
        webSocketClient.connect();
        addOnlineCount();           //在线数加1  
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());  
    }  

    /** 
     * 连接关闭调用的方法 
     */  
    @OnClose  
    public void onClose(){  
        ssMap.remove(this.session);
        webSocketClient.close();
        subOnlineCount();           //在线数减1  
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());  
    }  

    /** 
     * 收到客户端消息后调用的方法 
     * @param message 客户端发送过来的消息 
     * @param session 可选的参数 
     */  
    @OnMessage  
    public void onMessage(String message, Session session) {  
        System.out.println("来自客户端的消息:" + message);  
        WSServer tmp = ssMap.get(session);
        try {
            tmp.sendMessage(message);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }  
    
    @OnMessage  
    public void onByteMessage(byte[] message, boolean last, Session session) {
        try {
        	System.out.println("从客户端接受数据："+message);  
        	this.webSocketClient.send(message);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }  
    
    
    /** 
     * 发生错误时调用 
     * @param session 
     * @param error 
     */  
    @OnError  
    public void onError(Session session, Throwable error){  
        System.out.println("发生错误");  
        error.printStackTrace();  
    }  

    /** 
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。 
     * @param message 
     * @throws IOException 
     */  
    public void sendMessage(String message) throws IOException{  
        this.session.getBasicRemote().sendText(message);  
        //this.session.getAsyncRemote().sendText(message);  
    }  

    public void sendMessage(ByteBuffer message) throws IOException{  
        this.session.getBasicRemote().sendBinary(message);  
        //this.session.getAsyncRemote().sendText(message);  
    }  

    public static synchronized int getOnlineCount() {  
        return onlineCount;  
    }  

    public static synchronized void addOnlineCount() {  
        WSServer.onlineCount++;  
    }  

    public static synchronized void subOnlineCount() {  
        WSServer.onlineCount--;  
    }
}