package myComponents;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class MySplitPaneUI extends BasicSplitPaneUI{
	
    /**
     * Creates the overrided divider.
     */
    public BasicSplitPaneDivider createDefaultDivider() {
        return new MySplitPaneDivider(this);
    }
    
}
