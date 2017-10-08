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
	Font hydytj = new Font("���ǵ������",Font.ROMAN_BASELINE,22);
	Font fzjljt = new Font("�������ټ���",Font.BOLD,30);
	
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
		//���ò��ֹ�����ΪGridBagLayout
		mainPanel.setLayout(gbl);
		Insets inset = new Insets(10,20,10,20);
		left.insets = inset;
		right.insets = inset;
		left.gridx = 0; right.gridx = 1;
		left.weightx = 1; right.weightx = 2;
//		left.gridwidth = 2; right.gridwidth = 1;
//		left.fill=GridBagConstraints.HORIZONTAL;
		right.fill=GridBagConstraints.HORIZONTAL;	//�ұߵ������ˮƽ��չ
		left.anchor = GridBagConstraints.CENTER;	//���ߵ�������������������ʾ
		right.anchor = GridBagConstraints.CENTER;
		
		//����QQ�����ʽ�ı���
		String qqRegex = "[a-zA-Z0-9]{6,10}";
		RegexFormatter qqFormatter = new RegexFormatter(qqRegex);
		qqFormatter.setOverwriteMode(false);
		qqField = new JFormattedTextField(qqFormatter);
		
		//��Tel�ĸ�ʽ�ı���
		try {
			MaskFormatter formatter = new MaskFormatter("+86 1## #### ####");
			formatter.setPlaceholderCharacter('��');
			telField = new JFormattedTextField(formatter);
		} catch (ParseException e) {e.printStackTrace();}
		
		//����email�ĸ�ʽ�ı���
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
		//��������������
		mainWin.setTitle("�һ�����");
		mainWin.setIconImage(qqIcon.getImage());
		//���ùرմ���ʱ���ٸô���,�����˳�����
		mainWin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		mainWin.setBounds((screen.width - WIDTH)/2,(screen.height - HEIGHT)/2,WIDTH,HEIGHT);
		mainWin.setResizable(false);
		mainWin.setVisible(true);
		qqLabel.setToolTipText("����Ҫ�һ������QQ!");
		telLabel.setToolTipText("����ע��ʱ��д�ĵ绰����!");
		emailLabel.setToolTipText("����ע��ʱ��д�������ַ!");
		mainPanel.setToolTipText("�����Ҽ��ɸ���Ƥ��!");
	}
	
	private void initActions(){
		//Ϊ������Ӵ����¼�������,���رոô���ʱ,�򿪵�¼����,���ر����ݿ�����
		mainWin.addWindowListener( new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				MySQL.closeRes(null, ps, null);
				if(login!=null)
					login.setVisible(true);
			}
		});
		
		submit.addActionListener(evt ->{
			//���QQ������Ƿ�Ϊ��
			String QQ = qqField.getText();
			if(QQ.length()==0){
				JOptionPane.showMessageDialog(qqField, "������Ҫ�һ������QQ����!","����Ƿ�",
							JOptionPane.ERROR_MESSAGE);
				return;
			}
			//���Tel�Ƿ�������ȷ
			String Tel = telField.getText().substring(4);	//ȥ����ͷ��"+86 "
			if( Tel.indexOf('��')>=0 ){
				JOptionPane.showMessageDialog(telField, "�����������ĵ绰����!","����Ƿ�",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			//���Email��д�Ƿ���ȷ
			String Email = emailField.getText();
			if( Email.equals("abc@qq.com") ){
				JOptionPane.showMessageDialog(emailField, "��������ȷ�������ַ!","����Ƿ�",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			/*
			 * ����,������д����ȷ 
			 */
			
			short rst = findPwd(QQ,Tel,Email);
			if(rst==0){
				//�����һسɹ�
				JOptionPane pane = new JOptionPane("��ϲ! ����ɹ��һ�!\n"+
						"����QQ��: "+QQ+" ��Ӧ������Ϊ:\n\t"+PWD+
						"\n�����Ʊ���!��������Ŷ!",					//message
						JOptionPane.INFORMATION_MESSAGE,		//messageType
						JOptionPane.DEFAULT_OPTION,				//optionType
						qqIcon);								//Icon
				pane.setFont(hydytj);
				pane.setForeground(fgColor);
				JDialog dialog = pane.createDialog("�����һسɹ�");//title
				//��ʾ�Ի���
				dialog.setVisible(true);
				//���ٱ�����
				mainWin.dispose();
				MySQL.closeRes(null, ps, null);
				//��ʾ��½����
				if(login!=null)
					login.setVisible(true);
			}else if(rst==1){
				JOptionPane.showMessageDialog(mainWin, "û���ҵ����˺Ŷ�Ӧ������!\n"+
								"��ȷ�� QQ,Tel,Email ��д�Ƿ���ȷ!","�������ʧ��",
								JOptionPane.ERROR_MESSAGE);
			}else if(rst==2){
				JOptionPane.showMessageDialog(mainWin, "�������ݿ�ʧ��!\n"+
						"���Ժ�����!","�������ʧ��",
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
	
	//����һ�����������ߵ�JLabel
	private void addLeft(JComponent c){
		c.setFont(fzjljt);
		c.setForeground(fgColor);
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
		
		gbl.setConstraints(c,right);
		mainPanel.add(c);
	}
	
	private short findPwd(String QQ,String Tel,String Email){
		initConnections();
		
		//��ѯ���ݿ�
		try {
			ps.setString(1, QQ);
			ps.setString(2, Tel);
			ps.setString(3, Email);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				PWD = rs.getString(2);
				return 0;
			}
			return 1;	//û���ҵ������������˺�����
		} catch (SQLException e) {
			e.printStackTrace();
			return 2;//�������ݿ��쳣
		}
	}

	public static void main(String[] args){
		new FindPwd(null,null);
	}
	
}

