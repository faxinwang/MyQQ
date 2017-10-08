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
	//�����ڵĴ�С
	static final int WIDTH = 650;
	static final int HEIGHT = 650;
	//����½���û�����Ϣ���ݽ���
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
	//��ʼͷ��Ϊ���
	ImageIcon qqIcon = new ImageIcon(ModifyInfo.class.getResource("QQIMG/0.png"));
	String choosedImagePath=null;
	JComboBox<ImageIcon> photos = MyComboBox.getImageJComboBox();
	JButton borwser = new JButton("ѡ���ϴ�ͷ��");
	MyFileChooser imgChooser = new MyFileChooser();
	
	GridBagLayout gbl= new GridBagLayout();
	GridBagConstraints left = new GridBagConstraints();
	GridBagConstraints right = new GridBagConstraints();
	
	Color bgColor = new Color(200,90,230,80);
	Color fgColor = new Color(200,90,200);
	Dimension leftDim = new Dimension(120,35);
	Dimension rightDim = new Dimension(80,35);
	Font hydytj = new Font("���ǵ������",Font.ROMAN_BASELINE,22);
	Font fzjljt = new Font("�������ټ���",Font.BOLD,22);
	JLabel[]  labels = new JLabel[8];
	
	JButton submit = new JButton("�ύ");
	JButton reset = new JButton("����");
	
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
		//���ò��ֹ�����ΪGridBagLayout
		mainPanel.setLayout(gbl);
		Insets inset = new Insets(10,20,10,20);
		left.insets = inset;
		right.insets = inset;
		left.gridx = 0; right.gridx = 1;
		left.weightx = 1; right.weightx = 1;
