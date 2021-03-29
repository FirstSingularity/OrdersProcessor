package processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Order implements Runnable{

	private int id;
	private File orderFile;
	private String order = "";
	private HashMap<String, Double> prices;
	private TreeMap<String, Integer> orderSum;
	private Lock lock;

	public Order(File orderFile, HashMap<String, Double> prices, Lock lock) {
		this.prices = prices;
		this.orderFile = orderFile;
		this.lock = lock;
	}
	
	public void run() {
		constructOrder();
		System.out.println("Reading order for client with id: " + this.id);
		String answer = getSummary();
		synchronized(this.lock){
			lock.getLockSum().add(answer);
		}
		updateQuant();
		synchronized(this.lock){
			lock.setGrandTotal(lock.getGrandTotal() + getOrderTotal());
		}
	}

	public void constructOrder() {
		try {
			Scanner fileReader = new Scanner(orderFile);

			while (fileReader.hasNextLine()) {
				this.order += fileReader.nextLine() + "\n";
			}
			fileReader.close();
			this.order = this.order.substring(0, this.order.length() - 1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		this.orderSum = new TreeMap<String, Integer>();

		String[] orderList = this.order.split("\n");

		for (int i = 1; i < orderList.length; i++) {
			if (!orderSum.containsKey(orderList[i].split(" ")[0])) {
				orderSum.put(orderList[i].split(" ")[0], 1);
			} else {
				orderSum.put(orderList[i].split(" ")[0], orderSum.get(orderList[i].split(" ")[0]) + 1);
			}
		}
		
		this.id = Integer.parseInt(this.order.split("\n")[0].substring(this.order.split("\n")[0].length() - 4,
				this.order.split("\n")[0].length()));
	}

	public TreeMap<String, Integer> getOrderQuant() {
		return this.orderSum;
	}

	public double getOrderTotal() {
		double orderTotal = 0;

		for (String s : orderSum.keySet()) {
			orderTotal += prices.get(s) * orderSum.get(s);
		}

		return orderTotal;
	}

	public String getSummary() {
		String answer = "----- Order details for client with Id: " + this.id + " -----";

		for (String s : orderSum.keySet()) {
			answer += "\nItem's name: " + s + ", Cost per item: "
					+ NumberFormat.getCurrencyInstance().format(prices.get(s)) + ", Quantity: " + orderSum.get(s)
					+ ", Cost: " + NumberFormat.getCurrencyInstance().format(prices.get(s) * orderSum.get(s));
		}

		answer += "\nOrder Total: " + NumberFormat.getCurrencyInstance().format(getOrderTotal()) + "\n";

		return answer;
	}
	
	public int getId() {
		return this.id;
	}

	public void updateQuant() {
		for(String s : this.orderSum.keySet()) {
			synchronized(this.lock) {
				if(!this.lock.getQuant().containsKey(s)) {
					this.lock.getQuant().put(s, this.orderSum.get(s));
				}else {
					this.lock.getQuant().put(s, this.lock.getQuant().get(s) + this.orderSum.get(s));
				}
			}
		}
	}
}
