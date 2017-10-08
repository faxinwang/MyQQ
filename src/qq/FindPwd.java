package qq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

import uitl.MySQL;
import uitl.MyUIManager;
import uitl.RegexFormatter;

public class FindPwd {
	
	static final int WIDTH = 500;
	static final int HEIGHT= 350;
	
	JFrame login;
	JFrame mainWin = new JFrame();
	
	JPanel mainPanel = new JPanel();
	
	JLabel qqLabel = new JLabel("QQ:");
	JLabel telLabel = new JLabel("Tel:");
	JLabel emailLabel = new JLabel("Email:");
	JFormattedTextField qqField;
	JFormattedTextField telField;  
	JFormattedTextField emailField;
	JButton submit = new JButton("submit");
	
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints left=new GridBagConstraints();
	GridBagConstraints right=new GridBagConstraints();

	Color bgColor = new Color(100,240,85,110);
	Color fgColor = new Color(240,160,60);
	Font hydytj = new Font("汉仪蝶语体简",Font.ROMAN_BASELINE,22);
	Font fzjljt = new Font("方正静蕾简体",Font.BOLD,30);
	
	Connection conn=null;
	PreparedStatement ps=null;
	ImageIcon qqIcon= new ImageIcon(FindPwd.class.getResource("img/qq.png"));
	String PWD;
	
	public FindPwd(JFrame jf,Connection conn){
		login = jf;
		this.conn = conn;
		initLayouts();
		initSettings();
		initActions();
		initConnections();
	}
	
	private void initLayouts(){
		//设置布局管理器为GridBagLayout
		mainPanel.setLayout(gbl);
		Insets inset = new Insets(10,20,10,20);
		left.insets = inset;
		right.insets = inset;
		left.gridx = 0; right.gridx = 1;
		left.weightx = 1; right.weightx = 2;
//		left.gridwidth = 2; right.gridwidth = 1;
//		left.fill=GridBagConstraints.HORIZONTAL;
		right.fill=GridBagConstraints.HORIZONTAL;	//右边的输入框水平扩展
		left.anchor = GridBagConstraints.CENTER;	//两边的组件都在网格的中央显示
		right.anchor = GridBagConstraints.CENTER;
		
		//创建QQ号码格式文本框
		String qqRegex = "[a-zA-Z0-9]{6,10}";
		RegexFormatter qqFormatter = new RegexFormatter(qqRegex);
		qqFormatter.setOverwriteMode(false);
		qqField = new JFormattedTextField(qqFormatter);
		
		//创Tel的格式文本框
		try {
			MaskFormatter formatter = new MaskFormatter("+86 1## #### ####");
			formatter.setPlaceholderCharacter('□');
			telField = new JFormattedTextField(formatter);
		} catch (ParseException e) {e.printStackTrace();}
		
		//创建email的格式文本框
		String mailRegex = "(((\\w)|(\\.)){3,20}@\\w+\\.(com|cn|org|net|gov))";
		RegexFormatter emailFormatter = new RegexFormatter(mailRegex);
		emailFormatter.setOverwriteMode(false);
		emailField = new JFormattedTextField(emailFormatter);
		emailField.setValue("abc@qq.com");
		
		left.gridy = 0;		addLeft(qqLabel);
		left.gridy = 1;		addLeft(telLabel);
		left.gridy = 2;		addLeft(emailLabel);
		right.gridy = 0;	addRight(qqField);
		right.gridy = 1;	addRight(telField);
		right.gridy = 2;	addRight(emailField);
		
		submit.setForeground(fgColor);
		submit.setFont(fzjljt);
		mainWin.add(submit,BorderLayout.SOUTH);
		
		mainPanel.setBackground(bgColor);
		mainPanel.setComponentPopupMenu(new MyUIManager(mainWin,null).getJPopupMenu());
		
		mainWin.add(mainPanel);
	}
	
	private void initSettings(){
		//设置主窗口属性
		mainWin.setTitle("找回密码");
		mainWin.setIconImage(qqIcon.getImage());
		//设置关闭窗口时销毁该窗口,并不退出程序
		mainWin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		mainWin.setBounds((screen.width - WIDTH)/2,(screen.height - HEIGHT)/2,WIDTH,HEIGHT);
		mainWin.setResizable(false);
		mainWin.setVisible(true);
		qqLabel.setToolTipText("输入要找回密码的QQ!");
		telLabel.setToolTipText("输入注册时填写的电话号码!");
		emailLabel.setToolTipText("输入注册时填写的邮箱地址!");
		mainPanel.setToolTipText("单击右键可更换皮肤!");
	}
	
