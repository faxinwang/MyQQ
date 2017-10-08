package uitl;

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MyUIManager {
	private JFrame  jf= null;
	private JMenuBar jmb= null;
	//定义一个右键菜单用于设置程序风格
	private JPopupMenu pop = new JPopupMenu();
	//用于组合3个风格菜单的ButtonGroup
	private ButtonGroup flavorGroup =new ButtonGroup();
	//创建5个单选按钮,用于设置程序的外观风格
	private JRadioButtonMenuItem metal_item = new JRadioButtonMenuItem("Metal",true);
	private JRadioButtonMenuItem nimbus_item = new JRadioButtonMenuItem("Nimbus");
	private JRadioButtonMenuItem windows_item = new JRadioButtonMenuItem("Windows");
	private JRadioButtonMenuItem classic_item = new JRadioButtonMenuItem("Classic");
	private JRadioButtonMenuItem motif_item = new JRadioButtonMenuItem("Motif");

	//--------------下面开始组合右键菜单------------------//
	private void init(){
		flavorGroup.add(metal_item);
		flavorGroup.add(nimbus_item);
		flavorGroup.add(windows_item);
		flavorGroup.add(classic_item);
		flavorGroup.add(motif_item);
		pop.add(metal_item);
		pop.add(nimbus_item);
		pop.add(windows_item);
		pop.add(classic_item);
		pop.add(motif_item);
		
		add_actions();
	}
	
	private void add_actions(){
		//为右键菜单添加事件监听器
		ActionListener flavorListener = ac ->{
			try{
				switch( ac.getActionCommand() ){
				case "Metal": 	update("Metal");		break;
				case "Nimbus": 	update("Nimbus");		break;
				case "Windows": update("Windows");		break;
				case "Classic": update("Classic");		break;
				case "Motif": 	update("Motif");		break;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		};
		//为5个风格菜单项添加事件监听器
		metal_item.addActionListener(flavorListener);
		nimbus_item.addActionListener(flavorListener);
		windows_item.addActionListener(flavorListener);
		classic_item.addActionListener(flavorListener);
		motif_item.addActionListener(flavorListener);
	}
	private void update(String type)throws Exception{
		String LAF=null;
		switch(type){
		case "Metal":
			LAF="javax.swing.plaf.metal.MetalLookAndFeel";
			break;
		case "Nimbus":
			LAF="javax.swing.plaf.nimbus.NimbusLookAndFeel";
			break;
		case "Windows":
			LAF="com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			break;
		case "Classic":
			LAF="com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel";
			break;
		case "Motif":
			LAF="com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			break;
		}
		UIManager.setLookAndFeel(LAF);
		//更新jf窗口内顶级容器以及内部所有组件的UI
		if(jf!=null)
			SwingUtilities.updateComponentTreeUI(jf.getContentPane());
		//更新mbar菜单条及其内部的所有组件UI
		if(jmb!=null)
			SwingUtilities.updateComponentTreeUI(jmb);
		//更新pop右键菜单以及所有内部组件的UI
		SwingUtilities.updateComponentTreeUI(pop);
		
//		jf.pack();
	}
	
	public MyUIManager(JFrame jf,JMenuBar jmb){
		this.jf = jf;
		this.jmb = jmb;
		init();
	}
	public JPopupMenu getJPopupMenu(){
		return pop;
	}
	
	public static void setLAF(JFrame jf,JMenuBar jmb,JPopupMenu pop){
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		if(jf!=null)
			SwingUtilities.updateComponentTreeUI(jf.getContentPane());
		//更新mbar菜单条及其内部的所有组件UI
		if(jmb!=null)
			SwingUtilities.updateComponentTreeUI(jmb);
		//更新pop右键菜单以及所有内部组件的UI
		if(pop!=null)
			SwingUtilities.updateComponentTreeUI(pop);
	}
}
