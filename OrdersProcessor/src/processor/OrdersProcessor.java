package processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class OrdersProcessor {
	
	public static void main(String[] args) {
		HashMap<String, Double> itemsData = new HashMap<String, Double>();
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Enter item's data file name: ");
		File itemsDataFile = new File(scanner.next());
		Scanner fileReader;
		try {
			fileReader = new Scanner(itemsDataFile);
			String itemsDataString = "";
			while(fileReader.hasNextLine()) {
				itemsDataString += fileReader.nextLine() + "\n";
			}
			
			String[] brokenOrder = itemsDataString.split("\n");
			for(String s : brokenOrder) {
				itemsData.put(s.split(" ")[0], Double.parseDouble(s.split(" ")[1]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("Enter 'y' for multiple threads, any other character otherwise: ");
		String threadChoice = scanner.next();
		
		System.out.println("Enter number of orders to process: ");
		int numOfOrders = scanner.nextInt();
		
		System.out.println("Enter order's base filename: ");
		String base = scanner.next();

		File[] orders = new File[numOfOrders];
		File dir = new File(".");
		
		ArrayList<File> possibleOrders = new ArrayList<File>();
		for(File f : dir.listFiles()) {
			if(f.toString().contains(base)) {
				possibleOrders.add(f);
			}
		}
		TreeMap<Integer, File> sortedData = sortData(possibleOrders, base);
		
		int i = 0;
		for(File f : sortedData.values()) {
			orders[i] = f;
			i++;
			if(i == orders.length) {
				break;
			}
		}
		
		
		System.out.println("Enter result's filename: ");
		File results = new File(scanner.next());
		
		scanner.close();
		
		long startTime = System.currentTimeMillis();
		
		if(threadChoice.equals("y")) {
			multiThread(itemsData, orders, results);
		}else {
			singleThread(itemsData, orders, results);
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Processing time (msec): " + (endTime - startTime));
		System.out.println("Results can be found in the file: " + results.toString());
	}
	
	private static void singleThread(HashMap<String, Double> prices, File[] orders, File results) {
		String toFile = "";
		Lock lock = new Lock();
		Order[] orderThreads = new Order[orders.length];
		
		for(int i = 0; i < orderThreads.length; i++) {
			Order o = new Order(orders[i], prices, lock);
			o.run();
		}
		
		Collections.sort(lock.getLockSum());
		
		for(String s : lock.getLockSum()) {
			toFile += s;
		}
		
		toFile += "***** Summary of all orders *****\n";
		
		for(String s : lock.getQuant().keySet()) {
			toFile += "Summary - Item's name: " + s + ", Cost per item: " + NumberFormat.getCurrencyInstance().format(prices.get(s)) + ", Number sold: " + lock.getQuant().get(s) + ", Item's Total: " + NumberFormat.getCurrencyInstance().format(prices.get(s) * lock.getQuant().get(s)) + "\n";
		}
		
		toFile += "Summary Grand Total: " + NumberFormat.getCurrencyInstance().format(lock.getGrandTotal()) + "\n";
		
		String resultsName = results.toString();
		results.delete();
		File emptyResults = new File(resultsName);
		try {
			Files.writeString(Paths.get(emptyResults.toString()), toFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void multiThread(HashMap<String, Double> prices, File[] orders, File results) {
		String toFile = "";
		Lock lock = new Lock();
		Order[] orderThreads = new Order[orders.length];
		Thread[] threads = new Thread[orders.length];
		for(int i = 0; i < orderThreads.length; i++) {
			Order o = new Order(orders[i], prices, lock);
			orderThreads[i] = o;
		}
		
		for(int i = 0; i < orderThreads.length; i++) {
			threads[i] = new Thread(orderThreads[i]);
			threads[i].start();
		}
		
		for(Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Collections.sort(lock.getLockSum());
		
		for(String s : lock.getLockSum()) {
			toFile += s;
		}
		
		toFile += "***** Summary of all orders *****\n";
		
		for(String s : lock.getQuant().keySet()) {
			toFile += "Summary - Item's name: " + s + ", Cost per item: " + NumberFormat.getCurrencyInstance().format(prices.get(s)) + ", Number sold: " + lock.getQuant().get(s) + ", Item's Total: " + NumberFormat.getCurrencyInstance().format(prices.get(s) * lock.getQuant().get(s)) + "\n";
		}
		
		toFile += "Summary Grand Total: " + NumberFormat.getCurrencyInstance().format(lock.getGrandTotal()) + "\n";
		
		String resultsName = results.toString();
		results.delete();
		File emptyResults = new File(resultsName);
		try {
			Files.writeString(Paths.get(emptyResults.toString()), toFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static TreeMap<Integer, File> sortData(ArrayList<File> list, String base){
		TreeMap<Integer, File> sortedData = new TreeMap<Integer, File>();
		
		for(File f : list) {
			String sort = f.toString();
			sort = sort.substring(2 + base.length(), sort.length());
			sort = sort.substring(0, sort.length() - 4);
			int sortedInt = Integer.parseInt(sort);
			
			sortedData.put(sortedInt, f);
		}
		
		return sortedData;
	}

}
