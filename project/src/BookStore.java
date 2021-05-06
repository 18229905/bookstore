import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.Scanner;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Calendar;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class BookStore {

	Scanner in = null;
	Connection conn = null;
	// Database Host
	final String databaseHost = "orasrv1.comp.hkbu.edu.hk";
	// Database Port
	final int databasePort = 1521;
	// Database name
	final String database = "pdborcl.orasrv1.comp.hkbu.edu.hk";
	final String proxyHost = "faith.comp.hkbu.edu.hk";
	final int proxyPort = 22;
	final String forwardHost = "localhost";
	int forwardPort;
	Session proxySession = null;
	boolean noException = true;

	// JDBC connecting host
	String jdbcHost;
	// JDBC connecting port
	int jdbcPort;

	String[] admin = { 
			"print student info (by sid)", 
			"print book info (by bid)",
			"list order (by sid)",
			"list all students",
			"list all books",
			"list all orders",
			"update deliver date (by sid, bid)",
			"add a student (by sid, name, gender = 'M'/'F', major)",
			"add a book (by bid, title, author, amount, price)",
			"delete a student (by sid)",
			"delete a book (by bid)",
			"logout" };
	
	String[] student = {
			"list all books",
			"list order",
			"ordering books",
			"cancelling order",
			"logout"};
	
	String[] options = {
			"login",
			"exit"
	};
	
	String[] purchase = {
		"add order",
		"purchase",
		"reset",
		"shopping list"
	};


	boolean getYESorNO(String message) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(message));
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog dialog = pane.createDialog(null, "Question");
		dialog.setVisible(true);
		boolean result = JOptionPane.YES_OPTION == (int) pane.getValue();
		dialog.dispose();
		return result;
	}


	String[] getUsernamePassword(String title) {
		JPanel panel = new JPanel();
		final TextField usernameField = new TextField();
		final JPasswordField passwordField = new JPasswordField();
		panel.setLayout(new GridLayout(2, 2));
		panel.add(new JLabel("Username"));
		panel.add(usernameField);
		panel.add(new JLabel("Password"));
		panel.add(passwordField);
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectInitialValue() {
				usernameField.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, title);
		dialog.setVisible(true);
		dialog.dispose();
		return new String[] { usernameField.getText(), new String(passwordField.getPassword()) };
	}


	public boolean loginProxy() {
		if (getYESorNO("Using ssh tunnel or not?")) { // if using ssh tunnel
			String[] namePwd = getUsernamePassword("Login cs lab computer");
			String sshUser = namePwd[0];
			String sshPwd = namePwd[1];
			try {
				proxySession = new JSch().getSession(sshUser, proxyHost, proxyPort);
				proxySession.setPassword(sshPwd);
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				proxySession.setConfig(config);
				proxySession.connect();
				proxySession.setPortForwardingL(forwardHost, 0, databaseHost, databasePort);
				forwardPort = Integer.parseInt(proxySession.getPortForwardingL()[0].split(":")[0]);
			} catch (JSchException e) {
				e.printStackTrace();
				return false;
			}
			jdbcHost = forwardHost;
			jdbcPort = forwardPort;
		} else {
			jdbcHost = databaseHost;
			jdbcPort = databasePort;
		}
		return true;
	}


	public boolean loginDB() {
		String username = "e8229905";//Replace e1234567 to your username
		String password = "e8229905";//Replace e1234567 to your password
		
		/* Do not change the code below */
		if(username.equalsIgnoreCase("e1234567") || password.equalsIgnoreCase("e1234567")) {
			String[] namePwd = getUsernamePassword("Login sqlplus");
			username = namePwd[0];
			password = namePwd[1];
		}
		String URL = "jdbc:oracle:thin:@" + jdbcHost + ":" + jdbcPort + "/" + database;

		try {
			System.out.println("Logging " + URL + " ...");
			conn = DriverManager.getConnection(URL, username, password);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	public void showAdminOptions() {
		System.out.println("Please choose following option:");
		for (int i = 0; i < admin.length; ++i) {
			System.out.println("(" + (i + 1) + ") " + admin[i]);
		}
	}
	
	public void showStudentOptions() {
		System.out.println("Please choose following option:");
		for (int i = 0; i < student.length; ++i) {
			System.out.println("(" + (i + 1) + ") " + student[i]);
		}
	}
	
	public void showOptions() {
		System.out.println("Please choose following option:");
		for (int i = 0; i < options.length; ++i) {
			System.out.println("(" + (i + 1) + ") " + options[i]);
		}
	}
	
	public void showBuyingOptions() {
		System.out.println("Please choose following option:");
		for (int i = 0; i < purchase.length; ++i) {
			System.out.println("(" + (i + 1) + ") " + purchase[i]);
		}
	}
	
	private void printStudentInfo(String sid){
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT * FROM STUDENT WHERE SID = '" + sid + "'";
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next()) {
				System.out.println("Student doesn't exist");
				return;
			}
			String[] heads = { "SID", "NAME", "GENDER", "MAJOR", "TOTAL_SPENT", "DISCOUNT" };
			for (int i = 0; i < 6; ++i) { // student table 6 attributes
				try {
					if(i == 5) {
						System.out.println(heads[i] + " : " + rs.getDouble(i + 1)*100+"%");
					}else
					System.out.println(heads[i] + " : " + rs.getString(i + 1));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}
		}
	
	private void printBookInfo(String bid) {
	try {
		Statement stm = conn.createStatement();
		String sql = "SELECT * FROM BOOK WHERE BID = '" + bid + "'";
		ResultSet rs = stm.executeQuery(sql);
		if (!rs.next()) {
			System.out.println("Book doesn't exist");
			return;
		}
		String[] heads = { "BID", "TITLE", "AUTHOR", "AMOUNT", "PRICE" };
		for (int i = 0; i < 5; ++i) { // BOOK table 5 attributes	
			try {
				System.out.println(heads[i] + " : " + rs.getString(i + 1)); // attribute

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	} catch (SQLException e1) {
		e1.printStackTrace();
		noException = false;
	}
	}

	 private void printOrder(String sid, String bid, int cnt){//changed from 8 attributes to 6
		try {
				Statement stm = conn.createStatement();
				String sql = "SELECT * FROM ORDERED WHERE SID = '" + sid + "' AND BID = '"+bid+"' AND ORDER_COUNT = "+cnt+"";
				ResultSet rs = stm.executeQuery(sql);
//				if (!rs.next()) {
//					return;
//				}
					
				
				String[] heads = { "SID", "BID", "PAYMENT", "CARD_NO", "ORDER_DATE", "DELIVER_DATE" };
				
					try {
						while(rs.next()) {
						for (int i = 0; i < 6; ++i) { // ordered table 6 attributes
						System.out.println(heads[i] + " : " + rs.getString(i + 1));
						}
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				
			} catch (SQLException e1) {
				e1.printStackTrace();
				noException = false;
			}
		}
	 
	 private void listAllStudent() {
			System.out.println("All students in the database now:");
			try {
				Statement stm = conn.createStatement();
				String sql = "SELECT SID FROM STUDENT";
				ResultSet rs = stm.executeQuery(sql);

				int resultCount = 0;
				while (rs.next()) {
					printStudentInfo(rs.getString(1));
					System.out.println();
					++resultCount;
				}
				System.out.println("Total " + resultCount + " student(s).");
				rs.close();
				stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
				noException = false;
			}
		}
	 
	 private void listAllBook() {
			System.out.println("All books in the database now:");
			try {
				Statement stm = conn.createStatement();
				String sql = "SELECT BID FROM BOOK";
				ResultSet rs = stm.executeQuery(sql);

				int resultCount = 0;
				while (rs.next()) {
					printBookInfo(rs.getString(1));
					System.out.println();
					++resultCount;
				}
				System.out.println("Total " + resultCount + " book(s).");
				rs.close();
				stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
				noException = false;
			}
		}
	 
	 private void listOrder(String sid) {
			try {
				Statement stm = conn.createStatement();
				String sql = "SELECT SID, BID, ORDER_COUNT FROM ORDERED WHERE SID = '" + sid + "'";
				ResultSet rs = stm.executeQuery(sql);

				int resultCount = 0;
				while (rs.next()) {
					printOrder(rs.getString(1), rs.getString(2), rs.getInt(3));
					System.out.println();
					++resultCount;
				}
				System.out.println("Total " + resultCount + " order(s).");
				rs.close();
				stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
				noException = false;
			}
		}
	 
	 private void listAllOrder() {
			System.out.println("All orders in the database now:");
			try {
				Statement stm = conn.createStatement();
				String sql = "SELECT SID, BID FROM ORDERED";
				ResultSet rs = stm.executeQuery(sql);

				int resultCount = 0;
				while (rs.next()) {
					System.out.println(rs.getString(1) + " " + rs.getString(2));
					++resultCount;
				}
				System.out.println("Total " + resultCount + " order(s).");
				rs.close();
				stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
				noException = false;
			}
		}
	 
	 private void updateDeliver(String sid, String bid, Date deliver){//edited
		 
		 if(!isStudentExist(sid)) {
			 System.out.println("\nfail to update delivered date (student-"+sid+" does not exist\n");
			 return;
		 }
		 
		 if(!isBookExist(bid)) {
			 System.out.println("\nfail to update delivered date (book-"+bid+" does not exist\n");
			 return;
		 }
	       try {
			Statement stm = conn.createStatement();
			String sql = "UPDATE ORDERED SET DELIVER_DATE = date '"+deliver+"' WHERE SID = '" + sid + "' AND  BID = '" + bid + "' AND (DELIVER_DATE IS NULL)";
			stm.executeUpdate(sql);
			stm.close();
	        System.out.println("\nsucceed to update delivered date ");

		} catch (SQLException e) {
			e.printStackTrace();
	         System.out.println("\nfail to update delivered date ");
			noException = false;
		}
	       if(isAllOrderDeliver(sid)) {
	    	   try {
	   			Statement stm = conn.createStatement();
	   			String sql = "ALTER TRIGGER UPDATE_DELETE_ORDER DISABLE";
	   			stm.executeUpdate(sql);
	   			System.out.println("DISABLE TRIGGER UPDATE_DELETE_ORDER");
	   			deleteOrder(sid,false);
	   			sql = "ALTER TRIGGER UPDATE_DELETE_ORDER ENABLE";
	   			stm.executeUpdate(sql);
	   			stm.close();
	   	        

	   		} catch (SQLException e) {
	   			e.printStackTrace();
	   	         System.out.println("ERROR");
	   			noException = false;
	   		}
	    	   
	    	   
	       }

	}
	 

	 
	 // True if at least one book of a student's order has been delivered
	 private boolean is1OrderDeliver(String sid) { 
		 int resultCount = 0;
		 try {
				Statement stm = conn.createStatement();
				String sql = "SELECT SID, BID FROM ORDERED WHERE SID = '" + sid + "' AND (DELIVER_DATE IS NOT NULL)";
				ResultSet rs = stm.executeQuery(sql);
				
				while (rs.next()) {
					++resultCount;
				}
//				System.out.println("Total " + resultCount + " order(s).");
				rs.close();
				stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
				noException = false;
			}
		 if (resultCount > 0) return true; else return false;
	 }
	 
	 private boolean isAllOrderDeliver(String sid) { //new added
		 int resultCount = 0;
		 try {
				Statement stm = conn.createStatement();
				String sql = "SELECT SID, BID FROM ORDERED WHERE SID = '" + sid + "' AND (DELIVER_DATE IS NULL)";
				ResultSet rs = stm.executeQuery(sql);
				
				while (rs.next()) {
					++resultCount;
				}
//				System.out.println("Total " + resultCount + " order(s).");
				rs.close();
				stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
				noException = false;
			}
		 if (resultCount == 0) return true; else return false;
	 }
	 
	 private Date getOrderDate(String sid) {
		 Date order = null;
		 try {
				Statement stm = conn.createStatement();
				String sql = "SELECT ORDER_DATE FROM ORDERED WHERE SID = '" + sid + "'";
				ResultSet rs = stm.executeQuery(sql);
				if (rs.next()) {
					order = rs.getDate(1);
				}
				rs.close();
				stm.close();
				return order;
			} catch(SQLException e) {
				
			}
		 return null;
		 
	 }
	 
	 private void deleteOrder(String sid, boolean check) { 
		 
		 
		 //calculate days difference compare with today
		 java.sql.Date now = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		 Date order = getOrderDate(sid);

		 if(order == null) {
			 // order date cannot be null
			 System.out.println("\norder is null!\n");
			 return;
		 }
		 if(check == true) {
		 long diffTime = now.getTime() - order.getTime();
		 long diffDay = diffTime / (1000*60*60*24);
		 if(order != null) {
			 if(diffDay > 7) {
				 // must satisfy : The order was made within 7 days.
				 // else: reject deletion
				 System.out.println("\nFail to cancel order since the order was NOT made within 7 days\n");
				 return;
			 }
		 }
		 }
		 try {
				Statement stm = conn.createStatement();

				String sql = ("DELETE FROM ORDERED WHERE SID = '" + sid + "'");
				stm.executeUpdate(sql);
				stm.close();
				System.out.println("succeed to delete order ");
				
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("fail to delete order ");
				noException = false;
			}
		}
	 
	 
	 private void addStudent(String sid, String name, String gender, String major) {
			double total_spent = 0;
			double discount = 0;

				try {
					Statement stm = conn.createStatement();
					String sql = "INSERT INTO STUDENT VALUES( '" + sid + "', " + 
							"'" + name + "', '" + gender + "', '" + major + "', '" +
							 total_spent + "', '" + discount + "')";
					stm.executeUpdate(sql);
					stm.close();
					System.out.println("succeed to add student ");
					printStudentInfo(sid);
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("fail to add a student " + sid);
					noException = false;
				}

		}
	 
	 private void deleteStudent(String sid) {//edited
		 if(isOrderOfStudentExist(sid)) {
			 System.out.println("Cannot delete student since this student has order");
			 return;
		 }
			try {
				if(isStudentExist(sid)) {
				Statement stm = conn.createStatement();

				String sql = "";

				sql = ("DELETE FROM STUDENT WHERE SID = '" + sid + "'");
				stm.executeUpdate(sql);
				stm.close();
				System.out.println("succeed to delete student");
				}
				else {
					System.out.println("No this student");
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("fail to delete student ");
				noException = false;
			}
		}

	 private void addBook(String bid, String title, String author, int amount, double price) {
			try {
				Statement stm = conn.createStatement();
		String sql = "INSERT INTO BOOK VALUES( '" + bid + "', " + 
						"'" + title + "', '" + author + "', " + amount + ", " +
						  price + ")";
				stm.executeUpdate(sql);
				stm.close();
				System.out.println("succeed to add book ");
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("fail to add a book ");
				noException = false;
			}
		}
	 
	 private void deleteBook(String bid) {//edited
		 if(isOrderOfBookExist(bid)) {
			 System.out.println("Cannot delete this book since there are students ordered this book");
			 return;
		 }

			try {
				if(isBookExist(bid)) {
				Statement stm = conn.createStatement();

				String sql = "DELETE FROM BOOK WHERE BID = '" + bid + "'";
				stm.executeUpdate(sql);
				stm.close();
				System.out.println("succeed to delete book ");
				}
				else
				{
					System.out.println("No this book");
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("fail to delete book ");
				noException = false;
			}
		}
	 private int NoOfOrderOfStudent(String sid, String bid){ 
		 int cnt = 0;
		 try {
		 		Statement stm = conn.createStatement();
		 		String sql = "SELECT * FROM ORDERED WHERE SID = '" + sid + "' AND BID = '"+bid+"'";
		 		ResultSet rs = stm.executeQuery(sql);
		 		
		 		while(rs.next()){
		 			cnt++;
		 		}
		 		return cnt;
		 	} catch (SQLException e) {
		 		e.printStackTrace();
		 		noException = false;
		 		return cnt;
		 	}
	 }

	 //Changed attributes and considered SQL when card_no is null 
	 private boolean buyABook(String sid, String bid, Date order, String payment, String card_no, int cnt,boolean check) {
			if(!isAllOrderDeliver(sid) && !check) {
				// at least one order has not been delivered
				System.out.println("\norder rejected! ");
				System.out.println("reason: you have outstanding order!\n");
				return false;
			}
				
			
			//for duplicated {sid, bid} use different cnt
			if(NoOfOrderOfStudent(sid,bid) != 0) {
				cnt = NoOfOrderOfStudent(sid,bid) + 1;
			}
			
		 try {
					Statement stm = conn.createStatement();
					String sql = "";
					if (card_no == null) {
						sql = "INSERT INTO ORDERED VALUES('" + sid + "', '" + bid + "', 'B'"+
								 ", NULL , date '" +order + "', NULL,"+ cnt +")";
					} else {
						sql = "INSERT INTO ORDERED VALUES('" + sid + "', '" + bid + "', 'C'" +
								 ", '" + card_no + "', date '" +order + "', NULL,"+ cnt +")";
					}
					
					stm.executeUpdate(sql);
					stm.close();
					System.out.print("succeed to buy book:\n");
					printBookInfo(bid);
					System.out.println("\nyour order:");
					printOrder(sid, bid, cnt);
					System.out.println();
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("fail to buy a book ");
					noException = false;
					return false;
				}
		}
	 
	 private boolean isStudentExist(String sid){ //added if statement
		 try {
		 		Statement stm = conn.createStatement();
		 		String sql = "SELECT SID FROM STUDENT WHERE SID = '" + sid + "'";
		 		ResultSet rs = stm.executeQuery(sql);
		 		
		 		if(rs.next()) {
		 			rs.close();
		 			stm.close();
		 			return true;
		 		} else {
		 			rs.close(); 
		 			return false;
		 		}
		 		
		 	} catch (SQLException e) {
		 		e.printStackTrace();
		 		noException = false;
		        return false;
		 	}
	 }

	 private boolean isBookExist(String bid){ //new
		 try {
		 		Statement stm = conn.createStatement();
		 		String sql = "SELECT BID FROM BOOK WHERE BID = '" + bid + "'";
		 		ResultSet rs = stm.executeQuery(sql);
		 		
		 		if(rs.next()) {
		 			rs.close();
			 		stm.close();
		 			return true;
		 		} else return false;
		 	} catch (SQLException e) {
		 		e.printStackTrace();
		 		noException = false;
		        return false;
		 	}
	 }
	 
	 private boolean isOrderOfStudentExist(String sid){ //NEW
		 try {
		 		Statement stm = conn.createStatement();
		 		String sql = "SELECT SID FROM ORDERED WHERE SID = '" + sid + "'";
		 		ResultSet rs = stm.executeQuery(sql);
		 		
		 		if(rs.next()) {
		 			rs.close();
		 			stm.close();
		 			return true;
		 		} else {
		 			rs.close(); 
		 			return false;
		 		}
		 		
		 	} catch (SQLException e) {
		 		e.printStackTrace();
		 		noException = false;
		        return false;
		 	}
	 }
	 
	 private boolean isOrderOfBookExist(String bid){ //NEW
		 try {
		 		Statement stm = conn.createStatement();
		 		String sql = "SELECT BID FROM ORDERED WHERE BID = '" + bid + "'";
		 		ResultSet rs = stm.executeQuery(sql);
		 		
		 		if(rs.next()) {
		 			rs.close();
		 			stm.close();
		 			return true;
		 		} else {
		 			rs.close(); 
		 			return false;
		 		}
		 		
		 	} catch (SQLException e) {
		 		e.printStackTrace();
		 		noException = false;
		        return false;
		 	}
	 }
	 
	 private String[] getBookInfo(String bid) { 
			try {
				String[] output = new String[2];
				Statement stm = conn.createStatement();
				String sql = "SELECT PRICE, AMOUNT FROM BOOK WHERE BID = '" + bid + "'";
				ResultSet rs = stm.executeQuery(sql);
				if (!rs.next())
					return null;
				for (int i = 0; i < 2; ++i) {	
					try {
						output[i] = rs.getString(i + 1);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				return output;
			} catch (SQLException e1) {
				e1.printStackTrace();
				noException = false;
				return null;
			}
			
		}
	 
	 private double getDiscountLevel(String sid) { //new
			try {
				Statement stm = conn.createStatement();
				String sql = "SELECT DISCOUNT FROM STUDENT WHERE SID = '" + sid + "'";
				ResultSet rs = stm.executeQuery(sql);
				if (!rs.next())
					return 0;
				else
					return rs.getDouble(1);
			} catch (SQLException e1) {
				e1.printStackTrace();
				noException = false;
				return 0;
			}
			
		}
	 
	 
	 private void orderingBooks(String sid) {//changed
		 listAllBook();
		 java.sql.Date now;
		 List<String> bid = new ArrayList<>();
		 double cost = 0;
		 double discount = getDiscountLevel(sid);
		 while(true) {

			 String payment;
			 String card_no = null;
			 showBuyingOptions();
				String line = in.nextLine();
				int choice = -1;
				try {
					choice = Integer.parseInt(line);
				} catch (Exception e) {
					System.out.println("This option is not available");
					continue;
				}
				if (!(choice >= 1 && choice <= purchase.length)) {
					System.out.println("This option is not available");
					continue;
				}
				
				if(choice == 1) {
					if(!isAllOrderDeliver(sid)) {
						System.out.println("You have outstanding order");
						break;
					}
					System.out.print("Enter bid:");
					String bidN = in.nextLine();
					String[] bookInfo = getBookInfo(bidN);
					if(bookInfo == null) {
						System.out.println("\nOrder rejected!");
						System.out.println("No such book\n");
					}else if(Integer.parseInt(bookInfo[1])<1){
						System.out.println("\nOrder rejected!");
						System.out.println("This book is out of stock\n");
					}
					else{
						bid.add(bidN);
						cost = cost + Double.parseDouble(bookInfo[0]);
						System.out.println("All of the book(s) you ordered cost $"+cost);
					}
					
				}else if(choice == 2) {
					if(!isAllOrderDeliver(sid)) {
						System.out.println("You have outstanding order");
						break;
					}
					do {
						System.out.print("Enter payment method (Bank transfer enter 'B'/Credit card enter'C'): ");
						payment = in.nextLine().toUpperCase();
						if(!payment.equals("B") && !payment.equals("C")) {
							System.out.println("Invalid payment method");
							continue;
						}
						
						if(payment.equals("C")) {
							System.out.print("Enter your credit card number: ");
							card_no = in.nextLine();
						}
						
						break;
					}while(true);
					
					now = new java.sql.Date(Calendar.getInstance().getTime().getTime());
					boolean successfulBuying = false;
					boolean check = false;
					for(int i=0; i<bid.size(); i++) {
						if(i > 0) {
							check  = true;
						}
						successfulBuying = buyABook(sid, bid.get(i), now, payment, card_no, 1, check);
					}
					if(successfulBuying) {
					System.out.print("Book you finally ordered:");
					for(int i=0; i<bid.size(); i++) {
						System.out.println("bid: "+bid.get(i));
					}
					System.out.println("Total cost: $"+cost);
					System.out.println("You get a "+discount*100+"% discount");
					System.out.println("After discount: $"+cost*(1-discount));
					bid.clear();
					cost = 0;
					break;
					}else
						break;
				}else if(choice == 3) {
					bid.clear();
					cost = 0;
				}else if(choice == 4) {
					System.out.println("Your shopping list:");
					for(int i=0; i<bid.size(); i++) {
						System.out.println("bid: "+bid.get(i));
					}
					System.out.println("Total cost: $"+cost);
					System.out.println("You get a "+discount*100+"% discount");
					System.out.println("After discount: $"+cost*(1-discount));
				}
		 }
	 }




	public void run() {
		while (true) {
			
			showOptions();
			String line = in.nextLine();
			int choice = -1;
			try {
				choice = Integer.parseInt(line);
			} catch (Exception e) {
				System.out.println("This option is not available");
				continue;
			}
			if (!(choice >= 1 && choice <= options.length)) {
				System.out.println("This option is not available");
				continue;
			}
			if(choice == 1) {
				System.out.println("login: \nif you are a student, enter your sid"
						+ "\nif you are admin, enter 'a'");
				line = in.nextLine();
				if (line.equalsIgnoreCase("a")) {
					while(true) {
						showAdminOptions();
						line = in.nextLine();
						int Achoice = -1;
						try {
							Achoice = Integer.parseInt(line);
						} catch (Exception e) {
							System.out.println("This option is not available");
							continue;
						}
						if (!(Achoice >= 1 && Achoice <= admin.length)) {
							System.out.println("This option is not available");
							continue;
						}
						
						if(Achoice == 1) {
							System.out.print("Enter sid: ");
							line = in.nextLine();
							printStudentInfo(line);
						} else if(Achoice == 2) {
							System.out.print("Enter bid: ");
							line = in.nextLine();
							printBookInfo(line);
						} else if(Achoice == 3){
							System.out.print("Enter sid: ");
							line = in.nextLine();
							listOrder(line);
						}else if(Achoice == 4){
							listAllStudent();
						} else if(Achoice == 5) {
							listAllBook();
						} else if(Achoice == 6) {
							listAllOrder();
						} else if(Achoice == 7) {
							System.out.print("Enter sid: ");
							String sid = in.nextLine();
							System.out.print("Enter bid: ");
							String bid = in.nextLine();
							java.sql.Date now = new java.sql.Date(Calendar.getInstance().getTime().getTime());
							updateDeliver(sid, bid, now);
						} else if(Achoice == 8) {
							System.out.print("Enter sid: ");
							String sid = in.nextLine();
							System.out.print("Enter name: ");
							String name = in.nextLine();
							System.out.print("Enter gender: ");
							String gender = in.nextLine();
							System.out.print("Enter major: ");
							String major = in.nextLine();
							addStudent(sid, name, gender.toUpperCase(), major);
						} else if(Achoice == 9) {
							System.out.print("Enter bid: ");
							String bid = in.nextLine();
							System.out.print("Enter title: ");
							String title = in.nextLine();
							System.out.print("Enter author: ");
							String author = in.nextLine();
							System.out.print("Enter amount: ");
							int amount = Integer.parseInt(in.nextLine());
							System.out.print("Enter price: ");
							double price = Double.parseDouble(in.nextLine());
							addBook(bid, title, author, amount, price);
						} else if(Achoice == 10) {
							System.out.print("Enter sid: ");
							line = in.nextLine();
							deleteStudent(line);
						} else if(Achoice == 11) {
							System.out.print("Enter bid: ");
							line = in.nextLine();
							deleteBook(line);
						} else if(Achoice == 12) {
							break;
						}
						
					}
				} else if (isStudentExist(line)) {
					String sid = line;
					while(true) {
						showStudentOptions();
						line = in.nextLine();
						int Schoice = -1;
						try {
							Schoice = Integer.parseInt(line);
						} catch (Exception e) {
							System.out.println("This option is not available");
							continue;
						}
						if (!(Schoice >= 1 && Schoice <= admin.length)) {
							System.out.println("This option is not available");
							continue;
						}
						
						if(Schoice == 1) {
							listAllBook();
						} else if(Schoice == 2) {
							listOrder(sid);
						} else if(Schoice == 3) {
							orderingBooks(sid);
						} else if(Schoice == 4) {
							if(is1OrderDeliver(sid)) {
								 System.out.println("\nFail to cancel order since at least one of your order already delivered\n");
								 continue;
							 }
							deleteOrder(sid,true);
						} else if(Schoice == 5) {
							break;
						}
					}
				}
			} else if(choice == 2) {
				return;
			}
			
		}
		
	}

	
	public void close() {
		System.out.println("Thanks for visiting! Bye...");
		try {
			if (conn != null)
				conn.close();
			if (proxySession != null) {
				proxySession.disconnect();
			}
			in.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public BookStore() {
		System.out.println("Welcome to use this online book store!");
		in = new Scanner(System.in);
	}


	public static void main(String[] args) {
		BookStore b = new BookStore();
		if (!b.loginProxy()) {
			System.out.println("Login proxy failed, please re-examine your username and password!");
			return;
		}
		if (!b.loginDB()) {
			System.out.println("Login database failed, please re-examine your username and password!");
			return;
		}
		System.out.println("Login succeed!");
		try {
			b.run();
		} finally {
			b.close();
		}
	}
}
