/* ------------------------------------------------------------------------- */
/*   Copyright (C) 2011 Marius C. Silaghi
                Author: Marius Silaghi: msilaghi@fit.edu
                Florida Tech, Human Decision Support Systems Laboratory
   
       This program is free software; you can redistribute it and/or modify
       it under the terms of the GNU Affero General Public License as published by
       the Free Software Foundation; either the current version of the License, or
       (at your option) any later version.
   
      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.
  
      You should have received a copy of the GNU Affero General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.              */
/* ------------------------------------------------------------------------- */
package ASN1;

import java.util.TimeZone;
import java.util.Calendar;
//import ASN1.Encoder;
public class Util {
    public static final int MAX_DUMP = 20;
    public static final int MAX_UPDATE_DUMP = 400;
    static String HEX[]={"0","1","2","3","4","5","6","7","8","9",
			 "A","B","C","D","E","F"};
    public static String byteToHex(byte[] b, int off, int len, String separator){
        if(b==null) return "NULL";
        String result="";
        for(int i=off; i<off+len; i++)
	    result = result+separator+HEX[(b[i]>>4)&0x0f]+HEX[b[i]&0x0f];
        return result;
    }
    public static String byteToHex(byte[] b, String sep){
        if(b==null) return "NULL";
        String result="";
        for(int i=0; i<b.length; i++)
	    result = result+sep+HEX[(b[i]>>4)&0x0f]+HEX[b[i]&0x0f];
        return result;
    }
    public static String byteToHex(byte[] b){
        return Util.byteToHex(b,"");
    }
 	/**
	 * Get a Calendar for this gdate, or null in case of failure
	 * @param gdate
	 * @return
	 */
	public static Calendar getCalendar(String gdate) {
		return getCalendar(gdate, null);
	}
	/**
	 * Get a Calendar for this gdate, or ndef in case of failure
	 * @param gdate
	 * @param def
	 * @return
	 */
	public static Calendar getCalendar(String gdate, Calendar def) {
		if((gdate!=null)&&(gdate.length()<14)) gdate = gdate+"00000000000000";
		if((gdate==null) || (gdate.length()<14)) {
			return def;
		}
		Calendar date = CalendargetInstance();
		try{
		date.set(Integer.parseInt(gdate.substring(0, 4)),
				Integer.parseInt(gdate.substring(4, 6))-1, 
				Integer.parseInt(gdate.substring(6, 8)),
				Integer.parseInt(gdate.substring(8, 10)),
				Integer.parseInt(gdate.substring(10, 12)),
				Integer.parseInt(gdate.substring(12, 14)));
		date.set(Calendar.MILLISECOND, Integer.parseInt(gdate.substring(15, 18)));
		}catch(Exception e){return def;}
		//System.out.println("getCalendar "+gdate+" into "+date);
		return date;
	}
	/**
	 * Get generalized time for this moment
	 * @return
	 */
    public static String getGeneralizedTime(){
    	return Encoder.getGeneralizedTime(CalendargetInstance());
    }
	/**
	 * Get the generalized date for the date at "days_ago" days ago
	 * @param days_ago
	 * @return
	 */
	public static String getGeneralizedDate(int days_ago) {
		Calendar c = CalendargetInstance();
		c.setTimeInMillis(c.getTimeInMillis()-days_ago*(60000*60*24l));
		return Encoder.getGeneralizedTime(c);
	}
	/**
	 * Return now at UTC
	 * @return
	 */
	public static Calendar CalendargetInstance(){
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}
}
