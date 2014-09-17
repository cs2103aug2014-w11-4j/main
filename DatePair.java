/**
 * DatePair Class:
 * to store dates in a pair
 * When undefined the GregorianCalendar default date is: 01 January 1970 
 * 
 * Author: Sia Wei Kiat Jason A0111794E
 * 
 */

package rubberDuck;

import java.util.GregorianCalendar;

public class DatePair {
	private GregorianCalendar startDate = new GregorianCalendar();
	private GregorianCalendar endDate= new GregorianCalendar();
	
	public DatePair(){
		
	}
	
	public DatePair(GregorianCalendar startDate){
		this.startDate = startDate;
	}
	
	public DatePair(GregorianCalendar startDate,GregorianCalendar endDate){
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public GregorianCalendar getStartDate(){
		return this.startDate;
	}
	
	public GregorianCalendar getEndDate(){
		return this.endDate;
	}
	
	public void setStartDate(GregorianCalendar startDate){
		this.startDate = startDate;
		
	}
	
	public void setEndDate(GregorianCalendar endDate){
		this.endDate = endDate;
	}

}
