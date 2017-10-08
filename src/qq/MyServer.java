package qq;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

import uitl.MyProtocal;

public class MyServer {
	/*****************UI部分********************/
	private JFrame mainWin = new JFrame();
	private JTextArea logArea = new JTextArea(8,50);
	private final int WIDTH = 500;
	private final int HEIGHT = 350;
	/**************后台部分*********************/
	private Selector selector;
	private int PORT;
	private String IP;
	private Charset utf8 = Charset.forName("UTF-8");
	private ImageIcon icon;
	//保存Channel到QQ账号的映射
	private Map<SocketChannel,String> map = Collections.synchronizedMap(new HashMap<>());
	private int count=0;
	//每隔一段时间向客户端发送一次在线用户名单
	Timer timer = new Timer(1000*30, evt->{
		LinkedList<String> qqList = getQQList();
		if(qqList.isEmpty()) return;
		StringBuffer buff = new StringBuffer(); 
		buff.append( MyProtocal.USER_ROUND );
		for(String qq : qqList){
			buff.append(qq + MyProtocal.SPARATOR);
		}
		//删除最后一个多余的MyProtocal.SPARATOR
		buff.deleteCharAt(buff.length()-1);
		buff.append(MyProtocal.USER_ROUND);
		//将list发送给所有客户端
		for(SocketChannel sc:map.keySet()){		
			try {
				sc.write(utf8.encode(buff.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//每个3分钟在控制台打印输出在线用户名单
		if(++count==6){
			count=0;
			logArea.append("\n----------------"+LocalTime.now()+"----------------\n");
			qqList.forEach((qq) ->logArea.append(qq+"\n"));
			logArea.append("---------------------------------------------------\n");
		}
	});
	
	public MyServer(){
		initData();
		initUI();
	}
	
	private void initData(){
		Properties pps = new Properties();
		File curDir = new File(".");
		String absolute = curDir.getAbsolutePath();
		icon = new ImageIcon(absolute.substring(0,absolute.length()-1)+
				"src/qq/img/serverIcon.png");
		try {
			pps.load(new FileInputStream(new File(
					absolute.substring(0,absolute.length()-1)+"Server_cfg.ini")));
		} catch (FileNotFoundException e) {
			logArea.append("配置文件丢失，请在当前目录下提供名为cfg.ini的配置文件\n");
			logArea.append("文件内容为:\n");
			logArea.append("IP=xxx.xxx.xxx.xxx\n");
			logArea.append("PORT=xxx\n");
			logArea.append("其中IP为当前电脑的IP地址，PORT为该服务器监听的端口号\n");
		} catch (IOException e) {
			logArea.append("发生IO错误:"+e.getMessage()+"\n");
		}
		IP = pps.getProperty("ServerIP");
		PORT = Integer.parseInt(pps.getProperty("ServerPort"));
	}
	
	private void initUI(){
		logArea.setEditable(false);
		logArea.setBackground(Color.BLACK);
//		logArea.setForeground(new Color(129,82,173));
		logArea.setForeground(Color.blue);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		mainWin.setBounds((screenSize.width-WIDTH)/2, (screenSize.height - HEIGHT)/2,
				WIDTH, HEIGHT);
		mainWin.add(new JScrollPane(logArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		ImageIcon serverIcon = new ImageIcon(icon.getImage());
		mainWin.setIconImage(serverIcon.getImage());
		mainWin.setTitle("服务器  正在监听"+IP+":"+PORT);
		mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWin.setResizable(false);
		mainWin.setVisible(true);
	}
	
	
	public void start()throws IOException{
		selector = Selector.open();
		//打开一个未绑定的ServerSocketChannel
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		InetSocketAddress address = new InetSocketAddress(IP,PORT);
		//将serverChannel绑定到指定端口
		serverChannel.bind(address);
		//设置serverChannel以非阻塞方式工作
		serverChannel.configureBlocking(false);
		//将该channel注册到selector
		serverChannel.register(selector,SelectionKey.OP_ACCEPT);
		
		logArea.append("服务器已经启动,正在监听 "+IP+" : "+PORT+" 端口...\n");
		
		//每隔90秒更新一次
		timer.start();
		
		
		//当Selector上注册的所有Channel都没有需要处理的IO操作时,select()方法将被阻塞,调用该方法的线程将被阻塞
		while(selector.select()>0){
			for(SelectionKey curKey : selector.selectedKeys()){
				//从已选择的SelectionKey中除去当前操作的key
				selector.selectedKeys().remove(curKey);
				//如果该selectionKey所对应的channel包含客户端的连接请求
				if(curKey.isAcceptable()){
					SocketChannel sc = serverChannel.accept();
					sc.configureBlocking(false);
					//将该socketChannel也注册到selector上
					sc.register(selector, SelectionKey.OP_READ);
					curKey.interestOps(SelectionKey.OP_ACCEPT);
					map.put(sc,null);
				}
				//如果curKey对应的SocketChannel中有数据需要读取
				if(curKey.isReadable()){
					SocketChannel curClient = (SocketChannel)curKey.channel();
					ByteBuffer buff = ByteBuffer.allocate(1024);
					StringBuffer content= new StringBuffer();
					try{
						//开始读取数据
						//从客户端发来的消息,包括发送方QQ,文本格式,消息内容
						while(curClient.read(buff)>0){
							buff.flip();
							content.append(utf8.decode(buff)); 
						}
						curKey.interestOps(SelectionKey.OP_READ);
					//如果捕获到skey对应的Channel发生的异常,即表明该Channel对应的Client出现了问题
					//所以要从Selector中取消该skey的注册
					}catch(IOException e){
						//从在线列表中删除指定用户
						if( map.containsKey(curKey.channel()) ){
							logArea.append(map.get(curKey.channel())+"\t下线了...\t"+
									LocalTime.now()+"\n");
							map.remove(curKey.channel());
						}
						//从selector中删除指定的selectionKey
						curKey.cancel();
						if(curKey.channel()!=null){
							curKey.channel().close();
						}
					}
					
					//获取发送方的QQ号码,第一项是QQ号码
					if(content.length() > 2){
						String[] msgs = parseMsg(content.toString());
						String msgFrom = msgs[0];	//qq from
						String msgTo = msgs[8];		//qq to
//						System.out.println("Msg From:"+msgFrom);
//						System.out.println("Msg To:来自"+msgTo);
	
						if(map.get(curClient)==null){
							map.put(curClient, msgFrom);
							logArea.append(msgFrom+"\t上线了...\t"+LocalTime.now()+"\n");
						}
							
						
						//群聊消息
						if("all".equals(msgTo)){
							for(SelectionKey key:selector.keys()){
								//遍历该selector中所有的SelectionKey(其中有一个key对应了ServerSocketChannel)
								Channel channel = key.channel();
								//如果该channel是SocketChannel而不是ServerSocketChannel
								//并且不是该当前Client对应的channel,就把消息群发给他
								if(channel instanceof SocketChannel &&
										key.hashCode()!=curKey.hashCode()){
									SocketChannel dest = (SocketChannel)channel;
									dest.write( utf8.encode(content.toString()) );
								}
							}
						}else{
							//私聊消息,找出目标channel，并将消息发送到该channel
							for(SocketChannel targetChannel:map.keySet()){
								if(map.get(targetChannel).equals(msgTo)){
									targetChannel.write(utf8.encode(content.toString()));
									break;
								}
							}
						}
					}

				}
			}
		}
		//关闭serverChannel
		serverChannel.close();
		logArea.append("服务器已关闭!");
	}
	
	
	//获取所有在线的QQ名单
	private LinkedList<String> getQQList(){
		LinkedList<String> list = new LinkedList<>();
		map.forEach((channel,qq) ->{
			if(qq!=null){
				list.add(qq);
			}
		});
		return list;
	}
	
	//将收到的信息分解成字符串数组
	private String[] parseMsg(String msg){
		if(msg.trim().length()<=2) return null;
		return  msg.substring(MyProtocal.PROTOCAL_LEN-1,
				msg.length()-MyProtocal.PROTOCAL_LEN+1).
				split(MyProtocal.SPARATOR);
	}
	
	public static void main(String[] args)throws IOException{
		new MyServer().start();
	}
}

