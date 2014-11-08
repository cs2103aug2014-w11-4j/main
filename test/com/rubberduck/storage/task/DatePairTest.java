package com.rubberduck.storage.task;

import com.rubberduck.storage.task.DatePair;
import com.rubberduck.storage.task.Task;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//@author A0111794E
public class DatePairTest {

    /**
     * Test for view of task that are within date range Task : 1 aug 2014 - 20
     * aug 2014 View Scope : 1 july 2014 - 25 aug 2014 result: true
     */

    @Test
    public void taskWithinPeriod() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.AUGUST, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 20);
        DatePair dp = new DatePair(startDate, endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.JULY, 1);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 25);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);

        assertEquals(true, actual);

    }

    /**
     * Test for view of task overlap date range Task : 1 aug 2014 - 20 aug 2014
     * View Scope : 5 aug 2014 - 15 aug 2014 Expected: true
     */

    @Test
    public void taskOverlapPeriod() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.AUGUST, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 20);
        DatePair dp = new DatePair(startDate, endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.AUGUST, 5);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 15);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);

        assertEquals(true, actual);

    }

    /**
     * Test for view of task overlap date range Task : 1 aug 2014 - 20 aug 2014
     * View Scope : 1 july 2014 - 15 aug 2014 Expected: true
     */

    @Test
    public void taskOverlapPeriod2() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.AUGUST, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 20);
        DatePair dp = new DatePair(startDate, endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.JULY, 1);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 15);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);

        assertEquals(true, actual);

    }

    /**
     * Test for view of task overlap date range Task : 20 aug 2014 View Scope :
     * 5 aug 2014 - 30 aug 2014 Expected: true
     */

    @Test
    public void endDateWithinPeriod() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 20);
        DatePair dp = new DatePair(endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.AUGUST, 5);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 30);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);

        assertEquals(true, actual);

    }

    /**
     * Task Boundary test : 20 aug 2014 View Scope :19 aug 2014 - 30 aug 2014
     * Expected: true
     *
     * Range = 19 - 30 Test Value 20
     */

    @Test
    public void endDateStartBoundary() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 20);
        DatePair dp = new DatePair(endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.AUGUST, 19);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 30);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);

        assertEquals(true, actual);

    }

    /**
     * Task Boundary Test: 19 aug 2014 View Scope :19 aug 2014 - 30 aug 2014
     * Expected: true
     *
     * Range = 19 - 30 Test Value 19
     */

    @Test
    public void endDateStartBoundary2() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 19);
        DatePair dp = new DatePair(endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = (Calendar) endDate.clone();
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 30);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);

        assertEquals(true, actual);

    }

    /**
     * Task Boundary Test : 17 aug - 18 aug 2014 View Scope :19 aug 2014 - 30 aug 2014
     * Expected: false
     *
     * Range = 19 - 30 Test Value 17 aug - 18 aug
     */

    @Test
    public void endDateStartBoundary3() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 18);
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.AUGUST, 17);
        DatePair dp = new DatePair(startDate,endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.AUGUST, 19);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 30);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);
        assertEquals(false, actual);

    }



    /**
     * Task Boundary Test : 24 aug 2014 View Scope :19 aug 2014 - 25 aug 2014
     * Expected: true
     *
     * Range = 19 - 25 Test Value 24
     */

    @Test
    public void endDateEndBoundary() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 24);
        DatePair dp = new DatePair(endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.AUGUST, 19);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 25);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);
        assertEquals(true, actual);

    }

    /**
     * Task Boundary Test: 24 aug 2014 View Scope :19 aug 2014 - 25 aug 2014
     * Expected: true
     *
     * Range = 19 - 25 Test Value 24
     */

    @Test
    public void endDateEndBoundary2() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 25);
        DatePair dp = new DatePair(endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.AUGUST, 19);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 25);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);
        assertEquals(true, actual);

    }

    /**
     * Task Boundary Test : 26 aug 2014 27 aug 2014 View Scope : 19 aug 2014 -
     * 25 aug 2014 Expected: false
     *
     * Range = 19 - 25 Test Value 26 start, 27 end; need to have 2 dates, as
     * those with no start date = infinite early
     */

    @Test
    public void endDateEndBoundary3() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar endDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.AUGUST, 26);
        endDate.set(2014, Calendar.AUGUST, 27);
        DatePair dp = new DatePair(startDate, endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.AUGUST, 19);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 25);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);
        assertEquals(false, actual);

    }

    /**
     * Test: null startDate / endDate Expected: true
     */
    //@author A0119416H
    @Test
    public void nullDates() throws IOException {
        DatePair tempDatePair;

        Calendar tempDate = Calendar.getInstance();
        tempDate.set(2014, Calendar.AUGUST, 20);

        DatePair datePairDoubleNull = new DatePair(); // null-null
        DatePair datePairStartNull = new DatePair(tempDate); // null-20/08
        DatePair datePairEndNull = new DatePair(tempDate, null); // 20/08-null
        DatePair datePairDoubleNotNull = new DatePair(tempDate, tempDate); // 20/08-20/08

        // If one of DatePair has null start & end, always true.

        assertTrue(datePairDoubleNull.isWithinPeriod(datePairDoubleNotNull));
        // null-null against 20/08-20/08

        assertTrue(datePairStartNull.isWithinPeriod(datePairDoubleNull));
        // 20/08-null against null-null

        // If one of DatePair has null start, as long as the other one does not
        // start after its endDate, should be true.

        assertTrue(datePairStartNull.isWithinPeriod(datePairEndNull));
        // null-20/08 against 20/08-null

        // Construct one DatePair start after its endDate
        tempDate = Calendar.getInstance();
        tempDate.set(2014, Calendar.AUGUST, 21);
        tempDatePair = new DatePair(tempDate, null); // 21/08-null

        assertFalse(tempDatePair.isWithinPeriod(datePairStartNull));
        // 21/08-null against null-20/08

        // If one of DatePair has null end, as long as the other one does not
        // end before its startDate, should be true.

        assertTrue(datePairEndNull.isWithinPeriod(tempDatePair));
        // 20/08-null against 21/08-null

        // Construct one DatePair end before its startDate
        tempDate = Calendar.getInstance();
        tempDate.set(2014, Calendar.AUGUST, 19);
        tempDatePair = new DatePair(tempDate); // null-19/08

        assertFalse(tempDatePair.isWithinPeriod(datePairEndNull));
        // null-19/08 against 20/08-null
    }

}
