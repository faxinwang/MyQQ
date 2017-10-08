package uitl;

import javax.swing.ImageIcon;

public class UserBean {
	private String QQ;
	private ImageIcon photo;
	private String nickname;
	private String Sex;
	private String Email;
	
	public UserBean(String qq,String name,String sex,String email,ImageIcon icon){
		QQ = qq;
		nickname = name;
		Sex = sex;
		Email = email;
		photo = icon;
	}
	public String getQQ(){
		return QQ;
	}
	public void setQQ(String qq){
		QQ = qq;
	}

	public void setPhoto(ImageIcon img){
		photo = img;
	}
	public ImageIcon getPhoto(){
		return photo;
	}
	public void setNickname(String name){
		this.nickname =name; 
	}
	public String getNickname(){
		return nickname;
	}

	public String getSex() {
		return Sex;
	}

	public void setSex(String sex) {
		Sex = sex;
	}
	
	public void setEmail(String mail){
		Email = mail;
	}
	public String getEmail(){
		return Email;
	}
}
