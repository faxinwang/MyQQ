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
	//ChatFrame�е�JTextPane�е�doc
	private StyledDocument curDoc ;
	//����QQ�˺��б�
	private HashSet<String> onlineList;
	//�����û��б�,�û���ʾ������Ϣʱ������Ӧ���û������ǳ�
	private Vector<UserBean> userList ;
	//ָ��ChatFrame�е�JList,���ڽ��ܵ�˽����Ϣ��ѡ��ָ����
	JList<UserBean> friendList;
	private Selector selector;
	private SocketChannel sc ;
	private int PORT = MySQL.getServerPort();
	private String IP= MySQL.getServerIP();
	private Charset utf8 = Charset.forName("UTF-8");
	
	//���������û�����ʱ������Լ�
	private SimpleAttributeSet timeNameAttr = new SimpleAttributeSet();
	//����������Ϣ�����Լ�
	private SimpleAttributeSet msgAttr = new SimpleAttributeSet();
	//��������doc,����ʵ��˽��
	private HashMap<String,StyledDocument> docs;
	//��ȡmsgPanel�Ĺ�����,�ǹ�����ʼ����ʾ�����·�
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
		//����ʱ���ı���ʽ
		StyleConstants.setFontFamily(timeNameAttr, "���ǵ������");
		StyleConstants.setFontSize(timeNameAttr, 12);
		StyleConstants.setForeground(timeNameAttr, nameTimeColor);
		
		
		try {
			selector = Selector.open();
			sc = SocketChannel.open(new InetSocketAddress(IP,PORT));
			//����sc�Է�����ʽ����
			sc.configureBlocking(false);
			sc.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//�������շ��������ݵ��߳�
		new ClientThread().start();
	}
	
	
	
	public void addMsgToDoc(String msg,int align){
		//�ָ���Ϣ
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
		
		//�ӷ�������������Ϣ
		//�����ǳƻ�QQ
		//�����Ϣ�������,�Ȳ����û���,�ڲ���ʱ��
		if(align==StyleConstants.ALIGN_LEFT){
			StyledDocument doc=null;
			//�յ�����Ⱥ����Ϣ������Ⱥ��doc��
			if("all".equals(target)){
				doc = getDocByQQ("all");
			}else{
				//˽����Ϣ��������Ϣ���ͷ���Ӧ��doc��
				 doc = getDocByQQ(qq);
				if(doc==null){
					doc = new DefaultStyledDocument();
					docs.put(qq, doc);
				}
				//����յ��Ĳ���Ⱥ����Ϣ����ѡ�и��Լ�����Ϣ����һ��
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
		
		//�����������������·�
		sBar.setValue(sBar.getMaximum());
	}
	
	//�յ�˽����Ϣ��ʹ˽�Ķ�����ѡ��״̬
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
	
	//���������û��б�
	private void updateOnlineList(String msg){
		String[] qqList = parseMsg(msg);
		onlineList.clear();
		onlineList.add("all");
		for(int i=0;i<qqList.length;++i){
			onlineList.add(qqList[i]);
		}
	}
	
	//���յ�����Ϣ�ֽ���ַ�������
	private String[] parseMsg(String msg){
		if(msg.trim().length()<=2) return null;
		return  msg.substring(MyProtocal.PROTOCAL_LEN-1,
				msg.length()-MyProtocal.PROTOCAL_LEN+1).
				split(MyProtocal.SPARATOR);
	}
	
	//��str���뵽doc��,������Ϊָ���ĸ�ʽ
	private void insert(String str,SimpleAttributeSet attr,StyledDocument doc){
		int lastIndex = doc.getLength();
		try {
			doc.insertString(lastIndex, str+"\n", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		doc.setParagraphAttributes(lastIndex, doc.getLength()-lastIndex, attr, true);
	}
	
	//��qq�Ų����û��ǳ�,����ǳ�Ϊ��,ֱ�ӷ���qq��
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
	//���������������
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
	//������ϢҪ���뵽��Ŀ��document
	public void setDocument(StyledDocument doc){
		this.curDoc = doc;
	}
/*	
	//���õ�ǰdoc
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
	//���շ��������ݵ��߳�
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
								//���յ����������û�����
								if(msg.startsWith(MyProtocal.USER_ROUND ) &&
									msg.endsWith(MyProtocal.USER_ROUND) ){
									updateOnlineList(msg);
//									System.out.println("updateing online list...");
								}else{
									//���������û���������Ϣ��ʾ����Ϣ�������
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
