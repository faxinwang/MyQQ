package uitl;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;



public class MyFileChooser {
	String src ="./res/img/";
	//����ͼƬԤ������Ĵ�С
	public static final int PREVIEW_SIZE = 60;
	JLabel accessory = new JLabel();
	JFileChooser chooser = new JFileChooser();
	//�����ļ�������
	ExtensionFileFilter filter = new ExtensionFileFilter();
	
	public MyFileChooser(){
		//----------���濪ʼ��ʼ��JFileChooser�������--------------//
		filter.addExtension(".jpg");
		filter.addExtension(".jpeg");
		filter.addExtension("gif");
		filter.addExtension(".png");
		filter.setDescription("ͼƬ�ļ�(*.jpg,*jpeg,*.gif,*png)");
		//��ֹ"�ļ�����"�����б�����ʾ"�����ļ�"ѡ��
		chooser.setAcceptAllFileFilterUsed(false);
		//Ϊ�ļ�ѡ����ָ���Զ����FileView����
		chooser.setFileView(new FileIconView(filter));
		//�����ļ�������
		chooser.setFileFilter(filter);
		//����ѡ��ģʽΪֻ��ѡ���ļ�
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		//����ֻ��ѡ�񵥸��ļ�
		chooser.setMultiSelectionEnabled(false);
		//Ϊ�ļ�ѡ����ָ��һ��Ԥ��ͼƬ�ĸ���(JComponent)
		chooser.setAccessory(accessory);
		//����Ԥ��ͼƬ�Ĵ�С�ͱ߿�
		accessory.setPreferredSize(new Dimension(PREVIEW_SIZE,PREVIEW_SIZE));
		accessory.setBorder(BorderFactory.createEtchedBorder());
		
		//���ڼ�ⱻѡ���ļ��ĸı��¼�
		chooser.addPropertyChangeListener(event ->{
			//JFileChooser�ı�ѡ�ļ��Ѿ������˸ı�
			if(event.getPropertyName()==JFileChooser.SELECTED_FILE_CHANGED_PROPERTY){
				//��ȡ�û�ѡ����ļ�
				File f = (File)event.getNewValue();
				if(f==null){
					accessory.setIcon(null);
					return ;
				}
				//����ѡ�ļ�����ImageIcon��
				ImageIcon icon = new ImageIcon(f.getPath());
				System.out.println(f.getPath());
				//���ͼ��̫��,��ѹ����
				if(icon.getIconWidth() > PREVIEW_SIZE){
					icon = new ImageIcon(icon.getImage()
							.getScaledInstance(PREVIEW_SIZE,PREVIEW_SIZE, Image.SCALE_SMOOTH));
				}
				//�ı�accessory Label��ͼ��
				accessory.setIcon(icon);
			}
		});
	}
	
	public String chooseImage(){
		//�����ļ��Ի���ĵ�ǰ·��
		chooser.setCurrentDirectory(new File("."));
		//��ʾ�ļ��Ի���
		int result = chooser.showDialog(null, "��ͼƬ�ļ�");
		//����û�ѡ����APPROVE��ť,�������,����ȵİ�ť
		if(result==JFileChooser.APPROVE_OPTION){
			String name = chooser.getSelectedFile().getPath();
			System.out.println(name);
			return name;
		}
		return null;
	}
	
	
	
	//����һ��FileView��,����Ϊָ�����͵��ļ����ļ�������ͼ��
	class FileIconView extends FileView{
		private FileFilter filter;
		public FileIconView(FileFilter filter){
			this.filter = filter;
		}
		//��д�÷���,Ϊ�ļ���,�ļ�����ͼ��
		public Icon getIcon(File f){
			//�����filter���ܵ��ļ�,����img.pngͼ����ʾ
			if(!f.isDirectory() && filter.accept(f)){
				return new ImageIcon(src+"img.png");
			}
			else if(f.isDirectory()){
				//��ȡ���и�·��
				File[] roots = File.listRoots();
				for(File rt:roots){
					//���f�Ǹ�·��
					if(rt.equals(f)){
						//�����Ӳ��,����dsk.pngͼ����ʾ
						return new ImageIcon(src+"dsk.png");
					}
				}
				//�������ͨ�ļ���,����folder.pngͼ����ʾ
				return new ImageIcon(src+"folder.png");
			}
			//ʹ��Ĭ��ͼ��
			return null;
		}
	}
	
	//����FileFilter������,����ʵ���ļ����˹���
	class ExtensionFileFilter extends FileFilter{
		private String description;
		private ArrayList<String> extensions = new ArrayList<>();
		//�Զ��巽��,��������ļ���չ��
		public void addExtension(String extension){
			if(!extension.startsWith(".")){
				extension= "." + extension;
			}
			extensions.add(extension);
		}
		//�жϸ��ļ��������Ƿ����ָ�����ļ�
		@Override
		public boolean accept(File f) {
			//�����ļ���
			if(f.isDirectory()) return true;
			//�����ļ�����Сд�Ƚ��ļ�
			String name = f.getName().toLowerCase();
			//�������пɽ��ܵ���չ�ļ���,����������ļ�������չ�ļ���,˵�����ļ��ɽ���
			for(String ext : extensions){
				if(name.endsWith(ext)){
					return true;
				}
			}
			return false;
		}
		
		public void setDescription(String desc){
			description = desc;
		}
		
		//�������ø��ļ��������������ı�
		@Override
		public String getDescription() {
			return description;
		}
	}
}
