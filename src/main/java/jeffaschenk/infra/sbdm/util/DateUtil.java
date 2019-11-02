package jeffaschenk.infra.sbdm.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DateUtil {
	
	private static DatatypeFactory df = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);
	private static final List<DateTimeFormatter> knownFormatters;
	
	static {
		knownFormatters = new ArrayList<DateTimeFormatter>();
		knownFormatters.add(DateTimeFormat.forPattern("MMddyyyy"));
		knownFormatters.add(DateTimeFormat.forPattern("MM/dd/yyyy"));
		knownFormatters.add(DateTimeFormat.forPattern("yyyy-MM-dd' 'HH:mm:ss"));
		knownFormatters.add(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm:ss a"));
		knownFormatters.add(ISODateTimeFormat.dateTime());
		
		/** * Needed to create XMLGregorianCalendar instances */
		
		try {
			df = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException dce) {
			throw new IllegalStateException(
					"Exception while obtaining DatatypeFactory instance", dce);
		}
		
	}
	
	private DateUtil() {
	}
	
	public static Date format(final String dateString) {

		 if (StringUtils.isEmpty(dateString)) {
	            return null;
	     }
		 
        for (DateTimeFormatter fmt: knownFormatters) {
        	try {
        		return fmt.parseDateTime(dateString).toDate();
        	} catch (Exception ex) {
        		
        	}
        }
        
        return null;
    }
	
	public static String getDateStringInISOFromat(Date date) {
		
		if (date == null) {
			return null;
		}
		try {
    		return ISODateTimeFormat.dateTime().print(new DateTime(date));
    	} catch (Exception ex) {
    		LOGGER.error("Error while getting date string in ISO format.",ex);
    		return null;
    	}
	}
	

    public static Date format(final String dateString, final String pattern) {

        if (StringUtils.isEmpty(dateString)) {
            return null;
        }

        try {
            return new SimpleDateFormat(pattern).parse(dateString);
        } catch (Exception e) {
        	LOGGER.error("Error while formatting date.",e);
            return null;
        }
    } 
    
    public static int monthsBetweenNowAndTermDate (Date termDate) {

        DateTime now = new DateTime();
        DateTime tDate = new DateTime(termDate);

        Months months = Months.monthsBetween(tDate, now);
        
        return months.getMonths();
    }
    
    public static int daysBetweenNowAndTermDate (Date termDate) {

        if (termDate == null) {
        	return 0;
        }
    	DateTime now = new DateTime();
        DateTime tDate = new DateTime(termDate);
        
        return calculateDaysBetween(tDate,now);
    }
    
    public static int calculateDaysBetween(DateTime beginDate, DateTime endDate) {
        return Days.daysBetween(beginDate, endDate).getDays();
	}
    
    public static boolean isDateInFuture(Date date) {
    	
    	  DateTime now = new DateTime();
          DateTime dateToCompare = new DateTime(date);
          return dateToCompare.compareTo(now) > 0;
    	
    }
    
    public static boolean isDateInThePast(Date date) {
    	
  	    DateTime now = new DateTime();
        DateTime dateToCompare = new DateTime(date);
        return dateToCompare.compareTo(now) < 0;
  	
    }
    
    public static boolean isDateInFuture(String date) {
    	
  	 Date inDate = format(date);
  	 return isDateInFuture(inDate);
    }
    

    
    public static Date greaterOf(Date date1, Date date2) {
    	
    	 DateTime dt1 = new DateTime(date1);
    	 DateTime dt2 = new DateTime(date2);
    	 
    	return dt1.compareTo(dt2) > 0 ? dt1.toDate() : dt2.toDate();
    }
    
   
    
    /**
	 * * Converts a java.util.Date into an instance of XMLGregorianCalendar * * @param
	 * date Instance of java.util.Date or a null reference * @return
	 * XMLGregorianCalendar instance whose value is based upon the * value in the
	 * date parameter. If the date parameter is null then * this method will
	 * simply return null.
	 */
	public static XMLGregorianCalendar asXMLGregorianCalendar(
			Date date) {
		if (date == null) {
			return null;
		} else {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTimeInMillis(date.getTime());
			return df.newXMLGregorianCalendar(gc);
		}
	}

	public static XMLGregorianCalendar asXMLGregorianCalendar(
			String date) {
		if (date == null) {
			return null;
		} else {
			return  asXMLGregorianCalendar(format(date));
		}
	}

	/**
	 * * Converts an XMLGregorianCalendar to an instance of java.util.Date * * @param
	 * xgc Instance of XMLGregorianCalendar or a null reference * @return
	 * java.util.Date instance whose value is based upon the * value in the xgc
	 * parameter. If the xgc parameter is null then * this method will simply
	 * return null.
	 */
	public static Date asDate(XMLGregorianCalendar xgc) {
		if (xgc == null) {
			return null;
		} else {
			return xgc.toGregorianCalendar().getTime();
		}
	}

	/**
	 * Calculate the Time Duration for the Request.
	 *
	 * @param startTime - Begin Time
	 * @param endTime - End Time
	 * @return String containing Duration of Request.
	 */
	public static String getDuration(DateTime startTime, DateTime endTime) {
		return PeriodFormat.getDefault().print(new Period(startTime, endTime));
	}
        
}
