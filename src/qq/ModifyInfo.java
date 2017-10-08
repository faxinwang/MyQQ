package qq;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.MaskFormatter;

import uitl.MyComboBox;
import uitl.MyFileChooser;
import uitl.MySQL;
import uitl.MyUIManager;
import uitl.RegexFormatter;
import uitl.UserBean;
import uitl.ZoomImage;

public class ModifyInfo{
	JFrame login;
	JFrame mainWin = new JFrame();
	//本窗口的大小
	static final int WIDTH = 650;
	static final int HEIGHT = 650;
	//将登陆的用户的信息传递进来
	UserBean userBean ;
	
	JPanel mainPanel = new JPanel();
	JFormattedTextField qq ;
	JPasswordField pwd1 = new JPasswordField();
	JPasswordField pwd2 = new JPasswordField();
	JFormattedTextField tel ;
	JTextField nickname = new JTextField();
	JFormattedTextField age = new JFormattedTextField(NumberFormat.getIntegerInstance());
	JRadioButton male = new JRadioButton("boy");
	JRadioButton female = new JRadioButton("girl");
	JFormattedTextField email ;
	//初始头像为企鹅
	ImageIcon qqIcon = new ImageIcon(ModifyInfo.class.getResource("QQIMG/0.png"));
	String choosedImagePath=null;
	JComboBox<ImageIcon> photos = MyComboBox.getImageJComboBox();
	JButton borwser = new JButton("选择上传头像");
	MyFileChooser imgChooser = new MyFileChooser();
	
	GridBagLayout gbl= new GridBagLayout();
	GridBagConstraints left = new GridBagConstraints();
	GridBagConstraints right = new GridBagConstraints();
	
	Color bgColor = new Color(200,90,230,80);
	Color fgColor = new Color(200,90,200);
	Dimension leftDim = new Dimension(120,35);
	Dimension rightDim = new Dimension(80,35);
	Font hydytj = new Font("汉仪蝶语体简",Font.ROMAN_BASELINE,22);
	Font fzjljt = new Font("方正静蕾简体",Font.BOLD,22);
	JLabel[]  labels = new JLabel[8];
	
	JButton submit = new JButton("提交");
	JButton reset = new JButton("重置");
	
	Connection conn=null;
	PreparedStatement insertInfo=null;
	PreparedStatement insertPhoto=null;
	
	public ModifyInfo(JFrame last,UserBean user,Connection conn){
		login = last;
		userBean = user;
		this.conn = conn;
		initLayouts();
		initSettings();
		initActions();
		initDatas();
		initConnections();
	}
	
	private void initLayouts(){
		//设置布局管理器为GridBagLayout
		mainPanel.setLayout(gbl);
		Insets inset = new Insets(10,20,10,20);
		left.insets = inset;
		right.insets = inset;
		left.gridx = 0; right.gridx = 1;
		left.weightx = 1; right.weightx = 1;
//		left.gridwidth = 2; right.gridwidth = 1;
//		left.fill=GridBagConstraints.HORIZONTAL;
		right.fill=GridBagConstraints.HORIZONTAL;	//右边的输入框水平扩展
		left.anchor = GridBagConstraints.CENTER;	//两边的组件都在网格的中央显示
		right.anchor = GridBagConstraints.CENTER;
		
		labels[0] = new JLabel("*输入QQ:");
		labels[1] = new JLabel("*输入密码:");
		labels[2] = new JLabel("*确认密码:");
		labels[3] = new JLabel("*手机号码:");
		labels[4] = new JLabel("*输入年龄:");
		labels[5] = new JLabel("*输入email:");
		labels[6] = new JLabel(" 输入昵称:");
		labels[7] = new JLabel(" 选择性别:");
		//将这8个标签放到mainPanel中
		for(int i=0;i<8;++i){
			//设置标签的纵坐标
			left.gridy = i;
			addLeft(labels[i]);
		}
		//创建QQ号码格式文本框
		String qqRegex = "[a-zA-Z0-9]{6,10}";
		RegexFormatter qqFormatter = new RegexFormatter(qqRegex);
		qqFormatter.setOverwriteMode(false);
		qq = new JFormattedTextField(qqFormatter);
		
		//创建电话号码的格式文本框
		try {
			MaskFormatter formatter = new MaskFormatter("+86 1## #### ####");
			formatter.setPlaceholderCharacter('□');
			tel = new JFormattedTextField(formatter);
		} catch (ParseException e) {e.printStackTrace();}
		//创建email的格式文本框
		String mailRegex = "(((\\w)|(\\.)){3,20}@\\w+\\.(com|cn|org|net|gov))";
		RegexFormatter emailFormatter = new RegexFormatter(mailRegex);
		emailFormatter.setOverwriteMode(false);
		email = new JFormattedTextField(emailFormatter);
		email.setValue("abc@qq.com");
		
		
		//将输入框放入mainPanel中
		right.gridy = 0;	addRight(qq);
		right.gridy = 1;	addRight(pwd1);
		right.gridy = 2;	addRight(pwd2);
		right.gridy = 3;	addRight(tel);
		right.gridy = 4;	addRight(age);
		right.gridy = 5;	addRight(email);
		right.gridy = 6;	addRight(nickname);
		JPanel sex = new JPanel();
		sex.setLayout(new GridLayout(1,2));
		ButtonGroup gp = new ButtonGroup();
		gp.add(male);
		gp.add(female);
		sex.add(male);
		sex.add(female);
		male.setSelectedIcon(new ImageIcon(ModifyInfo.class.getResource("img/boy.png")));
		female.setSelectedIcon(new ImageIcon(ModifyInfo.class.getResource("img/girl.png")));
		right.gridy = 7;	addRight(sex);
		
		//添加头像上传部件
		left.gridy = 8;
		gbl.setConstraints(photos, left);
		mainPanel.add(photos);
		
		right.gridy = 8;	
		addRight(borwser);
		
		
		//添加提交和重置按钮到mainPanel中
		left.gridy = 9; 	addLeft(submit);
		right.gridy= 9;		addRight(reset);
		//将mainPanel放入主窗口中
		mainWin.add(mainPanel);
	}
	
