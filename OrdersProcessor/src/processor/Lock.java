package processor;

import java.util.ArrayList;
import java.util.TreeMap;

public class Lock {

	private ArrayList<String> lockSum = new ArrayList<String>();
	private double grandTotal = 0;
	private TreeMap<String, Integer> quant = new TreeMap<String, Integer>();
	
	public TreeMap<String, Integer> getQuant() {
		return quant;
	}
	public double getGrandTotal() {
		return grandTotal;
	}
	public void setGrandTotal(double newTotal) {
		grandTotal = newTotal;
	}
	public ArrayList<String> getLockSum() {
		return lockSum;
	}
	
	
}
