package qq;

import java.awt.Color;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import uitl.MyProtocal;
import uitl.MySQL;
import uitl.UserBean;

public class MyClient {
	//ChatFrame中的JTextPane中的doc
	private StyledDocument curDoc ;
	//在线QQ账号列表
	private HashSet<String> onlineList;
	//所有用户列表,用户显示聊天消息时给出对应的用户名或昵称
	private Vector<UserBean> userList ;
	//指向ChatFrame中的JList,用于接受到私聊信息是选择指定项
	JList<UserBean> friendList;
	private Selector selector;
	private SocketChannel sc ;
	private int PORT = MySQL.getServerPort();
	private String IP= MySQL.getServerIP();
	private Charset utf8 = Charset.forName("UTF-8");
	
	//用于设置用户名和时间的属性集
	private SimpleAttributeSet timeNameAttr = new SimpleAttributeSet();
	//用于设置消息的属性集
	private SimpleAttributeSet msgAttr = new SimpleAttributeSet();
	//保存所有doc,用以实现私聊
	private HashMap<String,StyledDocument> docs;
	//获取msgPanel的滚动条,是滚动条始终显示在最下方
	JScrollBar sBar;
	private Color nameTimeColor = new Color(220,168,210,200);
	
	public MyClient(StyledDocument doc,Vector<UserBean> users,JList<UserBean> list,
				HashSet<String> onlineList,JScrollBar sBar,HashMap<String,StyledDocument> docs){
		this.curDoc = doc;
		this.docs = docs;
		userList = users;	//Vector<UserBean>
		friendList = list;	//JList<UserBean>
		this.onlineList = onlineList;	//HashSet<UserBean>
		this.sBar = sBar;	//ScrollBar
		//设置时间文本格式
		StyleConstants.setFontFamily(timeNameAttr, "汉仪蝶语体简");
		StyleConstants.setFontSize(timeNameAttr, 12);
		StyleConstants.setForeground(timeNameAttr, nameTimeColor);
		
		
		try {
			selector = Selector.open();
			sc = SocketChannel.open(new InetSocketAddress(IP,PORT));
			//设置sc以非阻塞式工作
			sc.configureBlocking(false);
			sc.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//启动接收服务器数据的线程
		new ClientThread().start();
	}
	
	
	
	public void addMsgToDoc(String msg,int align){
		//分割消息
		String[] contents = parseMsg(msg);
		String qq = contents[0];
		String font = contents[1];
		int size = Integer.valueOf(contents[2]);
		boolean italic = Boolean.valueOf(contents[3]);
		boolean bold = Boolean.valueOf(contents[4]);
		boolean underline = Boolean.valueOf(contents[5]);
		String[] rgb = contents[6].split(","); 
		Color color = new Color(Integer.valueOf(rgb[0]),Integer.valueOf(rgb[1]),
				Integer.valueOf(rgb[2]));
		String target = contents[8];//target qq
		
		LocalDateTime dt = LocalDateTime.now();
		String datetime = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
		
		StyleConstants.setAlignment(msgAttr, align);
		StyleConstants.setFontFamily(msgAttr, font);
		StyleConstants.setFontSize(msgAttr, size);
		StyleConstants.setItalic(msgAttr, italic);
		StyleConstants.setBold(msgAttr, bold);
		StyleConstants.setUnderline(msgAttr, underline);
		StyleConstants.setForeground(msgAttr, color);
		
		//从服务器发来的消息
		//插入昵称或QQ
		//如果消息是左对齐,先插入用户名,在插入时间
		if(align==StyleConstants.ALIGN_LEFT){
			StyledDocument doc=null;
			//收到的是群聊消息，插入群聊doc中
			if("all".equals(target)){
				doc = getDocByQQ("all");
			}else{
				//私聊消息，插入消息发送方对应的doc中
				 doc = getDocByQQ(qq);
				if(doc==null){
					doc = new DefaultStyledDocument();
					docs.put(qq, doc);
				}
				//如果收到的不是群聊消息，就选中给自己发消息的另一方
				setSelectedListItem(qq);
			}
			StyleConstants.setAlignment(timeNameAttr, align);
			insert(getNameByQQ(qq)+" "+datetime,timeNameAttr,doc);
			insert(contents[7]+"\n",msgAttr,doc);
			
		}else{
			StyleConstants.setAlignment(timeNameAttr, align);
			insert(datetime+" "+getNameByQQ(qq),timeNameAttr,curDoc);
			insert(contents[7]+"\n",msgAttr,curDoc);
		}
		
		//将滚动条滚动到最下方
		sBar.setValue(sBar.getMaximum());
	}
	
	//收到私聊消息后使私聊对象处于选中状态
	private void setSelectedListItem(String target){
		System.out.println("selected List item:"+target);
		int idx=0;
		for(UserBean ub :userList){
			if(ub.getQQ().equals(target)){
				friendList.setSelectedIndex(idx);
				break;
			}
			++idx;
		}
	}
	
	private StyledDocument getDocByQQ(String qq){
		for(String key:docs.keySet()){
			if(key.equals(qq)){
				return docs.get(key);
			}
		}
		return null;
	}
	
	//更新在线用户列表
	private void updateOnlineList(String msg){
		String[] qqList = parseMsg(msg);
		onlineList.clear();
		onlineList.add("all");
		for(int i=0;i<qqList.length;++i){
			onlineList.add(qqList[i]);
		}
	}
	
	//将收到的信息分解成字符串数组
	private String[] parseMsg(String msg){
		if(msg.trim().length()<=2) return null;
		return  msg.substring(MyProtocal.PROTOCAL_LEN-1,
				msg.length()-MyProtocal.PROTOCAL_LEN+1).
				split(MyProtocal.SPARATOR);
	}
	
	//把str插入到doc中,并设置为指定的格式
	private void insert(String str,SimpleAttributeSet attr,StyledDocument doc){
		int lastIndex = doc.getLength();
		try {
			doc.insertString(lastIndex, str+"\n", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		doc.setParagraphAttributes(lastIndex, doc.getLength()-lastIndex, attr, true);
	}
	
	//用qq号查找用户昵称,如果昵称为空,直接返回qq号
	private String getNameByQQ(String qq){
		for(int i=0;i<userList.size();++i){
			if( qq.equals( userList.get(i).getQQ() ) ){
				if(userList.get(i).getNickname()!=null)
					return userList.get(i).getNickname();
				else
					return qq;
			}
		}
		return qq;
	}
	//向服务器发送数据
	public void sendMsg(String msg){
		try{
			sc.write(ByteBuffer.wrap(msg.getBytes("UTF-8")));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public StyledDocument getDocument(){
		return curDoc;
	}
	//设置消息要插入到的目标document
	public void setDocument(StyledDocument doc){
		this.curDoc = doc;
	}
/*	
	//设置当前doc
	private void setCurDoc(String qq){
		for(String key:docs.keySet()){
			if(key.equals(qq)){
				curDoc = docs.get(key);
				if(curDoc==null){
					curDoc = new DefaultStyledDocument();
					docs.put(key, curDoc);
				}
			}
		}
	}
*/
	//接收服务器数据的线程
	class ClientThread extends Thread{
		public void run(){
			try{
				while(selector.select()>0){
					for(SelectionKey curKey:selector.selectedKeys()){
						selector.selectedKeys().remove(curKey);
						
						if( curKey.isValid() && curKey.isReadable() ){
							SocketChannel sc =(SocketChannel)curKey.channel();
							ByteBuffer buff = ByteBuffer.allocate(1024);
							StringBuffer sb= new StringBuffer();
							try{
								while(sc.read(buff) > 0){
									buff.flip();
									sb.append(utf8.decode(buff));
								}
								curKey.interestOps(SelectionKey.OP_READ);
								String msg = sb.toString();
								//接收到的是在线用户名单
								if(msg.startsWith(MyProtocal.USER_ROUND ) &&
									msg.endsWith(MyProtocal.USER_ROUND) ){
									updateOnlineList(msg);
//									System.out.println("updateing online list...");
								}else{
									//来自其他用户的聊天信息显示在消息面板的左边
									addMsgToDoc(msg,StyleConstants.ALIGN_LEFT);
								}
								
							}catch(IOException e){
								sc.socket().close();
								sc.close();
								curKey.cancel();
							}
						}
					}
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}