	private void initSettings(){
		//设置窗口属性
		mainWin.setIconImage(qqIcon.getImage());
		mainWin.setTitle("修改信息");
		mainWin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		mainWin.setBounds((screen.width - WIDTH)/2, (screen.height - HEIGHT)/2, WIDTH, HEIGHT);
		mainWin.setResizable(false);
		mainWin.setVisible(true);
		
		//设置标签提示文字
		labels[0].setToolTipText("QQ号只能包含数字和英文字母,长度不小于6位,不超过10位."
				+ "还不赶紧注册短号,体验一把拥有6位数QQ的感觉!");
		labels[1].setToolTipText("请输入秘密,长度不小于6位,不超过10位!");
		labels[2].setToolTipText("带星号的为必填项!");
		labels[3].setToolTipText("请输入正确的电话号码,用于和QQ号,电子邮件一起找回密码!");
		labels[4].setToolTipText("请输入一个正整数!");
		labels[5].setToolTipText("请输入正确的邮箱地址,用于和QQ号,电话号码一起找回密码!");
		//设置背景色和右键菜单
		mainPanel.setBackground(bgColor);
		mainPanel.setComponentPopupMenu(new MyUIManager(mainWin,null).getJPopupMenu());
		mainPanel.setToolTipText("点击右键可更换程序外观!");
		//设置photos(JBomboBox)初始选择第一个头像
		photos.setSelectedIndex(0);
		photos.setToolTipText("请选择列表中的头像或点击右边按钮上传自定义头像!");
		//设置qq文本框不可编辑,前景色为蓝色
		qq.setEditable(false);
		qq.setForeground(Color.BLUE);
	}
	
