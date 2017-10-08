package myComponents;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class MySplitPaneDivider extends BasicSplitPaneDivider{

	private static final long serialVersionUID = 3752532483599556451L;
	private JButton rightBn = super.createRightOneTouchButton();
	private JButton leftBn = super.createLeftOneTouchButton();
	public MySplitPaneDivider(BasicSplitPaneUI ui) {
		super(ui);
		rightBn.setEnabled(false);
		leftBn.setBackground(Color.RED);
		rightBn.setBackground(Color.CYAN);
		addActions();
	}
	
	private void addActions(){
		leftBn.addActionListener(evt ->{
			rightBn.setEnabled(true);
		});
		rightBn.addActionListener(evt ->{
			rightBn.setEnabled(false);
		});
	}
	
	protected JButton createLeftOneTouchButton(){
		return leftBn;
	}
	
	protected JButton createRightOneTouchButton(){
		return rightBn;
	}
	
	
}
