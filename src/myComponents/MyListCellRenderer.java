package myComponents;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.Timer;

import uitl.MyFileChooser;
import uitl.UserBean;

@SuppressWarnings({ "serial", "unused" })
public class MyListCellRenderer extends JPanel implements ListCellRenderer<UserBean>{
	public static final int PHOTOSIZE=MyFileChooser.PREVIEW_SIZE;
	public static final Dimension CELLSIZE = new Dimension(250,70);
	public static final int leftOffsetX = 2;
	public static final int topOffsetY = (CELLSIZE.height-PHOTOSIZE)/2;
	public static final int LabelWidth = 120;
	public static final int LabelHeight =35;
	public static final ImageIcon boy = new ImageIcon(MyListCellRenderer.class.getResource(
			"/qq/img/boy.png"));
	public static final ImageIcon girl = new ImageIcon(MyListCellRenderer.class.getResource(
			"/qq/img/girl.png"));	
	private JLabel photo = new JLabel();
	private JLabel sex = new JLabel();
	private JLabel nickname = new JLabel();
	private JLabel email = new JLabel();
	private Box box = new Box(BoxLayout.Y_AXIS);
	BufferedImage buff = new BufferedImage(PHOTOSIZE,PHOTOSIZE,
			BufferedImage.TYPE_BYTE_GRAY);//创建灰度图
	Graphics g = buff.getGraphics();
	
	HashSet<String> onlineList ;
	UserBean user;
	private final Color focusColor = new Color(200,90,230,200);
	Font hydytj = new Font("汉仪蝶语体简",Font.ROMAN_BASELINE,15);
	Font fzjljt = new Font("方正静蕾简体",Font.LAYOUT_LEFT_TO_RIGHT,24);

	public MyListCellRenderer(HashSet<String> online){
		super();
		this.onlineList = online;
		
		this.setDoubleBuffered(true);
		initLayouts();
		nickname.setFont(fzjljt);
		email.setFont(hydytj);
	}
	private void initLayouts(){
		this.add(photo);
		box.add(nickname);
		email.setPreferredSize(new Dimension(130,30));
		box.add(email);
		this.add(box);
		this.add(sex);
	}
	
	private void setValues(UserBean user){
		if(!onlineList.contains(user.getQQ())){
			//将不在线人的QQ头像转换为灰度图
			g.drawImage(user.getPhoto().getImage(), 0, 0, null);
			photo.setIcon(new ImageIcon(buff));
		}else{
			photo.setIcon(user.getPhoto());
		}
		
		//如果昵称不为空,就显示昵称,如果为空就在昵称的位置显示QQ号
		if(user.getNickname()!=null){
			nickname.setText(user.getNickname());
		}else{
			nickname.setText(user.getQQ());
		}
		email.setText(user.getEmail());

		sex.setIcon("boy".equals(user.getSex())?boy:girl);
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends UserBean> list, UserBean value, int index,
			boolean isSelected, boolean cellHasFocus) {
		if(value==null){
			return null;
		}
		
		setValues(value);
		
//		System.out.println(isSelected?index+" is selected":index+" is not selected");
//		System.out.println(cellHasFocus?index+" cell Has Focus":index+" cell Has No Focus");
		
		if(isSelected){
			nickname.setForeground(Color.RED);
			email.setForeground(Color.ORANGE);
			this.setBackground(list.getSelectionBackground());
		}else{
			nickname.setForeground(Color.black);
			email.setForeground(Color.black);
			this.setBackground(list.getBackground());
		}

		if(cellHasFocus){
			this.setBackground(focusColor);
		}else{
			this.setBackground(list.getBackground());
		}
		
//        this.repaint();
        list.repaint();
		return this;
	}
	

	public Dimension getPreferredSize(){
		return CELLSIZE;
	}
}


