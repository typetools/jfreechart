/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2016, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * --------------------------------
 * DynamicTimeSeriesCollection.java
 * --------------------------------
 * (C) Copyright 2002-2016, by I. H. Thomae and Contributors.
 *
 * Original Author:  I. H. Thomae (ithomae@ists.dartmouth.edu);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Ricardo JL Rufino (patch #310);
 *
 * Changes
 * -------
 * 22-Nov-2002 : Initial version completed
 *    Jan 2003 : Optimized advanceTime(), added implemnt'n of RangeInfo intfc
 *               (using cached values for min, max, and range); also added
 *               getOldestIndex() and getNewestIndex() ftns so client classes
 *               can use this class as the master "index authority".
 * 22-Jan-2003 : Made this class stand on its own, rather than extending
 *               class FastTimeSeriesCollection
 * 31-Jan-2003 : Changed TimePeriod --> RegularTimePeriod (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package (DG);
 * 29-Apr-2003 : Added small change to appendData method, from Irv Thomae (DG);
 * 19-Sep-2003 : Added new appendData method, from Irv Thomae (DG);
 * 05-May-2004 : Now extends AbstractIntervalXYDataset.  This also required a
 *               change to the return type of the getY() method - I'm slightly
 *               unsure of the implications of this, so it might require some
 *               further amendment (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 11-Jan-2004 : Removed deprecated code in preparation for the 1.0.0
 *               release (DG);
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 * 01-Jul-2014 : Add millisecond time period - see patch #310 by Ricardo JL
 *               Rufino (DG);
 *
 */

package org.jfree.data.time;

import org.checkerframework.common.value.qual.*;
import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.index.qual.*;

import org.checkerframework.checker.index.qual.NonNegative;

import java.util.Calendar;
import java.util.TimeZone;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;

/**
 * A dynamic dataset.
 * <p>
 * Like FastTimeSeriesCollection, this class is a functional replacement
 * for JFreeChart's TimeSeriesCollection _and_ TimeSeries classes.
 * FastTimeSeriesCollection is appropriate for a fixed time range; for
 * real-time applications this subclass adds the ability to append new
 * data and discard the oldest.
 * In this class, the arrays used in FastTimeSeriesCollection become FIFO's.
 * NOTE:As presented here, all data is assumed &gt;= 0, an assumption which is
 * embodied only in methods associated with interface RangeInfo.
 */
public class DynamicTimeSeriesCollection extends AbstractIntervalXYDataset
        implements IntervalXYDataset, DomainInfo, RangeInfo {

    /**
     * Useful constant for controlling the x-value returned for a time
     * period.
     */
    public static final int START = 0;

    /**
     * Useful constant for controlling the x-value returned for a time period.
     */
    public static final int MIDDLE = 1;

    /**
     * Useful constant for controlling the x-value returned for a time period.
     */
    public static final int END = 2;

    /** The maximum number of items for each series (can be overridden). */
    private @NonNegative int maximumItemCount = 2000;  // an arbitrary safe default value

    /** The history count. */
    protected @LTEqLengthOf("this.pointsInTime") @Positive int historyCount;

    /** Storage for the series keys. */
    private Comparable @SameLen("this.valueHistory") [] seriesKeys;

    /** The time period class - barely used, and could be removed (DG). */
    private Class timePeriodClass = Minute.class;   // default value;

    /** Storage for the x-values. */
    protected RegularTimePeriod @MinLen(1) [] pointsInTime;

    /** The number of series. */
    private @NonNegative int seriesCount;

    /**
     * A wrapper for a fixed array of float values.
     */
    protected class ValueSequence {

        /** Storage for the float values. */
        float @SameLen("this") [] dataPoints;

        /**
         * Default constructor:
         */
        public ValueSequence() {
            this(DynamicTimeSeriesCollection.this.maximumItemCount);
        }

        /**
         * Creates a sequence with the specified length.
         *
         * @param length  the length.
         */
        public ValueSequence(@NonNegative int length) {
            @SuppressWarnings("index") // SameLen on custom collections requires a suppressed warning to establish representation invariant https://github.com/kelloggm/checker-framework/issues/213
            float @SameLen("this") [] dataPointsTmp = new float[length];
            this.dataPoints = dataPointsTmp;
            for (int i = 0; i < length; i++) {
                this.dataPoints[i] = 0.0f;
            }
        }

        /**
         * Enters data into the storage array.
         *
         * @param index  the index.
         * @param value  the value.
         */
        public void enterData(@IndexFor("this") int index, float value) {
            this.dataPoints[index] = value;
        }

        /**
         * Returns a value from the storage array.
         *
         * @param index  the index.
         *
         * @return The value.
         */
        public float getData(@IndexFor("this") int index) {
            return this.dataPoints[index];
        }
    }

    /** An array for storing the objects that represent each series. */
    protected @SameLen("this.pointsInTime") ValueSequence @SameLen("this.seriesKeys") [] valueHistory;

    /** A working calendar (to recycle) */
    protected Calendar workingCalendar;

    /**
     * The position within a time period to return as the x-value (START,
     * MIDDLE or END).
     */
    private int position;

    /**
     * A flag that indicates that the domain is 'points in time'.  If this flag
     * is true, only the x-value is used to determine the range of values in
     * the domain, the start and end x-values are ignored.
     */
    private boolean domainIsPointsInTime;

    /** index for mapping: points to the oldest valid time and data. */
    private @IndexFor("this.pointsInTime") int oldestAt;  // as a class variable, initializes == 0

    /** Index of the newest data item. */
    private @IndexFor("this.pointsInTime") int newestAt;

    // cached values used for interface DomainInfo:

    /** the # of msec by which time advances. */
    private long deltaTime;

    /** Cached domain start (for use by DomainInfo). */
    private Long domainStart;

    /** Cached domain end (for use by DomainInfo). */
    private Long domainEnd;

    /** Cached domain range (for use by DomainInfo). */
    private Range domainRange;

    // Cached values used for interface RangeInfo: (note minValue pinned at 0)
    //   A single set of extrema covers the entire SeriesCollection

    /** The minimum value. */
    private Float minValue = new Float(0.0f);

    /** The maximum value. */
    private Float maxValue = null;

    /** The value range. */
    private Range valueRange;  // autoinit's to null.

    /**
     * Constructs a dataset with capacity for N series, tied to default
     * timezone.
     *
     * @param nSeries the number of series to be accommodated.
     * @param nMoments the number of TimePeriods to be spanned.
     */
    public DynamicTimeSeriesCollection(@NonNegative int nSeries, @Positive int nMoments) {
        this(nSeries, nMoments, new Millisecond(), TimeZone.getDefault());
        @SuppressWarnings("index") // nMoments is used later to populate the field this annotation refers to
        @IndexFor("this.pointsInTime") int newestAtTmp = nMoments - 1;
        this.newestAt = newestAtTmp;
    }

    /**
     * Constructs an empty dataset, tied to a specific timezone.
     *
     * @param nSeries the number of series to be accommodated
     * @param nMoments the number of TimePeriods to be spanned
     * @param zone the timezone.
     */
    public DynamicTimeSeriesCollection(@NonNegative int nSeries, @Positive int nMoments,
            TimeZone zone) {
        this(nSeries, nMoments, new Millisecond(), zone);
        @SuppressWarnings("index") // nMoments is used later to populate the field this annotation refers to
        @IndexFor("this.pointsInTime") int newestAtTmp = nMoments - 1;
        this.newestAt = newestAtTmp;
    }

    /**
     * Creates a new dataset.
     *
     * @param nSeries  the number of series.
     * @param nMoments  the number of items per series.
     * @param timeSample  a time period sample.
     */
    public DynamicTimeSeriesCollection(@NonNegative int nSeries, @Positive int nMoments,
            RegularTimePeriod timeSample) {
        this(nSeries, nMoments, timeSample, TimeZone.getDefault());
    }

    /**
     * Creates a new dataset.
     *
     * @param nSeries  the number of series.
     * @param nMoments  the number of items per series.
     * @param timeSample  a time period sample.
     * @param zone  the time zone.
     */
    @SuppressWarnings("index") // this constructor establishes the repr. invariants
    public DynamicTimeSeriesCollection(@NonNegative int nSeries, @Positive int nMoments,
            RegularTimePeriod timeSample, TimeZone zone) {

        // the first initialization must precede creation of the ValueSet array:
        this.maximumItemCount = nMoments;  // establishes length of each array
        this.historyCount = nMoments;
        this.seriesKeys = new Comparable[nSeries];
        // initialize the members of "seriesNames" array so they won't be null:
        for (int i = 0; i < nSeries; i++) {
            this.seriesKeys[i] = "";
        }
        this.valueHistory = new ValueSequence[nSeries];
        this.timePeriodClass = timeSample.getClass();

        /// Expand the following for all defined TimePeriods:
        if (this.timePeriodClass == Millisecond.class) {
            this.pointsInTime = new Millisecond[nMoments];
        } else if (this.timePeriodClass == Second.class) {
            this.pointsInTime = new Second[nMoments];
        } else if (this.timePeriodClass == Minute.class) {
            this.pointsInTime = new Minute[nMoments];
        } else if (this.timePeriodClass == Hour.class) {
            this.pointsInTime = new Hour[nMoments];
        }

        this.newestAt = nMoments - 1;

        ///  .. etc....
        this.workingCalendar = Calendar.getInstance(zone);
        this.position = START;
        this.domainIsPointsInTime = true;
    }

    /**
     * Fill the pointsInTime with times using TimePeriod.next():
     * Will silently return if the time array was already populated.
     *
     * Also computes the data cached for later use by
     * methods implementing the DomainInfo interface:
     *
     * @param start  the start.
     *
     * @return ??.
     */
    public synchronized long setTimeBase(RegularTimePeriod start) {
        if (this.pointsInTime[0] == null) {
            this.pointsInTime[0] = start;
            for (int i = 1; i < this.historyCount; i++) {
                this.pointsInTime[i] = this.pointsInTime[i - 1].next();
            }
        }
        long oldestL = this.pointsInTime[0].getFirstMillisecond(
                this.workingCalendar);
        @SuppressWarnings("index") // This seems like a bug to me. There's nothing in the docs for this class that suggests that pointsInTime must have more than 1 element
        long nextL = this.pointsInTime[1].getFirstMillisecond(
                this.workingCalendar);
        this.deltaTime = nextL - oldestL;
        this.oldestAt = 0;
        this.newestAt = this.historyCount - 1;
        findDomainLimits();
        return this.deltaTime;
    }

    /**
     * Finds the domain limits.  Note: this doesn't need to be synchronized
     * because it's called from within another method that already is.
     */
    protected void findDomainLimits() {
        long startL = getOldestTime().getFirstMillisecond(this.workingCalendar);
        long endL;
        if (this.domainIsPointsInTime) {
            endL = getNewestTime().getFirstMillisecond(this.workingCalendar);
        }
        else {
            endL = getNewestTime().getLastMillisecond(this.workingCalendar);
        }
        this.domainStart = new Long(startL);
        this.domainEnd = new Long(endL);
        this.domainRange = new Range(startL, endL);
    }

    /**
     * Returns the x position type (START, MIDDLE or END).
     *
     * @return The x position type.
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Sets the x position type (START, MIDDLE or END).
     *
     * @param position The x position type.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Adds a series to the dataset.  Only the y-values are supplied, the
     * x-values are specified elsewhere.
     *
     * @param values  the y-values.
     * @param seriesNumber  the series index (zero-based).
     * @param seriesKey  the series key.
     *
     * Use this as-is during setup only, or add the synchronized keyword around
     * the copy loop.
     */
    public void addSeries(float[] values, @NonNegative int seriesNumber,
            Comparable seriesKey) {

        invalidateRangeInfo();
        int i;
        if (values == null) {
            throw new IllegalArgumentException("TimeSeriesDataset.addSeries(): "
                + "cannot add null array of values.");
        }
        if (seriesNumber >= this.valueHistory.length) {
            throw new IllegalArgumentException("TimeSeriesDataset.addSeries(): "
                + "cannot add more series than specified in c'tor");
        }
        if (this.valueHistory[seriesNumber] == null) {
            @SuppressWarnings("index") // this.historyCount is the length of this.pointsInTime
            @SameLen("this.pointsInTime") ValueSequence valueSequence = new ValueSequence(this.historyCount);
            this.valueHistory[seriesNumber] = valueSequence;
            this.seriesCount++;
        }
        // But if that series array already exists, just overwrite its contents

        // Avoid IndexOutOfBoundsException:
        int srcLength = values.length;
        @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/158: this.valueHistory[seriesNumber]'s length is <= this.historyCount
        @LTEqLengthOf({"this.valueHistory[seriesNumber]", "values"}) int copyLength = this.historyCount;
        boolean fillNeeded = false;
        if (srcLength < this.historyCount) {
            fillNeeded = true;
            @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/158: this.valueHistory[seriesNumber]'s length is <= this.historyCount
            @LTEqLengthOf({"this.valueHistory[seriesNumber]", "values"}) int newCopyLength = srcLength;
            copyLength = newCopyLength;
        }
        //{
        for (i = 0; i < copyLength; i++) { // deep copy from values[], caller
                                           // can safely discard that array
            this.valueHistory[seriesNumber].enterData(i, values[i]);
        }
        if (fillNeeded) {
            for (i = copyLength; i < this.historyCount; i++) {
                @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/205
                @LTLengthOf("this.valueHistory[seriesNumber]") int i0 = i;
                this.valueHistory[seriesNumber].enterData(i0, 0.0f);
            }
        }
      //}
        if (seriesKey != null) {
            this.seriesKeys[seriesNumber] = seriesKey;
        }
        fireSeriesChanged();
    }

    /**
     * Sets the name of a series.  If planning to add values individually.
     *
     * @param seriesNumber  the series.
     * @param key  the new key.
     */
    public void setSeriesKey(@IndexFor("this.seriesKeys") int seriesNumber, Comparable key) {
        this.seriesKeys[seriesNumber] = key;
    }

    /**
     * Adds a value to a series.
     *
     * @param seriesNumber  the series index.
     * @param index  ??.
     * @param value  the value.
     */
    public void addValue(@NonNegative int seriesNumber, @IndexFor("this.getSeries(#1)") int index, float value) {
        invalidateRangeInfo();
        if (seriesNumber >= this.valueHistory.length) {
            throw new IllegalArgumentException(
                "TimeSeriesDataset.addValue(): series #"
                + seriesNumber + "unspecified in c'tor"
            );
        }
        if (this.valueHistory[seriesNumber] == null) {
            @SuppressWarnings("index") // this.historyCount is the length of this.pointsInTime
            @SameLen("this.pointsInTime") ValueSequence valueSequence = new ValueSequence(this.historyCount);
            this.valueHistory[seriesNumber] = valueSequence;
            this.seriesCount++;
        }
        // But if that series array already exists, just overwrite its contents
        //synchronized(this)
        //{
        @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/205
                @LTLengthOf("this.valueHistory[seriesNumber]") int index0 = index;
            this.valueHistory[seriesNumber].enterData(index0, value);
        //}
        fireSeriesChanged();
    }

    /**
     * Returns the number of series in the collection.
     *
     * @return The series count.
     */
    @Override
    @SuppressWarnings("index")
    public @NonNegative @LTEqLengthOf("this.valueHistory") int getSeriesCount() {
        return this.seriesCount;
    }

    /**
     * Returns the number of items in a series.
     * <p>
     * For this implementation, all series have the same number of items.
     *
     * @param series  the series index (zero-based).
     *
     * @return The item count.
     */
    @Override
    @SuppressWarnings("index") // SameLen to a method with any argument https://github.com/kelloggm/checker-framework/issues/209: I'd like to write SameLen on this.pointsInTime
    public @LengthOf("this.getSeries(#1)") int getItemCount(@NonNegative int series) {  // all arrays equal length,
                                           // so ignore argument:
        return this.historyCount;
    }

    // Methods for managing the FIFO's:

    /**
     * Re-map an index, for use in retrieving data.
     *
     * @param toFetch  the index.
     * @param series a ghost variable necessary for the annotations
     *
     * @return The translated index.
     */
    @SuppressWarnings("index") // internal method that breaks abstraction boundaries
    protected @IndexFor("this.pointsInTime") int translateGet(@IndexFor("this.getSeries(#2)") int toFetch, int series) {
        if (this.oldestAt == 0) {
            return toFetch;  // no translation needed
        }
        // else  [implicit here]
        int newIndex = toFetch + this.oldestAt;
        if (newIndex >= this.historyCount) {
            newIndex -= this.historyCount;
        }
        return newIndex;
    }

    /**
     * Returns the actual index to a time offset by "delta" from newestAt.
     *
     * @param delta  the delta.
     *
     * @return The offset.
     */
    public @IndexFor("this.pointsInTime") int offsetFromNewest(int delta) {
        return wrapOffset(this.newestAt + delta);
    }

    /**
     * ??
     *
     * @param delta ??
     *
     * @return The offset.
     */
    public @IndexFor("this.pointsInTime") int offsetFromOldest(int delta) {
        return wrapOffset(this.oldestAt + delta);
    }

    /**
     * ??
     *
     * @param protoIndex  the index.
     *
     * @return The offset.
     */
    @SuppressWarnings("index") // this method assumes that protoIndex will be within an absolute value of an actual index
    protected @IndexFor("this.pointsInTime") int wrapOffset(int protoIndex) {
        int tmp = protoIndex;
        if (tmp >= this.historyCount) {
            tmp -= this.historyCount;
        }
        else if (tmp < 0) {
            tmp += this.historyCount;
        }
        return tmp;
    }

    /**
     * Adjust the array offset as needed when a new time-period is added:
     * Increments the indices "oldestAt" and "newestAt", mod(array length),
     * zeroes the series values at newestAt, returns the new TimePeriod.
     *
     * @return The new time period.
     */
    public synchronized RegularTimePeriod advanceTime() {
        RegularTimePeriod nextInstant = this.pointsInTime[this.newestAt].next();
        this.newestAt = this.oldestAt;  // newestAt takes value previously held
                                        // by oldestAT
        /***
         * The next 10 lines or so should be expanded if data can be negative
         ***/
        // if the oldest data contained a maximum Y-value, invalidate the stored
        //   Y-max and Y-range data:
        boolean extremaChanged = false;
        float oldMax = 0.0f;
        if (this.maxValue != null) {
            oldMax = this.maxValue.floatValue();
        }
        for (int s = 0; s < getSeriesCount(); s++) {
            @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/205
                @LTLengthOf("this.valueHistory[s]") int oldestAt = this.oldestAt;
            if (this.valueHistory[s].getData(oldestAt) == oldMax) {
                extremaChanged = true;
            }
            if (extremaChanged) {
                break;
            }
        }  /*** If data can be < 0, add code here to check the minimum    **/
        if (extremaChanged) {
            invalidateRangeInfo();
        }
        //  wipe the next (about to be used) set of data slots
        float wiper = (float) 0.0;
        for (int s = 0; s < getSeriesCount(); s++) {
            @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/205
                @LTLengthOf("this.valueHistory[s]") int newestAt = this.newestAt;
            this.valueHistory[s].enterData(newestAt, wiper);
        }
        // Update the array of TimePeriods:
        this.pointsInTime[this.newestAt] = nextInstant;
        // Now advance "oldestAt", wrapping at end of the array
        int newOldestAt;
        if (this.oldestAt >= this.historyCount) {
            newOldestAt = 0;
        } else {
            newOldestAt = this.oldestAt + 1;
        }
        @SuppressWarnings("index") // the check right above ensures this doesn't go out of bounds
        @IndexFor("this.pointsInTime") int tmp = newOldestAt;
        this.oldestAt = tmp;
        // Update the domain limits:
        long startL = this.domainStart.longValue();  //(time is kept in msec)
        this.domainStart = new Long(startL + this.deltaTime);
        long endL = this.domainEnd.longValue();
        this.domainEnd = new Long(endL + this.deltaTime);
        this.domainRange = new Range(startL, endL);
        fireSeriesChanged();
        return nextInstant;
    }

    //  If data can be < 0, the next 2 methods should be modified

    /**
     * Invalidates the range info.
     */
    public void invalidateRangeInfo() {
        this.maxValue = null;
        this.valueRange = null;
    }

    /**
     * Returns the maximum value.
     *
     * @return The maximum value.
     */
    protected double findMaxValue() {
        double max = 0.0f;
        for (int s = 0; s < getSeriesCount(); s++) {
            for (int i = 0; i < this.historyCount; i++) {
                @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/205
                @LTLengthOf("this.getSeries(s)") int i0 = i;
                double tmp = getYValue(s, i0);
                if (tmp > max) {
                    max = tmp;
                }
            }
        }
        return max;
    }

    /** End, positive-data-only code  **/

    /**
     * Returns the index of the oldest data item.
     *
     * @return The index.
     */
    public @IndexFor("this.pointsInTime") int getOldestIndex() {
        return this.oldestAt;
    }

    /**
     * Returns the index of the newest data item.
     *
     * @return The index.
     */
    public @IndexFor("this.pointsInTime") int getNewestIndex() {
        return this.newestAt;
    }

    // appendData() writes new data at the index position given by newestAt/
    // When adding new data dynamically, use advanceTime(), followed by this:
    /**
     * Appends new data.
     *
     * @param newData  the data.
     */
    public void appendData(float[] newData) {
        int nDataPoints = newData.length;
        if (nDataPoints > this.valueHistory.length) {
            throw new IllegalArgumentException(
                    "More data than series to put them in");
        }
        int s;   // index to select the "series"
        for (s = 0; s < nDataPoints; s++) {
            // check whether the "valueHistory" array member exists; if not,
            // create them:
            if (this.valueHistory[s] == null) {
                @SuppressWarnings("index") // this.historyCount is the length of this.pointsInTime
                @SameLen("this.pointsInTime") ValueSequence valueSequence = new ValueSequence(this.historyCount);
                this.valueHistory[s] = valueSequence;
            }
            @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/205
                @LTLengthOf("this.valueHistory[s]") int newestAt = this.newestAt;
            this.valueHistory[s].enterData(newestAt, newData[s]);
        }
        fireSeriesChanged();
    }

    /**
     * Appends data at specified index, for loading up with data from file(s).
     *
     * @param  newData  the data
     * @param  insertionIndex  the index value at which to put it
     * @param  refresh  value of n in "refresh the display on every nth call"
     *                 (ignored if &lt;= 0 )
     */
    public void appendData(float[] newData, final @IndexFor("this.pointsInTime") int insertionIndex, int refresh) {
        int nDataPoints = newData.length;
        if (nDataPoints > this.valueHistory.length) {
            throw new IllegalArgumentException(
                    "More data than series to put them in");
        }
        for (int s = 0; s < nDataPoints; s++) {
            if (this.valueHistory[s] == null) {
                @SuppressWarnings("index") // this.historyCount is the length of this.pointsInTime
                @SameLen("this.pointsInTime") ValueSequence valueSequence = new ValueSequence(this.historyCount);
                this.valueHistory[s] = valueSequence;
            }
            @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/205
                @LTLengthOf("this.valueHistory[s]") int insertionIndex0 = insertionIndex;
            this.valueHistory[s].enterData(insertionIndex0, newData[s]);
        }
        if (refresh > 0) {
            int insertionIndexPlus = insertionIndex + 1;
            if (insertionIndexPlus % refresh == 0) {
                fireSeriesChanged();
            }
        }
    }

    /**
     * Returns the newest time.
     *
     * @return The newest time.
     */
    public RegularTimePeriod getNewestTime() {
        return this.pointsInTime[this.newestAt];
    }

    /**
     * Returns the oldest time.
     *
     * @return The oldest time.
     */
    public RegularTimePeriod getOldestTime() {
        return this.pointsInTime[this.oldestAt];
    }

    /**
     * Returns the x-value.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    // getXxx() ftns can ignore the "series" argument:
    // Don't synchronize this!! Instead, synchronize the loop that calls it.
    @Override
    public Number getX(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        RegularTimePeriod tp = this.pointsInTime[translateGet(item, series)];
        return new Long(getX(tp));
    }

    /**
     * Returns the y-value.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    @Override
    public double getYValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        // Don't synchronize this!!
        // Instead, synchronize the loop that calls it.
        @SuppressWarnings("index") // array-list interop: every other class that implements these interfaces uses a list to store series, so the annotation here is wrong
        ValueSequence values = this.valueHistory[series];
        return values.getData(translateGet(item, series));
    }

    /**
     * Returns the y-value.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    @Override
    public Number getY(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        return new Float(getYValue(series, item));
    }

    /**
     * Returns the start x-value.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    @Override
    public Number getStartX(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        RegularTimePeriod tp = this.pointsInTime[translateGet(item, series)];
        return new Long(tp.getFirstMillisecond(this.workingCalendar));
    }

    /**
     * Returns the end x-value.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    @Override
    public Number getEndX(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        RegularTimePeriod tp = this.pointsInTime[translateGet(item, series)];
        return new Long(tp.getLastMillisecond(this.workingCalendar));
    }

    /**
     * Returns the start y-value.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    @Override
    public Number getStartY(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        return getY(series, item);
    }

    /**
     * Returns the end y-value.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    @Override
    public Number getEndY(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        return getY(series, item);
    }

    /* // "Extras" found useful when analyzing/verifying class behavior:
    public Number getUntranslatedXValue(int series, @NonNegative int item)
    {
      return super.getXValue(series, item);
    }

    public float getUntranslatedY(int series, @NonNegative int item)
    {
      return super.getY(series, item);
    }  */

    /**
     * Returns the key for a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The key.
     */
    @Override
    @SuppressWarnings("index") // array-list interop: every other class that implements this interface backs the series with a list, so the annotation on this class don't correspond to the ones on the interface
    public Comparable getSeriesKey(@IndexFor("this.seriesKeys") int series) {
        return this.seriesKeys[series];
    }

    /**
     * Sends a {@link SeriesChangeEvent} to all registered listeners.
     */
    protected void fireSeriesChanged() {
        seriesChanged(new SeriesChangeEvent(this));
    }

    // The next 3 functions override the base-class implementation of
    // the DomainInfo interface.  Using saved limits (updated by
    // each updateTime() call), improves performance.
    //

    /**
     * Returns the minimum x-value in the dataset.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         x-interval is taken into account.
     *
     * @return The minimum value.
     */
    @Override
    public double getDomainLowerBound(boolean includeInterval) {
        return this.domainStart.doubleValue();
        // a Long kept updated by advanceTime()
    }

    /**
     * Returns the maximum x-value in the dataset.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         x-interval is taken into account.
     *
     * @return The maximum value.
     */
    @Override
    public double getDomainUpperBound(boolean includeInterval) {
        return this.domainEnd.doubleValue();
        // a Long kept updated by advanceTime()
    }

    /**
     * Returns the range of the values in this dataset's domain.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         x-interval is taken into account.
     *
     * @return The range.
     */
    @Override
    public Range getDomainBounds(boolean includeInterval) {
        if (this.domainRange == null) {
            findDomainLimits();
        }
        return this.domainRange;
    }

    /**
     * Returns the x-value for a time period.
     *
     * @param period  the period.
     *
     * @return The x-value.
     */
    private long getX(RegularTimePeriod period) {
        switch (this.position) {
            case (START) :
                return period.getFirstMillisecond(this.workingCalendar);
            case (MIDDLE) :
                return period.getMiddleMillisecond(this.workingCalendar);
            case (END) :
                return period.getLastMillisecond(this.workingCalendar);
            default:
                return period.getMiddleMillisecond(this.workingCalendar);
        }
     }

    // The next 3 functions implement the RangeInfo interface.
    // Using saved limits (updated by each updateTime() call) significantly
    // improves performance.  WARNING: this code makes the simplifying
    // assumption that data is never negative.  Expand as needed for the
    // general case.

    /**
     * Returns the minimum range value.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The minimum range value.
     */
    @Override
    public double getRangeLowerBound(boolean includeInterval) {
        double result = Double.NaN;
        if (this.minValue != null) {
            result = this.minValue.doubleValue();
        }
        return result;
    }

    /**
     * Returns the maximum range value.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The maximum range value.
     */
    @Override
    public double getRangeUpperBound(boolean includeInterval) {
        double result = Double.NaN;
        if (this.maxValue != null) {
            result = this.maxValue.doubleValue();
        }
        return result;
    }

    /**
     * Returns the value range.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The range.
     */
    @Override
    public Range getRangeBounds(boolean includeInterval) {
        if (this.valueRange == null) {
            double max = getRangeUpperBound(includeInterval);
            this.valueRange = new Range(0.0, max);
        }
        return this.valueRange;
    }

}
