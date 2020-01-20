package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.history;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


public class BiocoisoButtonColumn implements Serializable{
	
	private static final long serialVersionUID = 1;
	private EventListener eventListener;
	private Map<Integer, JButton> valueArray;
	private MouseListener mouseListener;
	private String icon;



	/**
	 * Constructor for the button column.
	 * 
	 * @param jTable
	 * @param column
	 * @param eventListener
	 * @param mouseListener
	 * @param interProRow
	 */
	public BiocoisoButtonColumn(JTable jTable, int column, EventListener eventListener, 
			MouseListener mouseListener, List<Integer> interProRow, String icon){
		super();
		this.icon = icon;
		this.eventListener = eventListener;
		this.mouseListener = mouseListener;
		//button.setFocusPainted( false );
		//button.setSize(new Dimension(1,1));
		TableColumnModel columnModel = jTable.getColumnModel();
		TableColumn dataColumn = columnModel.getColumn(column);
		valueArray = new TreeMap<Integer,JButton>();
		this.build(dataColumn);
	}
	

	private void build(TableColumn dataColumn){

		dataColumn.setCellRenderer(new TableCellRenderer(){
			public Component getTableCellRendererComponent(JTable table, Object value, 
					boolean isSelected, boolean hasFocus, int row, int column) {
				JButton button;
				if(valueArray.containsKey(row))
				{
					button = valueArray.get(row);
					return button;
				}
				else
				{
					if(row != -1 && row < table.getRowCount())
					{
						button = createButton(row);
						valueArray.put(row, button);
						return button;
					}
				}
				return null;
			}
		});

		dataColumn.setCellEditor(new TableCellEditor(){

			public Component getTableCellEditorComponent(JTable table, Object value, boolean flag, int row, int column) {
				this.isCellEditable(row, column);
				return valueArray.get(row);
			}
			public void addCellEditorListener(CellEditorListener arg0) {}
			public void cancelCellEditing() {}
			public Object getCellEditorValue() {return null;}
			public boolean isCellEditable(EventObject arg0) {return true;}
			public boolean isCellEditable(int row, int column) {return true;}
			public void removeCellEditorListener(CellEditorListener arg0) {}
			public boolean shouldSelectCell(EventObject arg0) {return true;}
			public boolean stopCellEditing() {return true;}	

		});

	}

	private JButton createButton(int row) {
		
		JButton button = new JButton();
		if (this.icon.equals("model"))
			button.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/cell.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
		else
			button.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/message.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
		//button.setBackground(Color.WHITE);
		addListenerToButton(button);
		addMouseListenerToButton(button);
		button.setToolTipText("More Information");
		button.setEnabled(true);
		return button;
	}

	private void addListenerToButton(JButton button){
		button.addActionListener((ActionListener) eventListener);

	}

	private void addMouseListenerToButton(JButton button){
		button.addMouseListener(mouseListener);
	}

	public int getSelectIndex(JButton button){
		for(int i : valueArray.keySet())
		{
			if(valueArray.get(i)==button)
			{
				return i;
			}
		}
		return -1;
	}

	public Map<Integer, JButton> getValueArray() {
		return valueArray;
	}

}