//		left.gridwidth = 2; right.gridwidth = 1;
//		left.fill=GridBagConstraints.HORIZONTAL;
		right.fill=GridBagConstraints.HORIZONTAL;	//�ұߵ������ˮƽ��չ
		left.anchor = GridBagConstraints.CENTER;	//���ߵ�������������������ʾ
		right.anchor = GridBagConstraints.CENTER;
		
		labels[0] = new JLabel("*����QQ:");
		labels[1] = new JLabel("*��������:");
		labels[2] = new JLabel("*ȷ������:");
		labels[3] = new JLabel("*�ֻ�����:");
		labels[4] = new JLabel("*��������:");
		labels[5] = new JLabel("*����email:");
		labels[6] = new JLabel(" �����ǳ�:");
		labels[7] = new JLabel(" ѡ���Ա�:");
		//����8����ǩ�ŵ�mainPanel��
		for(int i=0;i<8;++i){
			//���ñ�ǩ��������
			left.gridy = i;
			addLeft(labels[i]);
		}
		//����QQ�����ʽ�ı���
		String qqRegex = "[a-zA-Z0-9]{6,10}";
		RegexFormatter qqFormatter = new RegexFormatter(qqRegex);
		qqFormatter.setOverwriteMode(false);
		qq = new JFormattedTextField(qqFormatter);
		
		//�����绰����ĸ�ʽ�ı���
		try {
			MaskFormatter formatter = new MaskFormatter("+86 1## #### ####");
			formatter.setPlaceholderCharacter('��');
			tel = new JFormattedTextField(formatter);
		} catch (ParseException e) {e.printStackTrace();}
		//����email�ĸ�ʽ�ı���
		String mailRegex = "(((\\w)|(\\.)){3,20}@\\w+\\.(com|cn|org|net|gov))";
		RegexFormatter emailFormatter = new RegexFormatter(mailRegex);
		emailFormatter.setOverwriteMode(false);
		email = new JFormattedTextField(emailFormatter);
		email.setValue("abc@qq.com");
		
		
		//����������mainPanel��
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
		
		//���ͷ���ϴ�����
		left.gridy = 8;
		gbl.setConstraints(photos, left);
		mainPanel.add(photos);
		
		right.gridy = 8;	
		addRight(borwser);
		
		
		//����ύ�����ð�ť��mainPanel��
		left.gridy = 9; 	addLeft(submit);
		right.gridy= 9;		addRight(reset);
		//��mainPanel������������
		mainWin.add(mainPanel);
	}
	
	private void initSettings(){
		//���ô�������
		mainWin.setIconImage(qqIcon.getImage());
		mainWin.setTitle("�޸���Ϣ");
		mainWin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		mainWin.setBounds((screen.width - WIDTH)/2, (screen.height - HEIGHT)/2, WIDTH, HEIGHT);
		mainWin.setResizable(false);
		mainWin.setVisible(true);
		
		//���ñ�ǩ��ʾ����
		labels[0].setToolTipText("QQ��ֻ�ܰ������ֺ�Ӣ����ĸ,���Ȳ�С��6λ,������10λ."
				+ "�����Ͻ�ע��̺�,����һ��ӵ��6λ��QQ�ĸо�!");
		labels[1].setToolTipText("����������,���Ȳ�С��6λ,������10λ!");
		labels[2].setToolTipText("���Ǻŵ�Ϊ������!");
		labels[3].setToolTipText("��������ȷ�ĵ绰����,���ں�QQ��,�����ʼ�һ���һ�����!");
		labels[4].setToolTipText("������һ��������!");
		labels[5].setToolTipText("��������ȷ�������ַ,���ں�QQ��,�绰����һ���һ�����!");
		//���ñ���ɫ���Ҽ��˵�
		mainPanel.setBackground(bgColor);
		mainPanel.setComponentPopupMenu(new MyUIManager(mainWin,null).getJPopupMenu());
		mainPanel.setToolTipText("����Ҽ��ɸ����������!");
		//����photos(JBomboBox)��ʼѡ���һ��ͷ��
		photos.setSelectedIndex(0);
		photos.setToolTipText("��ѡ���б��е�ͷ������ұ߰�ť�ϴ��Զ���ͷ��!");
		//����qq�ı��򲻿ɱ༭,ǰ��ɫΪ��ɫ
		qq.setEditable(false);
		qq.setForeground(Color.BLUE);
	}
	
	private void initActions(){
		
		//Ϊ��������¼�������,���رձ�����ʱ,�򿪵�¼����
		mainWin.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				//close statements
				MySQL.closeRes(null, insertInfo, null);
				MySQL.closeRes(null, insertPhoto, null);
				//��ʾ��½����
				if(login!=null)
					login.setVisible(true);
			}
		});
		//Ϊͷ��ѡ��ť����¼�������
		borwser.addActionListener( evt ->{
			String path = imgChooser.chooseImage();
			if(path != null){
				ImageIcon icon =  new ImageIcon(path);
				//���ͷ��̫��,ѹ��ͷ��ָ����С
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
		
		//Ϊphotos(JComboBox<ImageIcon>)���ѡ����,���û�ѡ����ͷ��ʱ,����choosedImagePath
		photos.addActionListener(evt ->{
			int index = photos.getSelectedIndex();
			if(0<=index && index<=24){
				File curDir = new File(".");
				String parent = curDir.getAbsolutePath();
				choosedImagePath=parent.substring(0,parent.length()-1)
						+"src\\qq\\QQIMG\\"+index+".png";
			}
		});
		
		//Ϊ���ð�ť����¼�������
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
			photos.setSelectedIndex(0);//��һ��item�±�Ϊ0
			choosedImagePath=null;
		});
		//Ϊ�ύ��ť����¼�������
		submit.addActionListener(evt ->{
			
			if(qq.getText().length()==0){
				JOptionPane.showMessageDialog(qq, "QQ�˺Ų���Ϊ��!������6~10λ�����ֻ���ĸ��ɵ�QQ!","QQ�Ƿ�",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String pass1 = new String(pwd1.getPassword());
			if(pass1.length()<6){
				JOptionPane.showMessageDialog(pwd1, "���볤�Ȳ���С��6λ!","����Ϊ��",
						JOptionPane.ERROR_MESSAGE);
				return;
			}else if(pass1.length()>16){
				JOptionPane.showMessageDialog(pwd1, "���볤�Ȳ��ܴ���16λ!","���볤�ȳ���",
						JOptionPane.ERROR_MESSAGE);
				return;				
			}
			
			String pass2 = new String(pwd2.getPassword());
			if(!pass2.equals(pass1)){
				JOptionPane.showMessageDialog(pwd2, "�������벻һ��,����������!","���벻һ��",
						JOptionPane.ERROR_MESSAGE);
				return;	
			}
			
			String Tel=null;
			try {
				Tel = tel.getText(4, 13);
			} catch (Exception e) {e.printStackTrace();}
			if(Tel.indexOf('��') >=0){
				JOptionPane.showMessageDialog(tel, "�����������ĵ绰����!","�绰����Ϊ��",
						JOptionPane.ERROR_MESSAGE);
				return;	
			}
			
			//δ��������
			if(age.getText().length()==0){
				JOptionPane.showMessageDialog(age, "����������!","����Ϊ��",
						JOptionPane.ERROR_MESSAGE);
				return;	
			}else{
				try{
					//����Ϊ����,���Ϸ�
					if(Integer.valueOf(age.getText().trim()) < 0){
						JOptionPane.showMessageDialog(age, "���䲻��Ϊ����!","����Ƿ�",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				//�����쳣˵���������䳬����1000
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(age, "��Ѿ�Ǵ�Խ��������?��ô��!","����Ƿ�",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

			}
			
			if(email.getText().equals("abc@qq.com")){
				JOptionPane.showMessageDialog(email, "��������ȷ�������ַ!","����Ϊ��",
						JOptionPane.ERROR_MESSAGE);
				return;	
			}
			
			/*
			 * ����,�����������ݾ�����!
			 * ��ʼ�ύע����Ϣ.
			 */
			short rst = updateInfo(); 
			if(rst==0){
				//ע��ɹ�
				JOptionPane pane = new JOptionPane("��Ϣ�Ѿ��ɹ�����!\n"+
						"����QQ��Ϊ : "+userBean.getQQ()+"\n����Ϊ : "+new String(pwd1.getPassword())+
						"\n�����Ʊ���! ����������,��ʹ�� Tel,QQ,Email �һ�!",	//message
						JOptionPane.INFORMATION_MESSAGE,		//messageType
						JOptionPane.DEFAULT_OPTION,				//optionType
						qqIcon);								//Icon
				pane.setFont(hydytj);
				pane.setForeground(fgColor);
				JDialog dialog = pane.createDialog("�޸���Ϣ");
				dialog.setVisible(true);
				//���ٸô���
				mainWin.dispose();
				//�ر����ݿ�������Դ
				MySQL.closeRes(null, insertInfo, null);
				//��ʾ��½����
				if(login!=null)
					login.setVisible(true);
//				new Login();
			}else if(rst==1){
				JOptionPane.showMessageDialog(mainWin, "�绰���벻��ȷ!","����Ƿ�",
						JOptionPane.ERROR_MESSAGE);
			}else if(rst==2){
				//�������ݿ��쳣,ע��ʧ��
				JOptionPane.showMessageDialog(mainWin, "��Ϣ�ύʧ��,���Ժ�����!","�޸�ʧ��",
						JOptionPane.ERROR_MESSAGE);
			}else if(rst==3){
				JOptionPane.showMessageDialog(mainWin, "����δ֪�쳣,���Ժ�����!","�޸�ʧ��",
						JOptionPane.ERROR_MESSAGE);
			}
			
		});
	}
	
	private void initConnections(){
		//������ݿ�ر���,���»�ȡ���ݿ�����
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
			//���statement�ر�,���»�ȡstatement
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
		//��ȡ������Ϣ
		String QQ = userBean.getQQ();
		String Pwd = new String(pwd1.getPassword());
		String Tel=null;
		try {
			Tel = tel.getText(4,13);
		} catch (BadLocationException e){
			e.printStackTrace();
			return 1;//�绰���벻��ȷ,һ�㲻�ᷢ��
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
		//�鿴���ݿ������Ƿ�ɹ�
		try {
			if(conn==null || conn.isClosed()){
				return 2;//�������ݿ�ʧ��
			}
		} catch (SQLException e1) {e1.printStackTrace();}

		
		try{
			//����Զ�����ݿ��ύ��Ϣ
			insertInfo.setString(1, Pwd);
			insertInfo.setString(2, Tel);
			insertInfo.setInt(3, Age);
			insertInfo.setString(4, Email);
			insertInfo.setString(5, Nickname);
			insertInfo.setString(6, Sex);
			insertInfo.setString(7, QQ);

			//���ϴ��ı���Ϣ
			if(insertInfo.executeUpdate()==1){
				//����޸���ͷ��,�ϴ�ͷ����Ϣ
				if(choosedImagePath!=null){
					ZoomImage zoom = new ZoomImage(MyFileChooser.PREVIEW_SIZE,
							MyFileChooser.PREVIEW_SIZE);
					String zoomed = zoom.zoomImage(choosedImagePath);
//					System.out.println("��ȡ��ͼ���ļ�:"+zoomed);
					File f=new File(zoomed);
					try(
						InputStream fin = new FileInputStream(f);		
					){
						//�ϴ�ͷ��
						insertPhoto.setBinaryStream(1, fin, f.length());
						insertPhoto.setString(2, QQ);
						if(insertPhoto.executeUpdate()==1){
							return 0;//��Ϣ�ύ�ɹ�,ͷ��Ҳ�ϴ��ɹ�
						}	
					}
				}//û���޸�ͷ����Ϣ
				else{
					return 0;//��Ϣ�ύ�ɹ�,δ�޸�ͷ��
				}
			}
			return 2;//��Ϣ�ύʧ��
		} catch (Exception e) {
			e.printStackTrace();//�ύʧ��
			return 3;//�����쳣
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
	
	//����һ�����������ߵ�JLabel
	private void addLeft(JComponent c){
		c.setFont(fzjljt);
		c.setForeground(fgColor);
		c.setPreferredSize(leftDim);
		gbl.setConstraints(c, left);
		mainPanel.add(c);
	}
	//����һ����������ұߵ�����ؼ�
	private void addRight(JComponent c){
		c.setFont(hydytj);
		c.setForeground(fgColor);
		if(c instanceof JTextField){
			JTextField jtf = (JTextField)c;
			jtf.setHorizontalAlignment(JTextField.CENTER);
		}
		if(c instanceof JPasswordField){
			JPasswordField pwd = (JPasswordField)c;
			pwd.setEchoChar('��');
		}
		
		c.setPreferredSize(rightDim);
		gbl.setConstraints(c,right);
		mainPanel.add(c);
	}
	
}
