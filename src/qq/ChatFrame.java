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
	UserBean all = new UserBean("all","������","girl","˫���������Ⱥ��",
			new ImageIcon(ChatFrame.class.getResource("QQIMG/all.png")));
	UserBean me;
	UserBean target=all;//��ʼ����ΪȺ
	
	//����������ע����û�,���ڷ�����Ϣʱ������Ӧ���û��ǳ�
	Vector<UserBean> friends = new Vector<>();
	//�������������û�,���ڸ�MyCellRenderer�������ߺͲ����ߺ���
	HashSet<String> onlineList = new HashSet<>();
	//��������doc,����ʵ��˽��
	HashMap<String,StyledDocument> docs = new HashMap<>();
	
	JTextPane msgPanel = new JTextPane();
	JScrollPane scrollPane = new JScrollPane(msgPanel);
	MyClient client;//�ͻ���
	
	JPanel toolPanel = new JPanel();
	JToolBar jtb = new JToolBar();
	JLabel fontLabel = new JLabel("����:");
	JComboBox<String> fonts = ListJavaFonts.getFontsCombo();
	JLabel sizeLabel = new JLabel("�ֺ�:");
	JComboBox<Integer> fontSize = MyComboBox.getJComboBox(15, 36, 1);//start,end,step
	JCheckBox bold = new JCheckBox("����");
	JCheckBox italic = new JCheckBox("б��");
	JCheckBox underline = new JCheckBox("�»���");
	JButton colorChooser = new JButton(new ImageIcon(
			ChatFrame.class.getResource("img/colors.png")));
	
	//�༭����
	JTextPane editArea = new JTextPane();
	StyledDocument editDoc = editArea.getStyledDocument();
	SimpleAttributeSet attr = new SimpleAttributeSet();
	
	//������ߵĺ����б����
	JList<UserBean> friendList;
	Box rightBox = new Box(BoxLayout.Y_AXIS);
	JSplitPane content ;
	//�༭������ɫѡ����
	JColorChooser chooser = new JColorChooser();
	
	Color listBgColor = new Color(253,240,140,5);
	Color msgBgColor = new Color(253,240,140,75);
	Color selectedListCellBgColor = new Color(253,137,252,150);
	Color txtColor = selectedListCellBgColor;//��ʼ������ɫ
	
	Connection conn=null;
	PreparedStatement queryAll;
	
	public ChatFrame(UserBean user,Connection conn){
		me = user;
		this.conn = conn;
		//�������˺��Լ������û��б�
		friends.add(all);
		friends.add(me);
		//���Լ����������û��б�
		onlineList.add(all.getQQ());
		onlineList.add(me.getQQ());
		//��Ⱥ��doc����docs Map��
		docs.put(all.getQQ(), msgPanel.getStyledDocument());
		
		initConnections();
		initDatas();
		initLayouts();
		initSettings();
		initActions();
		client = new MyClient(msgPanel.getStyledDocument(),friends,friendList,onlineList,
				scrollPane.getVerticalScrollBar(),docs);
		
		//��ʼ�ı���ʽ
		StyleConstants.setFontFamily(attr, "���ǵ������");
		StyleConstants.setFontSize(attr,18);
		StyleConstants.setForeground(attr, selectedListCellBgColor);
		
		try {
			editDoc.insertString(0, "�ڴ˴�����������Ϣ,�Ϸ��Ĺ����������޸��ı���ʽ",null);
		} catch (BadLocationException e) {e.printStackTrace();}
		editDoc.setCharacterAttributes(0, editDoc.getLength(), attr, true);
		updateStyle();
	}
	
	//����Զ�����ݿ�
	private void initConnections(){
		try {
			if(conn==null || conn.isClosed()){
				conn = MySQL.getConnection();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			conn = MySQL.getConnection();
		}
		//��ѯ��������ע����û�
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
	
	//��������ע����û�����friends(Vector)�б���
	private void initDatas(){
		try {
			if(conn==null || conn.isClosed()){
				initConnections();
			}
			if(conn==null || conn.isClosed()){
				JOptionPane.showMessageDialog(mainWin, "�������ݿ�ʧ��!�����µ�¼!");
				return ;
			}
		} catch (SQLException e) {e.printStackTrace();}
		
		try(
			ResultSet rs = queryAll.executeQuery();	
		){
			while(rs.next()){
				String QQ = rs.getString(1);
//				���ظ����Լ�����vector��
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

	//��ʼ�����ڽ���
	private void initLayouts(){
		//����JList
		friendList = new JList<UserBean>(friends);
		friendList.setCellRenderer(new MyListCellRenderer(onlineList));
		friendList.setVisible(true);
		friendList.setBackground(listBgColor);
		friendList.setSelectionBackground(selectedListCellBgColor);
		friendList.setFixedCellWidth( MyListCellRenderer.CELLSIZE.width);
		friendList.setFixedCellHeight( MyListCellRenderer.CELLSIZE.height);
		
		//ΪmsgPanel���JLayer
//		GradientLayerUI layerUI= new GradientLayerUI();
//		JLayer<JComponent> layer = new JLayer<>(scrollPane,layerUI);
		//�����Ϣ��ʾ���
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
		//��ӹ������
		toolPanel.add(jtb);
		rightBox.add(toolPanel);
		
		editArea.setPreferredSize(new Dimension(540,50));
		//����ı��༭��
		rightBox.add(new JScrollPane(editArea));
		
		//�϶�ʱ��������
		content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,
						new JScrollPane(friendList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),	//��߷�friendList
						rightBox);											//�ұ߷�Box����
		mainWin.add(content);
	}

	//��ʼ����������
	private void initSettings(){
		//��������������
		ImageIcon qqIcon = new ImageIcon(ChatFrame.class.getResource("img/qq.png"));
		//���ô��ڱ���,����ǳƲ�Ϊ��,����ʾ�ǳ�,����ǳ�Ϊ��,����ʾQQ�˺�
		mainWin.setTitle(me.getNickname()!=null?me.getNickname():me.getQQ() + "--all");
		
		mainWin.setIconImage(qqIcon.getImage());
		mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//�ر�ʱ�˳�����
		Dimension screen  = Toolkit.getDefaultToolkit().getScreenSize();
		mainWin.setBounds((screen.width - WIDTH)/2, (screen.height - HEIGHT)/2, WIDTH, HEIGHT);
		mainWin.setResizable(false);
		mainWin.setVisible(true);
		
		friendList.setDoubleBuffered(true);
		friendList.setSelectedIndex(0);
		//���ú����б���۵�
//		content.setOneTouchExpandable(true);
		content.setDividerSize(10);
		//����JSplitPane��UI
		content.setUI(new MySplitPaneUI());
		content.resetToPreferredSizes();
		content.setDividerLocation(MyListCellRenderer.CELLSIZE.width+10);
		content.setComponentPopupMenu( new MyUIManager(mainWin,null).getJPopupMenu());
		//������Ϣ��岻�ɱ༭
		msgPanel.setEditable(false);
		msgPanel.setBackground(msgBgColor);
		
		//���ñ༭�����������ɫ
		editArea.setCaretColor(new Color(253,137,252));
		fonts.setSelectedItem("���ǵ������");
		fontSize.setSelectedItem(18);
		
		chooser.setDragEnabled(true);
	
	}
	
	private void initActions(){
		//Ϊ������Ӵ����¼�������
		mainWin.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				//�ر����ݿ�������Դ
				MySQL.closeRes(null, queryAll, conn);
			}
		});
		
		ActionListener okListener = evt ->{
			txtColor = chooser.getColor();
			StyleConstants.setForeground(attr, txtColor);
			updateStyle();
		};
		
		//����JTextPane֧���Ϸ�,���ѽ��յ���ֵ����foreground����
		editArea.setDragEnabled(true);
		editArea.setTransferHandler(new TransferHandler("foreground"));
		colorChooser.addActionListener(evt ->{
			//����һ����ģʽ����ɫѡ�����Ի���
			JColorChooser.createDialog(mainWin, "����������ɫ", false, chooser,
					okListener , null).setVisible(true);
		});
		//�޸ı༭������
		fonts.addActionListener(evt ->{
			StyleConstants.setFontFamily(attr, fonts.getSelectedItem().toString());
			updateStyle();
		});
		//�޸ı༭���ֺ�
		fontSize.addActionListener(evt ->{
			StyleConstants.setFontSize(attr, 
					Integer.valueOf(fontSize.getSelectedItem().toString()));
			updateStyle();
		});
		//�����Ƿ�б��
		italic.addActionListener(evt ->{
			StyleConstants.setItalic(attr, italic.isSelected());
			updateStyle();
		});
		//�����Ƿ����
		bold.addActionListener(evt ->{
			StyleConstants.setBold(attr, bold.isSelected());
			updateStyle();
		});
		//�����Ƿ��»���
		underline.addActionListener(evt ->{
			StyleConstants.setUnderline(attr, underline.isSelected());
			updateStyle();
		});
		
		//��Ӽ��̼�����
		editArea.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent evt){
				editDoc.setCharacterAttributes(0, editDoc.getLength(), attr, true);
			}
		});
		
		//���û��б��������¼�������
		friendList.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt){
				if(evt.getClickCount()==2){
					target = friendList.getSelectedValue();
					if(target.getQQ().equals(me.getQQ())) return;//���ܸ��Լ�˽��
					System.out.println(me.getQQ()+":cur target:"+target.getQQ());
					mainWin.setTitle(me.getNickname()+"--"+
					(target.getNickname()==null? target.getQQ():target.getNickname()));
					StyledDocument doc = docs.get(target.getQQ());
					if(doc==null){
						doc = new DefaultStyledDocument();
						docs.put(target.getQQ(),doc);
					}
					//����msgPanel��ǰҪ��ʾ��doc
					msgPanel.setStyledDocument(doc);
					//���ÿͻ��˵�ǰʹ�õ�doc
					client.setDocument(doc);
				}
			}
		});
		
		
		//������Ϣ��Action,Action��ActionListener���ӽӿ�
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
				String msg = MyProtocal.MSG_ROUND+					//��ʾ���͵���������Ϣ
							 me.getQQ()	+MyProtocal.SPARATOR +		//0��Ϣ��Դ
							 font 		+ MyProtocal.SPARATOR +		//1��Ϣ������
							 size 		+ MyProtocal.SPARATOR +		//2��Ϣ�Ĵ�С
							 ital 		+ MyProtocal.SPARATOR +		//3��Ϣ�Ƿ�б��
							 bld 		+ MyProtocal.SPARATOR +		//4��Ϣ�Ƿ����
							 underl 	+ MyProtocal.SPARATOR +		//5��Ϣ�Ƿ��»���
							 color 		+ MyProtocal.SPARATOR +		//6��Ϣ����ɫ��Ϣ
							 content 	+MyProtocal.SPARATOR +		//7��Ϣ����
							 target.getQQ() +						//8��Ϣ���Ͷ���
							 MyProtocal.MSG_ROUND;
				editArea.setText(null);
				client.addMsgToDoc(msg, StyleConstants.ALIGN_RIGHT);
				client.sendMsg(msg);
			}
		};
		//���س���������Ϣ
		editArea.getInputMap().put(KeyStroke.getKeyStroke('\n'), "send");
		editArea.getActionMap().put("send", sendAction);
	}
	
	private void updateStyle(){
		editDoc.setCharacterAttributes(0, editDoc.getLength(), attr, true);
		//���༭����foreground��docment��foreground��Ϊһ��
		editArea.setForeground(txtColor);
	}
	
	
}


//������ɫ��JLayer
@SuppressWarnings("serial")
class GradientLayerUI extends LayerUI<JComponent>{
	public void paint(Graphics g,JComponent c){
		super.paint(g, c);
		Graphics2D g2 = (Graphics2D)g.create();
		//����͸��Ч��
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
		//���ý��仭����ɫ
		g2.setPaint(new GradientPaint(0,0,Color.RED,0,c.getHeight(),Color.ORANGE));
		//����һ���뱻װ�����������ͬ��С�����
		g2.fillRect(0, 0, c.getWidth(), c.getHeight());
		g2.dispose();
	}
}