	private void initActions(){
		//为窗体添加窗口事件监听器,当关闭该窗口时,打开登录窗口,并关闭数据库连接
		mainWin.addWindowListener( new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				MySQL.closeRes(null, ps, null);
				if(login!=null)
					login.setVisible(true);
			}
		});
		
		submit.addActionListener(evt ->{
			//检测QQ输入框是否为空
			String QQ = qqField.getText();
			if(QQ.length()==0){
				JOptionPane.showMessageDialog(qqField, "请输入要找回密码的QQ号码!","输入非法",
							JOptionPane.ERROR_MESSAGE);
				return;
			}
			//检测Tel是否输入正确
			String Tel = telField.getText().substring(4);	//去掉开头的"+86 "
			if( Tel.indexOf('□')>=0 ){
				JOptionPane.showMessageDialog(telField, "请输入完整的电话号码!","输入非法",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			//检测Email填写是否正确
			String Email = emailField.getText();
			if( Email.equals("abc@qq.com") ){
				JOptionPane.showMessageDialog(emailField, "请输入正确的邮箱地址!","输入非法",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			/*
			 * 至此,数据填写均正确 
			 */
			
			short rst = findPwd(QQ,Tel,Email);
			if(rst==0){
				//密码找回成功
				JOptionPane pane = new JOptionPane("恭喜! 密码成功找回!\n"+
						"您的QQ号: "+QQ+" 对应的密码为:\n\t"+PWD+
						"\n请妥善保存!别在忘了哦!",					//message
						JOptionPane.INFORMATION_MESSAGE,		//messageType
						JOptionPane.DEFAULT_OPTION,				//optionType
						qqIcon);								//Icon
				pane.setFont(hydytj);
				pane.setForeground(fgColor);
				JDialog dialog = pane.createDialog("密码找回成功");//title
				//显示对话框
				dialog.setVisible(true);
				//销毁本窗体
				mainWin.dispose();
				MySQL.closeRes(null, ps, null);
				//显示登陆窗口
				if(login!=null)
					login.setVisible(true);
			}else if(rst==1){
				JOptionPane.showMessageDialog(mainWin, "没有找到该账号对应的密码!\n"+
								"请确认 QQ,Tel,Email 填写是否正确!","密码查找失败",
								JOptionPane.ERROR_MESSAGE);
			}else if(rst==2){
				JOptionPane.showMessageDialog(mainWin, "连接数据库失败!\n"+
						"请稍后重试!","密码查找失败",
						JOptionPane.ERROR_MESSAGE);				
			}
		});
	}
	
	private void initConnections(){
		try {
			if(conn==null || conn.isClosed()){
				conn = MySQL.getConnection();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			conn = MySQL.getConnection();
		}
		String query = "select * from userInfo where QQ=? and Tel=? and Email=?";
		try{
			if(ps==null || ps.isClosed())
				ps = conn.prepareStatement(query);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	//定义一个函数添加左边的JLabel
	private void addLeft(JComponent c){
		c.setFont(fzjljt);
		c.setForeground(fgColor);
		gbl.setConstraints(c, left);
		mainPanel.add(c);
	}
	//定义一个函数添加右边的输入控件
	private void addRight(JComponent c){
		c.setFont(hydytj);
		c.setForeground(fgColor);
		if(c instanceof JTextField){
			JTextField jtf = (JTextField)c;
			jtf.setHorizontalAlignment(JTextField.CENTER);
		}
		
		gbl.setConstraints(c,right);
		mainPanel.add(c);
	}
	
	private short findPwd(String QQ,String Tel,String Email){
		initConnections();
		
		//查询数据库
		try {
			ps.setString(1, QQ);
			ps.setString(2, Tel);
			ps.setString(3, Email);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				PWD = rs.getString(2);
				return 0;
			}
			return 1;	//没有找到符合条件的账号秘密
		} catch (SQLException e) {
			e.printStackTrace();
			return 2;//连接数据库异常
		}
	}

	public static void main(String[] args){
		new FindPwd(null,null);
	}
	
}

