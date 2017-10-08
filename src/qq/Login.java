package qq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import uitl.MySQL;
import uitl.MyUIManager;
import uitl.UserBean;

public class Login{
	//定义登录窗口的大小?
	final int WIDTH=430,HEIGHT=340;
	JLabel top = new JLabel();
	JLabel photo = new JLabel();
	JFrame mainWin = new JFrame();
	UserBean userBean ;
	
	final int INPUT_LENGTH=16;
	JTextField username = new JTextField(INPUT_LENGTH);
	JPasswordField pwd = new JPasswordField(INPUT_LENGTH);
	JButton regist = new JButton("注册账号");
	JButton findPwd = new JButton("找回密码");
	JButton loginBn = new JButton("登录");
	JButton modifyInfo = new JButton("修改信息");

	Connection conn=null;
	PreparedStatement queryInfo=null;
	
	
	public Login(){
		initLayouts();
		initSettings();
		initActions();
		initConnections();
	}
	
	private void initLayouts(){
		//设置窗口属性
		ImageIcon qq = new ImageIcon(Login.class.getResource("img/qq.png"));
		mainWin.setTitle("QQ登录");
		mainWin.setIconImage(qq.getImage());
		//设置窗口大小和位置
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		mainWin.setBounds((screenSize.width-WIDTH)/2,(screenSize.height-HEIGHT)/2, WIDTH, HEIGHT);
		mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//设置窗体布局
		top.setIcon(new ImageIcon(Login.class.getResource("img/login_top.gif")));
		photo.setIcon(qq);
		top.setComponentPopupMenu(new MyUIManager(mainWin,null).getJPopupMenu());
		mainWin.add(top,BorderLayout.NORTH); //NORTH
		Box box =new Box(BoxLayout.Y_AXIS);
		JPanel row1 =new JPanel();
		JPanel row2 =new JPanel();
		JPanel row3 =new JPanel();
		row1.add(username);
		row1.add(regist);
		row2.add(pwd);
		row2.add(findPwd);
		row3.add(loginBn);
		row3.add(modifyInfo);
		box.add(row1);
		box.add(row2);
		box.add(row3);
		JPanel center = new JPanel();
		//设置背景粉红色
		center.setBackground(new Color(240,89,254));
		center.setComponentPopupMenu(new MyUIManager(mainWin,null).getJPopupMenu());
		center.add(photo);
		center.add(box);
		mainWin.add(center,BorderLayout.CENTER);
		
	}

	private void initSettings(){
		//设置几个按钮的大小
		Dimension dim = new Dimension(185,30);
		loginBn.setPreferredSize(dim);		
		Dimension btnDim = new Dimension(90,30);
		regist.setPreferredSize(btnDim);
		findPwd.setPreferredSize(btnDim);
		modifyInfo.setPreferredSize(btnDim);
		//设置两个文本框的大小
		Dimension txtDim = new Dimension(60,30);
		username.setPreferredSize(txtDim);
		pwd.setPreferredSize(txtDim);
		
		username.setText("QQNumber");
		username.setHorizontalAlignment(JTextField.CENTER);
		username.setToolTipText("请输入QQ账号!");
		pwd.setText("123456");
		pwd.setHorizontalAlignment(JPasswordField.CENTER);
		pwd.setToolTipText("请输入QQ密码,若忘记密码,可点击右侧的按钮找回密码!");
		pwd.setEchoChar('♥');
		photo.setToolTipText("看什么看,没见过帅锅啊!!!");
		top.setToolTipText("点击右键可更换程序外观风格!");
		modifyInfo.setToolTipText("先输入正确的用户名和密码,才能点击该按钮修改信息!");
		
		mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWin.setResizable(false);
		mainWin.setVisible(true);

	}
	
