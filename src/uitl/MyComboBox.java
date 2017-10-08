package uitl;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;

public class MyComboBox {
	//返回以NumberListModel为model的JComboBox
	public static JComboBox<Integer> getJComboBox(int start,int end,int step){
		return new JComboBox<Integer>(new NumberComboBoxModel(start,end,step));
	}
	
	public static  JComboBox<ImageIcon> getImageJComboBox(){
		JComboBox<ImageIcon> combo = new JComboBox<>();
		for(int i=0;i<=24;++i){
			combo.addItem(new ImageIcon(
					MyComboBox.class.getResource("/qq/QQIMG/"+i+".png")));
		}
		return combo;
	}
	
}

@SuppressWarnings("serial")
class NumberListModel extends AbstractListModel<Integer>{
	protected Integer start;
	protected Integer end;
	protected Integer step;
	public NumberListModel(int start,int end,int step){
		if(start <= end){
			this.start = start;
			this.end = end;
			this.step = step;
		}
	}
	
	@Override
	public int getSize() {
		return (end-start)/step;
	}

	@Override
	public Integer getElementAt(int index) {
		return step*index+start;
	}
}

@SuppressWarnings("serial")
class NumberComboBoxModel extends NumberListModel implements ComboBoxModel<Integer>{
	private int selectedId=0;
	
	public NumberComboBoxModel(int start, int end, int step) {
		super(start, end, step);
	}
	
	//设置选中项
	@Override
	public void setSelectedItem(Object anItem) {
		if(anItem instanceof Integer){
			Integer target = (Integer)anItem;
			selectedId = (target-start)/step;
		}
	}

	@Override
	public Object getSelectedItem() {
		return selectedId*step+start;
	}
}
