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
	/*****************UI����********************/
	private JFrame mainWin = new JFrame();
	private JTextArea logArea = new JTextArea(8,50);
	private final int WIDTH = 500;
	private final int HEIGHT = 350;
	/**************��̨����*********************/
	private Selector selector;
	private int PORT;
	private String IP;
	private Charset utf8 = Charset.forName("UTF-8");
	private ImageIcon icon;
	//����Channel��QQ�˺ŵ�ӳ��
	private Map<SocketChannel,String> map = Collections.synchronizedMap(new HashMap<>());
	private int count=0;
	//ÿ��һ��ʱ����ͻ��˷���һ�������û�����
	Timer timer = new Timer(1000*30, evt->{
		LinkedList<String> qqList = getQQList();
		if(qqList.isEmpty()) return;
		StringBuffer buff = new StringBuffer(); 
		buff.append( MyProtocal.USER_ROUND );
		for(String qq : qqList){
			buff.append(qq + MyProtocal.SPARATOR);
		}
		//ɾ�����һ�������MyProtocal.SPARATOR
		buff.deleteCharAt(buff.length()-1);
		buff.append(MyProtocal.USER_ROUND);
		//��list���͸����пͻ���
		for(SocketChannel sc:map.keySet()){		
			try {
				sc.write(utf8.encode(buff.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//ÿ��3�����ڿ���̨��ӡ��������û�����
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
			logArea.append("�����ļ���ʧ�����ڵ�ǰĿ¼���ṩ��Ϊcfg.ini�������ļ�\n");
			logArea.append("�ļ�����Ϊ:\n");
			logArea.append("IP=xxx.xxx.xxx.xxx\n");
			logArea.append("PORT=xxx\n");
			logArea.append("����IPΪ��ǰ���Ե�IP��ַ��PORTΪ�÷����������Ķ˿ں�\n");
		} catch (IOException e) {
			logArea.append("����IO����:"+e.getMessage()+"\n");
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
		mainWin.setTitle("������  ���ڼ���"+IP+":"+PORT);
		mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWin.setResizable(false);
		mainWin.setVisible(true);
	}
	
	
	public void start()throws IOException{
		selector = Selector.open();
		//��һ��δ�󶨵�ServerSocketChannel
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		InetSocketAddress address = new InetSocketAddress(IP,PORT);
		//��serverChannel�󶨵�ָ���˿�
		serverChannel.bind(address);
		//����serverChannel�Է�������ʽ����
		serverChannel.configureBlocking(false);
		//����channelע�ᵽselector
		serverChannel.register(selector,SelectionKey.OP_ACCEPT);
		
		logArea.append("�������Ѿ�����,���ڼ��� "+IP+" : "+PORT+" �˿�...\n");
		
		//ÿ��90�����һ��
		timer.start();
		
		
		//��Selector��ע�������Channel��û����Ҫ�����IO����ʱ,select()������������,���ø÷������߳̽�������
		while(selector.select()>0){
			for(SelectionKey curKey : selector.selectedKeys()){
				//����ѡ���SelectionKey�г�ȥ��ǰ������key
				selector.selectedKeys().remove(curKey);
				//�����selectionKey����Ӧ��channel�����ͻ��˵���������
				if(curKey.isAcceptable()){
					SocketChannel sc = serverChannel.accept();
					sc.configureBlocking(false);
					//����socketChannelҲע�ᵽselector��
					sc.register(selector, SelectionKey.OP_READ);
					curKey.interestOps(SelectionKey.OP_ACCEPT);
					map.put(sc,null);
				}
				//���curKey��Ӧ��SocketChannel����������Ҫ��ȡ
				if(curKey.isReadable()){
					SocketChannel curClient = (SocketChannel)curKey.channel();
					ByteBuffer buff = ByteBuffer.allocate(1024);
					StringBuffer content= new StringBuffer();
					try{
						//��ʼ��ȡ����
						//�ӿͻ��˷�������Ϣ,�������ͷ�QQ,�ı���ʽ,��Ϣ����
						while(curClient.read(buff)>0){
							buff.flip();
							content.append(utf8.decode(buff)); 
						}
						curKey.interestOps(SelectionKey.OP_READ);
					//�������skey��Ӧ��Channel�������쳣,��������Channel��Ӧ��Client����������
					//����Ҫ��Selector��ȡ����skey��ע��
					}catch(IOException e){
						//�������б���ɾ��ָ���û�
						if( map.containsKey(curKey.channel()) ){
							logArea.append(map.get(curKey.channel())+"\t������...\t"+
									LocalTime.now()+"\n");
							map.remove(curKey.channel());
						}
						//��selector��ɾ��ָ����selectionKey
						curKey.cancel();
						if(curKey.channel()!=null){
							curKey.channel().close();
						}
					}
					
					//��ȡ���ͷ���QQ����,��һ����QQ����
					if(content.length() > 2){
						String[] msgs = parseMsg(content.toString());
						String msgFrom = msgs[0];	//qq from
						String msgTo = msgs[8];		//qq to
//						System.out.println("Msg From:"+msgFrom);
//						System.out.println("Msg To:����"+msgTo);
	
						if(map.get(curClient)==null){
							map.put(curClient, msgFrom);
							logArea.append(msgFrom+"\t������...\t"+LocalTime.now()+"\n");
						}
							
						
						//Ⱥ����Ϣ
						if("all".equals(msgTo)){
							for(SelectionKey key:selector.keys()){
								//������selector�����е�SelectionKey(������һ��key��Ӧ��ServerSocketChannel)
								Channel channel = key.channel();
								//�����channel��SocketChannel������ServerSocketChannel
								//���Ҳ��Ǹõ�ǰClient��Ӧ��channel,�Ͱ���ϢȺ������
								if(channel instanceof SocketChannel &&
										key.hashCode()!=curKey.hashCode()){
									SocketChannel dest = (SocketChannel)channel;
									dest.write( utf8.encode(content.toString()) );
								}
							}
						}else{
							//˽����Ϣ,�ҳ�Ŀ��channel��������Ϣ���͵���channel
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
		//�ر�serverChannel
		serverChannel.close();
		logArea.append("�������ѹر�!");
	}
	
	
	//��ȡ�������ߵ�QQ����
	private LinkedList<String> getQQList(){
		LinkedList<String> list = new LinkedList<>();
		map.forEach((channel,qq) ->{
			if(qq!=null){
				list.add(qq);
			}
		});
		return list;
	}
	
	//���յ�����Ϣ�ֽ���ַ�������
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