	private void initActions(){
		//为"修改信息"按钮添加监听器
		modifyInfo.addActionListener(evt ->{
			if(checkInput()==false) return;
			String user = username.getText().trim();
			String pass = new String(pwd.getPassword());
			//在数据库中查询用户名和密码,返回查询结果
			int rst = login(user,pass); 
			if(rst==0){
				//掩藏该窗口,打开修改信息窗口
				mainWin.setVisible(false);
				new ModifyInfo(mainWin,userBean,conn);
			}else if(rst==1){
				JOptionPane.showMessageDialog(mainWin,"用户名或密码错误!\n请填写正确的用户名和密码!","",
						JOptionPane.ERROR_MESSAGE);
			}else{
				JOptionPane.showMessageDialog(mainWin,"连接远程数据库失败!\n请稍后重试!","",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		//为"登录"按钮添加监听器
		loginBn.addActionListener( evt ->{
			lgin.actionPerformed(evt);
		});
		username.getInputMap().put(KeyStroke.getKeyStroke('\n'), "login");
		username.getActionMap().put("login", lgin);
		pwd.getInputMap().put(KeyStroke.getKeyStroke('\n'), "login");
		pwd.getActionMap().put("login", lgin);
		
		//为注册按钮添加事件监听器
		regist.addActionListener(evt ->{
			//关闭数据库连接
			MySQL.closeRes(null, queryInfo, null);
			//隐藏登录窗口,打开注册窗口
			mainWin.setVisible(false);
			//打开注册窗口
			new Regist(mainWin,conn);
		});
		//为找回密码按钮添加事件监听器
		findPwd.addActionListener(evt ->{
			MySQL.closeRes(null, queryInfo, null);
			//隐藏登录窗口,打开找回密码窗口
			mainWin.setVisible(false);
			//打开找回密码窗口
			new FindPwd(mainWin,conn);
		});
		
		//为窗体添加事件监听器,当关闭本窗口时,关闭数据库连接
		mainWin.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				//关闭数据库连接资源
				MySQL.closeRes(null, queryInfo,conn);
			}
		});
	}
	
	private void initConnections(){
		//如果数据库关闭了,重新获取数据库连接
		try {
			if(conn==null || conn.isClosed())
				conn = MySQL.getConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
			conn = MySQL.getConnection();
		}
		
		String sqlInfo = "select t1.Nickname, t1.Email, t1.Sex, t2.Photo "
				+ "from userInfo t1, userPhoto t2 "
				+ "where t1.QQ=t2.QQ and t1.QQ=? and t1.Pwd=?";
		
		try {
			//如果statement关闭,重新获取statement
			if(queryInfo==null || queryInfo.isClosed()){
				try {
					queryInfo = conn.prepareStatement(sqlInfo);
				} catch (SQLException e) {e.printStackTrace();}
			}
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	private short login(String username,String pwd){
		initConnections();
		try {
			if(conn==null || conn.isClosed()){
				return 2;	//连接数据库失败
			}
		} catch (SQLException e2) {	e2.printStackTrace();}
		
		//设置查询中的用户名和密码
		try {
			queryInfo.setString(1, username);
			queryInfo.setString(2, pwd);
		} catch (SQLException e1) {e1.printStackTrace();}
		
		try(
			ResultSet rs = queryInfo.executeQuery();
		){
			if(rs.next()){
				String Nickname = rs.getString(1);
				String Email = rs.getString(2);//may be null
				String Sex = rs.getString(3);	//mey by null
				Blob blob = rs.getBlob(4);
				ImageIcon Photo= new ImageIcon( blob.getBytes(1L, (int)blob.length() ) );
				userBean = new UserBean(username,Nickname,Sex,Email,Photo);
				return 0;//登录成功
			}
			return 1;	//用户名或密码错误
		} catch (Exception e) {
			e.printStackTrace();
			return 2;//连接数据库失败
		}
	}
	
	private boolean checkInput(){
		//用户名长度小小于6,不合法
		if(username.getText().trim().length()==0){
			JOptionPane.showMessageDialog(mainWin,"QQ号不能为空?,请输入QQ账号!","登录失败",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		//密码为空,不合法
		if(new String(pwd.getPassword()).length()==0){
			JOptionPane.showMessageDialog(mainWin,"密码不能为空,请输入密码!","登录失败",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("serial")
	Action lgin = new AbstractAction(){
		@Override
		public void actionPerformed(ActionEvent e) {
			if(checkInput()==false) return;
			
			String user = username.getText().trim();
			String pass = new String(pwd.getPassword());
			//在数据库中查询用户名和密码,返回查询结果
			int rst = login(user,pass); 
			if(rst==0){
				System.out.println("登录成功!");
				//关闭数据库连接
				MySQL.closeRes(null,queryInfo, null);
				new ChatFrame(userBean,conn);
				mainWin.dispose();
			}else if(rst==1){
				JOptionPane.showMessageDialog(mainWin,"用户名或密码错误!","登录失败",
						JOptionPane.ERROR_MESSAGE);
			}else{
				JOptionPane.showMessageDialog(mainWin,"连接远程数据库失败!","登录异常",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	};
	public static void main(String[] args){
		new Login();
	}
}

