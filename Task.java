/*
 * Task Class:
 * To store and manages information
 * 
 * Author: Sia Wei Kiat Jason A0111794E
 */

package rubberDuck;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class Task {
	private int id;
	private String description="";
	private ArrayList <DatePair> dateList = new ArrayList <DatePair>();
	private boolean isDone = false;
	
	public Task(){
		
	}
	
	public Task(String description){
		this.description = description;		
		this.isDone = false;
	}
	
	public Task(String description, ArrayList <DatePair> dateList){
		this.description = description;
		this.dateList = dateList;	
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDateList(ArrayList<DatePair> dateList){
		this.dateList = dateList;
	}
	
	public ArrayList<DatePair> getDateList(){
		return dateList;
	}
	
	public void addStartDate(GregorianCalendar startDate){
		DatePair dp  = new DatePair(startDate);
		dateList.add(dp);
	}
	
	public void addEndDate(GregorianCalendar endDate){
		DatePair dp = new DatePair(null, endDate);
		dateList.add(dp);
	}
	
	public void addDatePair(DatePair datePair){
		dateList.add(datePair);
	}
	
	public void setIsDone(boolean isDone){
		this.isDone = isDone;
	}
	
	public boolean getIsDone(){
		return isDone;
	}

	public String toString(){
		String status = "Done";
		if(!isDone){
			status = "Not Done";
		}
		return id + " " + description + " " + status;
	}

}
