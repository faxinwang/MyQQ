package uitl;

import java.awt.GraphicsEnvironment;

import javax.swing.JComboBox;

public class ListJavaFonts {
	public static String[] getFontsArray(){
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}
	
	public static JComboBox<String> getFontsCombo(){
		return new JComboBox<String>(getFontsArray());
	}
	
	public static void main(String[] args){
		for(String font :getFontsArray()){
			System.out.println(font);
		}
	}
}
