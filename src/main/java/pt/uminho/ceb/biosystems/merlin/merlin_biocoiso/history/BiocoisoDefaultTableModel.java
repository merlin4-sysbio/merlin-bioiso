package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.history;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


public class BiocoisoDefaultTableModel extends AbstractTableModel
{

	private final String[] columnNames = new String[] {
			"commit","compare models", "simulation", "select"
	};
	private final Class[] columnClass = new Class[] {
			String.class, String.class,String.class,Boolean.class
	};
	private Object[][] historyList;

	public BiocoisoDefaultTableModel(Object[][] dados2)
	{
		this.historyList = dados2;
	}

	public String getColumnName(int column)
	{
		return columnNames[column];
	}

	public Class<?> getColumnClass(int columnIndex)
	{
		return columnClass[columnIndex];
	}

	public int getColumnCount()
	{
		return columnNames.length;
	}

	public int getRowCount()
	{
		return historyList.length;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {

		if (columnIndex==1 || columnIndex==0 || columnIndex==3)
			return true;

		return false;
	}


	//	@Override
	//	public Object getValueAt(int rowIndex, int columnIndex)
	//	{
	//		Object row = historyList.get(rowIndex);
	//		if(0 == columnIndex) {
	//			return row;
	//		}
	//		else if(1 == columnIndex) {
	//			return row;
	//		}
	//		else if(2 == columnIndex) {
	//			return row;
	//		}
	//		else if(3 == columnIndex) {
	//			return row;
	//		}
	//		return null;
	//	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return historyList[rowIndex][columnIndex];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		//     super.setValueAt(aValue, rowIndex, columnIndex); by default empty implementation is not necesary if direct parent is AbstractTableModel
		historyList[rowIndex][columnIndex]= aValue; 
		fireTableCellUpdated(rowIndex, columnIndex);// notify listeners
	}

	public void removeRow(ArrayList<Integer> indexes) {

		if (historyList.length>1) {
			Object[][] result = new Object[historyList.length - indexes.size()][4];
			int j = 0;
			for (int i = 0; i<historyList.length;i++) {
				if (!indexes.contains(i)) {
					result[j]=historyList[i];
					j++;
				}
			}
			historyList = result;
		}
		else
			historyList = new Object[0][0];
		
		fireTableDataChanged();
		
	}
	
}


