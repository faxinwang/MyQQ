package qq;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.plaf.LayerUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import myComponents.MyListCellRenderer;
import myComponents.MySplitPaneUI;
import uitl.ListJavaFonts;
import uitl.MyComboBox;
import uitl.MyProtocal;
import uitl.MySQL;
import uitl.MyUIManager;
import uitl.UserBean;

public class ChatFrame{
	static final int WIDTH = 820;
	static final int HEIGHT = 490;
	JFrame mainWin = new JFrame();
	UserBean all = new UserBean("all","所有人","girl","双击该项进入群聊",
			new ImageIcon(ChatFrame.class.getResource("QQIMG/all.png")));
	UserBean me;
	UserBean target=all;//初始对象为群
	
	//保存所有已注册的用户,用于发送消息时查找相应的用户昵称
	Vector<UserBean> friends = new Vector<>();
	//保存所有在线用户,用于给MyCellRenderer区分在线和不在线好友
	HashSet<String> onlineList = new HashSet<>();
	//保存所有doc,用以实现私聊
	HashMap<String,StyledDocument> docs = new HashMap<>();
	
	JTextPane msgPanel = new JTextPane();
	JScrollPane scrollPane = new JScrollPane(msgPanel);
	MyClient client;//客户端
	
	JPanel toolPanel = new JPanel();
	JToolBar jtb = new JToolBar();
	JLabel fontLabel = new JLabel("字体:");
	JComboBox<String> fonts = ListJavaFonts.getFontsCombo();
	JLabel sizeLabel = new JLabel("字号:");
	JComboBox<Integer> fontSize = MyComboBox.getJComboBox(15, 36, 1);//start,end,step
	JCheckBox bold = new JCheckBox("粗体");
	JCheckBox italic = new JCheckBox("斜体");
	JCheckBox underline = new JCheckBox("下划线");
	JButton colorChooser = new JButton(new ImageIcon(
			ChatFrame.class.getResource("img/colors.png")));
	
	//编辑区域
	JTextPane editArea = new JTextPane();
	StyledDocument editDoc = editArea.getStyledDocument();
	SimpleAttributeSet attr = new SimpleAttributeSet();
	
	//窗口左边的好友列表组件
	JList<UserBean> friendList;
	Box rightBox = new Box(BoxLayout.Y_AXIS);
	JSplitPane content ;
	//编辑器的颜色选择器
	JColorChooser chooser = new JColorChooser();
	
	Color listBgColor = new Color(253,240,140,5);
	Color msgBgColor = new Color(253,240,140,75);
	Color selectedListCellBgColor = new Color(253,137,252,150);
	Color txtColor = selectedListCellBgColor;//初始文字颜色
	
	Connection conn=null;
	PreparedStatement queryAll;
	
	public ChatFrame(UserBean user,Connection conn){
		me = user;
		this.conn = conn;
		//将所有人和自己加入用户列表
		friends.add(all);
		friends.add(me);
		//将自己加入在线用户列表
		onlineList.add(all.getQQ());
		onlineList.add(me.getQQ());
		//将群聊doc加入docs Map中
		docs.put(all.getQQ(), msgPanel.getStyledDocument());
		
		initConnections();
		initDatas();
		initLayouts();
		initSettings();
		initActions();
		client = new MyClient(msgPanel.getStyledDocument(),friends,friendList,onlineList,
				scrollPane.getVerticalScrollBar(),docs);
		
		//初始文本格式
		StyleConstants.setFontFamily(attr, "汉仪蝶语体简");
		StyleConstants.setFontSize(attr,18);
		StyleConstants.setForeground(attr, selectedListCellBgColor);
		
		try {
			editDoc.insertString(0, "在此处输入聊天信息,上方的工具条可以修改文本格式",null);
		} catch (BadLocationException e) {e.printStackTrace();}
		editDoc.setCharacterAttributes(0, editDoc.getLength(), attr, true);
		updateStyle();
	}
	