	private void initActions(){
		
		//为窗体添加事件监听器,当关闭本窗口时,打开登录窗口
		mainWin.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				//close statements
				MySQL.closeRes(null, insertInfo, null);
				MySQL.closeRes(null, insertPhoto, null);
				//显示登陆界面
				if(login!=null)
					login.setVisible(true);
			}
		});
		//为头像选择按钮添加事件监听器
		borwser.addActionListener( evt ->{
			String path = imgChooser.chooseImage();
			if(path != null){
				ImageIcon icon =  new ImageIcon(path);
				//如果头像太大,压缩头像到指定大小
				if(icon.getIconWidth() > MyFileChooser.PREVIEW_SIZE){
					icon = new ImageIcon(icon.getImage().
							getScaledInstance(MyFileChooser.PREVIEW_SIZE,
									MyFileChooser.PREVIEW_SIZE, Image.SCALE_SMOOTH));
				}
				photos.addItem(icon);
				photos.setSelectedItem(icon);
				choosedImagePath = path;
			}
		});
		
		//为photos(JComboBox<ImageIcon>)添加选择器,当用户选择了头像时,更新choosedImagePath
		photos.addActionListener(evt ->{
			int index = photos.getSelectedIndex();
			if(0<=index && index<=24){
				File curDir = new File(".");
				String parent = curDir.getAbsolutePath();
				choosedImagePath=parent.substring(0,parent.length()-1)
						+"src\\qq\\QQIMG\\"+index+".png";
			}
		});
		
		//为重置按钮添加事件监听器
		reset.addActionListener( evt ->{
			qq.setValue(null);
			pwd1.setText(null);
			pwd2.setText(null);
			tel.setText(null);
			age.setText(null);
			email.setValue("abc@qq.com");
			nickname.setText(null);
			male.setSelected(false);
			female.setSelected(false);
			photos.setSelectedIndex(0);//第一个item下标为0
			choosedImagePath=null;
		});
		//为提交按钮添加事件监听器
		submit.addActionListener(evt ->{
			
			if(qq.getText().length()==0){
				JOptionPane.showMessageDialog(qq, "QQ账号不能为空!请输入6~10位由数字或字母组成的QQ!","QQ非法",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String pass1 = new String(pwd1.getPassword());
			if(pass1.length()<6){
				JOptionPane.showMessageDialog(pwd1, "密码长度不能小于6位!","密码为空",
						JOptionPane.ERROR_MESSAGE);
				return;
			}else if(pass1.length()>16){
				JOptionPane.showMessageDialog(pwd1, "密码长度不能大于16位!","密码长度超限",
						JOptionPane.ERROR_MESSAGE);
				return;				
			}
			
			String pass2 = new String(pwd2.getPassword());
			if(!pass2.equals(pass1)){
				JOptionPane.showMessageDialog(pwd2, "输入密码不一致,请重新输入!","密码不一致",
						JOptionPane.ERROR_MESSAGE);
				return;	
			}
			
			String Tel=null;
			try {
				Tel = tel.getText(4, 13);
			} catch (Exception e) {e.printStackTrace();}
			if(Tel.indexOf('□') >=0){
				JOptionPane.showMessageDialog(tel, "请输入完整的电话号码!","电话号码为空",
						JOptionPane.ERROR_MESSAGE);
				return;	
			}
			
			//未输入年龄
			if(age.getText().length()==0){
				JOptionPane.showMessageDialog(age, "请输入年龄!","年龄为空",
						JOptionPane.ERROR_MESSAGE);
				return;	
			}else{
				try{
					//年龄为负数,不合法
					if(Integer.valueOf(age.getText().trim()) < 0){
						JOptionPane.showMessageDialog(age, "年龄不能为负数!","年龄非法",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				//发生异常说明输入年龄超过了1000
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(age, "你丫是穿越过来的吗?这么老!","年龄非法",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

			}
			
			if(email.getText().equals("abc@qq.com")){
				JOptionPane.showMessageDialog(email, "请输入正确的邮箱地址!","邮箱为空",
						JOptionPane.ERROR_MESSAGE);
				return;	
			}
			
			/*
			 * 至此,所有输入数据均正常!
			 * 开始提交注册信息.
			 */
			short rst = updateInfo(); 
			if(rst==0){
				//注册成功
				JOptionPane pane = new JOptionPane("信息已经成功更新!\n"+
						"您的QQ号为 : "+userBean.getQQ()+"\n密码为 : "+new String(pwd1.getPassword())+
						"\n请妥善保存! 如忘记密码,可使用 Tel,QQ,Email 找回!",	//message
						JOptionPane.INFORMATION_MESSAGE,		//messageType
						JOptionPane.DEFAULT_OPTION,				//optionType
						qqIcon);								//Icon
				pane.setFont(hydytj);
				pane.setForeground(fgColor);
				JDialog dialog = pane.createDialog("修改信息");
				dialog.setVisible(true);
				//销毁该窗口
				mainWin.dispose();
				//关闭数据库连接资源
				MySQL.closeRes(null, insertInfo, null);
				//显示登陆窗口
				if(login!=null)
					login.setVisible(true);
//				new Login();
			}else if(rst==1){
				JOptionPane.showMessageDialog(mainWin, "电话号码不正确!","输入非法",
						JOptionPane.ERROR_MESSAGE);
			}else if(rst==2){
				//连接数据库异常,注册失败
				JOptionPane.showMessageDialog(mainWin, "信息提交失败,请稍后重试!","修改失败",
						JOptionPane.ERROR_MESSAGE);
			}else if(rst==3){
				JOptionPane.showMessageDialog(mainWin, "发生未知异常,请稍后重试!","修改失败",
						JOptionPane.ERROR_MESSAGE);
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
		
		//
		String sqlInfo = "update userInfo set Pwd=?,Tel=?,Age=?,Email=?,Nickname=?,sex=? "
				+"where QQ=?";
		try {
			//如果statement关闭,重新获取statement
			if(insertInfo==null || insertInfo.isClosed()){
				try {
					insertInfo = conn.prepareStatement(sqlInfo);
				} catch (SQLException e) {e.printStackTrace();}
			}
		} catch (SQLException e) {e.printStackTrace();}
		
		String sqlPhoto = "update userPhoto set Photo=? where QQ=?";
		
		try{
			if(insertPhoto==null || insertPhoto.isClosed()){
				try{
					insertPhoto = conn.prepareStatement(sqlPhoto);
				}catch(SQLException e){e.printStackTrace();}
			}
		}catch(SQLException e){e.printStackTrace();}
	}
	
	private short updateInfo(){
		//获取各项信息
		String QQ = userBean.getQQ();
		String Pwd = new String(pwd1.getPassword());
		String Tel=null;
		try {
			Tel = tel.getText(4,13);
		} catch (BadLocationException e){
			e.printStackTrace();
			return 1;//电话号码不正确,一般不会发生
		}
		int Age = Integer.valueOf(age.getText());
		String Email = email.getText();
		String Nickname =nickname.getText();
		String Sex = male.isSelected()?"boy":"girl";
		
		try {
			if(conn==null || conn.isClosed()){
				initConnections();
			}
		} catch (SQLException e1) {e1.printStackTrace();}
		//查看数据库连接是否成功
		try {
			if(conn==null || conn.isClosed()){
				return 2;//连接数据库失败
			}
		} catch (SQLException e1) {e1.printStackTrace();}

		
		try{
			//连接远程数据库提交信息
			insertInfo.setString(1, Pwd);
			insertInfo.setString(2, Tel);
			insertInfo.setInt(3, Age);
			insertInfo.setString(4, Email);
			insertInfo.setString(5, Nickname);
			insertInfo.setString(6, Sex);
			insertInfo.setString(7, QQ);

			//先上传文本信息
			if(insertInfo.executeUpdate()==1){
				//如果修改了头像,上传头像信息
				if(choosedImagePath!=null){
					ZoomImage zoom = new ZoomImage(MyFileChooser.PREVIEW_SIZE,
							MyFileChooser.PREVIEW_SIZE);
					String zoomed = zoom.zoomImage(choosedImagePath);
//					System.out.println("读取的图像文件:"+zoomed);
					File f=new File(zoomed);
					try(
						InputStream fin = new FileInputStream(f);		
					){
						//上传头像
						insertPhoto.setBinaryStream(1, fin, f.length());
						insertPhoto.setString(2, QQ);
						if(insertPhoto.executeUpdate()==1){
							return 0;//信息提交成功,头像也上传成功
						}	
					}
				}//没有修改头像信息
				else{
					return 0;//信息提交成功,未修改头像
				}
			}
			return 2;//信息提交失败
		} catch (Exception e) {
			e.printStackTrace();//提交失败
			return 3;//操作异常
		}

	}
	
	private void initDatas(){
		try {
			if(conn==null || conn.isClosed()){
				initConnections();
			}
		} catch (SQLException e) {e.printStackTrace();}
		String query1 = "select * from userInfo where QQ='"+userBean.getQQ()+"'";
		try(
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query1);
		){
			qq.setText(userBean.getQQ());
			if(rs.next()){
				pwd1.setText(rs.getString(2));
				pwd2.setText(rs.getString(2));
				tel.setText(rs.getString(3));
				age.setValue(rs.getInt(4));
				email.setText(rs.getString(5));
				nickname.setText(rs.getString(6));
				if(rs.getString(7).equals("boy")){
					male.setSelected(true);
				}else{
					female.setSelected(true);
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		String query2 = "select Photo from userPhoto where QQ='"+userBean.getQQ()+"'";
		try(
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query2);
		){
			if(rs.next()){
				Blob blob = rs.getBlob(1);
				ImageIcon icon = new ImageIcon(blob.getBytes(1L, (int)blob.length()));
				photos.addItem(icon);
				photos.setSelectedItem(icon);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	//定义一个函数添加左边的JLabel
	private void addLeft(JComponent c){
		c.setFont(fzjljt);
		c.setForeground(fgColor);
		c.setPreferredSize(leftDim);
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
		if(c instanceof JPasswordField){
			JPasswordField pwd = (JPasswordField)c;
			pwd.setEchoChar('密');
		}
		
		c.setPreferredSize(rightDim);
		gbl.setConstraints(c,right);
		mainPanel.add(c);
	}
	
}