	//连接远程数据库
	private void initConnections(){
		try {
			if(conn==null || conn.isClosed()){
				conn = MySQL.getConnection();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			conn = MySQL.getConnection();
		}
		//查询出所有已注册的用户
		String query = "select t1.QQ,t1.Nickname, t1.Sex, t1.Email, t2.Photo "
					+ "from userInfo t1, userPhoto t2 "
					+ "where t1.QQ=t2.QQ ";
		try {
			if(queryAll==null || queryAll.isClosed()){
				queryAll = conn.prepareStatement(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//将所有已注册的用户读入friends(Vector)列表中
	private void initDatas(){
		try {
			if(conn==null || conn.isClosed()){
				initConnections();
			}
			if(conn==null || conn.isClosed()){
				JOptionPane.showMessageDialog(mainWin, "连接数据库失败!请重新登录!");
				return ;
			}
		} catch (SQLException e) {e.printStackTrace();}
		
		try(
			ResultSet rs = queryAll.executeQuery();	
		){
			while(rs.next()){
				String QQ = rs.getString(1);
//				不重复把自己放入vector中
				if(QQ.equals(me.getQQ())) 
					continue;
				String Nickname = rs.getString(2);
				String Sex = rs.getString(3);
				String Email = rs.getString(4);
				Blob blob = rs.getBlob(5);
				ImageIcon Photo  =new ImageIcon(blob.getBytes(1L, (int)blob.length() ) );
				friends.add(new UserBean(QQ,Nickname,Sex,Email,Photo));
			}
		}catch(SQLException e){e.printStackTrace();}
		MySQL.closeRes(null, queryAll, conn);
	}

	//初始化窗口界面
	private void initLayouts(){
		//创建JList
		friendList = new JList<UserBean>(friends);
		friendList.setCellRenderer(new MyListCellRenderer(onlineList));
		friendList.setVisible(true);
		friendList.setBackground(listBgColor);
		friendList.setSelectionBackground(selectedListCellBgColor);
		friendList.setFixedCellWidth( MyListCellRenderer.CELLSIZE.width);
		friendList.setFixedCellHeight( MyListCellRenderer.CELLSIZE.height);
		
		//为msgPanel添加JLayer
//		GradientLayerUI layerUI= new GradientLayerUI();
//		JLayer<JComponent> layer = new JLayer<>(scrollPane,layerUI);
		//添加信息显示面板
		rightBox.add(scrollPane);
		msgPanel.setPreferredSize(new Dimension(540,290));
		
		jtb.add(fontLabel);
		jtb.add(fonts);
//		fonts.setPreferredSize(new Dimension(150,20));
		jtb.add(sizeLabel);
		jtb.add(fontSize);
//		fontSize.setPreferredSize(new Dimension(25,20));
		jtb.add(italic);
		jtb.add(bold);
		jtb.add(underline);
		jtb.add(colorChooser);
		toolPanel.setPreferredSize(new Dimension(540,20));
		//添加工具面板
		toolPanel.add(jtb);
		rightBox.add(toolPanel);
		
		editArea.setPreferredSize(new Dimension(540,50));
		//添加文本编辑域
		rightBox.add(new JScrollPane(editArea));
		
		//拖动时连续绘制
		content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,
						new JScrollPane(friendList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),	//左边放friendList
						rightBox);											//右边放Box容器
		mainWin.add(content);
	}

	//初始化所有设置
	private void initSettings(){
		//设置主窗口属性
		ImageIcon qqIcon = new ImageIcon(ChatFrame.class.getResource("img/qq.png"));
		//设置窗口标题,如果昵称不为空,就显示昵称,如果昵称为空,就显示QQ账号
		mainWin.setTitle(me.getNickname()!=null?me.getNickname():me.getQQ() + "--all");
		
		mainWin.setIconImage(qqIcon.getImage());
		mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//关闭时退出程序
		Dimension screen  = Toolkit.getDefaultToolkit().getScreenSize();
		mainWin.setBounds((screen.width - WIDTH)/2, (screen.height - HEIGHT)/2, WIDTH, HEIGHT);
		mainWin.setResizable(false);
		mainWin.setVisible(true);
		
		friendList.setDoubleBuffered(true);
		friendList.setSelectedIndex(0);
		//设置好友列表可折叠
//		content.setOneTouchExpandable(true);
		content.setDividerSize(10);
		//设置JSplitPane的UI
		content.setUI(new MySplitPaneUI());
		content.resetToPreferredSizes();
		content.setDividerLocation(MyListCellRenderer.CELLSIZE.width+10);
		content.setComponentPopupMenu( new MyUIManager(mainWin,null).getJPopupMenu());
		//设置信息面板不可编辑
		msgPanel.setEditable(false);
		msgPanel.setBackground(msgBgColor);
		
		//设置编辑器插入光标的颜色
		editArea.setCaretColor(new Color(253,137,252));
		fonts.setSelectedItem("汉仪蝶语体简");
		fontSize.setSelectedItem(18);
		
		chooser.setDragEnabled(true);
	
	}
	
	private void initActions(){
		//为窗口添加窗口事件处理器
		mainWin.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				//关闭数据库连接资源
				MySQL.closeRes(null, queryAll, conn);
			}
		});
		
		ActionListener okListener = evt ->{
			txtColor = chooser.getColor();
			StyleConstants.setForeground(attr, txtColor);
			updateStyle();
		};
		
		//设置JTextPane支持拖放,并把接收到的值赋给foreground属性
		editArea.setDragEnabled(true);
		editArea.setTransferHandler(new TransferHandler("foreground"));
		colorChooser.addActionListener(evt ->{
			//创建一个非模式的颜色选择器对话框
			JColorChooser.createDialog(mainWin, "设置文字颜色", false, chooser,
					okListener , null).setVisible(true);
		});
		//修改编辑器字体
		fonts.addActionListener(evt ->{
			StyleConstants.setFontFamily(attr, fonts.getSelectedItem().toString());
			updateStyle();
		});
		//修改编辑器字号
		fontSize.addActionListener(evt ->{
			StyleConstants.setFontSize(attr, 
					Integer.valueOf(fontSize.getSelectedItem().toString()));
			updateStyle();
		});
		//设置是否斜体
		italic.addActionListener(evt ->{
			StyleConstants.setItalic(attr, italic.isSelected());
			updateStyle();
		});
		//设置是否粗体
		bold.addActionListener(evt ->{
			StyleConstants.setBold(attr, bold.isSelected());
			updateStyle();
		});
		//设置是否下划线
		underline.addActionListener(evt ->{
			StyleConstants.setUnderline(attr, underline.isSelected());
			updateStyle();
		});
		
		//添加键盘监听器
		editArea.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent evt){
				editDoc.setCharacterAttributes(0, editDoc.getLength(), attr, true);
			}
		});
		
		//给用户列表添加鼠标事件监听器
		friendList.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt){
				if(evt.getClickCount()==2){
					target = friendList.getSelectedValue();
					if(target.getQQ().equals(me.getQQ())) return;//不能跟自己私聊
					System.out.println(me.getQQ()+":cur target:"+target.getQQ());
					mainWin.setTitle(me.getNickname()+"--"+
					(target.getNickname()==null? target.getQQ():target.getNickname()));
					StyledDocument doc = docs.get(target.getQQ());
					if(doc==null){
						doc = new DefaultStyledDocument();
						docs.put(target.getQQ(),doc);
					}
					//设置msgPanel当前要显示的doc
					msgPanel.setStyledDocument(doc);
					//设置客户端当前使用的doc
					client.setDocument(doc);
				}
			}
		});
		
		
		//发送消息的Action,Action是ActionListener的子接口
		@SuppressWarnings("serial")
		Action sendAction = new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(editArea.getText().trim().length()==0) return;
					
				String font = fonts.getSelectedItem().toString();
				String size = fontSize.getSelectedItem().toString();
				String ital = italic.isSelected()?"true":"false";
				String bld = bold.isSelected()?"true":"false";
				String underl = underline.isSelected()?"true":"false";
				Color cl = editArea.getForeground();
				String color = cl.getRed()+","+cl.getGreen()+","+cl.getBlue();
				String content = editArea.getText();
				String msg = MyProtocal.MSG_ROUND+					//表示发送的是聊天消息
							 me.getQQ()	+MyProtocal.SPARATOR +		//0消息来源
							 font 		+ MyProtocal.SPARATOR +		//1消息的字体
							 size 		+ MyProtocal.SPARATOR +		//2消息的大小
							 ital 		+ MyProtocal.SPARATOR +		//3消息是否斜体
							 bld 		+ MyProtocal.SPARATOR +		//4消息是否粗体
							 underl 	+ MyProtocal.SPARATOR +		//5消息是否下划线
							 color 		+ MyProtocal.SPARATOR +		//6消息的颜色信息
							 content 	+MyProtocal.SPARATOR +		//7消息内容
							 target.getQQ() +						//8消息发送对象
							 MyProtocal.MSG_ROUND;
				editArea.setText(null);
				client.addMsgToDoc(msg, StyleConstants.ALIGN_RIGHT);
				client.sendMsg(msg);
			}
		};
		//按回车键发送消息
		editArea.getInputMap().put(KeyStroke.getKeyStroke('\n'), "send");
		editArea.getActionMap().put("send", sendAction);
	}
	
	private void updateStyle(){
		editDoc.setCharacterAttributes(0, editDoc.getLength(), attr, true);
		//将编辑器的foreground和docment的foreground设为一致
		editArea.setForeground(txtColor);
	}
	
	
}


//渐变颜色的JLayer
@SuppressWarnings("serial")
class GradientLayerUI extends LayerUI<JComponent>{
	public void paint(Graphics g,JComponent c){
		super.paint(g, c);
		Graphics2D g2 = (Graphics2D)g.create();
		//设置透明效果
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
		//设置渐变画笔颜色
		g2.setPaint(new GradientPaint(0,0,Color.RED,0,c.getHeight(),Color.ORANGE));
		//绘制一个与被装饰组件具有相同大小的组件
		g2.fillRect(0, 0, c.getWidth(), c.getHeight());
		g2.dispose();
	}
}