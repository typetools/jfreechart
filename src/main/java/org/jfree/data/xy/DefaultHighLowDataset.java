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
 * --------------------------
 * DefaultHighLowDataset.java
 * --------------------------
 * (C) Copyright 2002-2016, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 21-Mar-2002 : Version 1 (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-May-2004 : Now extends AbstractXYDataset and added new methods from
 *               HighLowDataset (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 28-Nov-2006 : Added equals() method override (DG);
 * 22-Apr-2008 : Implemented PublicCloneable (DG);
 * 03-Jul-2013 : Use ParamChecks (DG);
 *
 */

package org.jfree.data.xy;

import org.checkerframework.checker.index.qual.*;

import java.util.Arrays;
import java.util.Date;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;

/**
 * A simple implementation of the {@link OHLCDataset} interface.  See also
 * the {@link DefaultOHLCDataset} class, which provides another implementation
 * that is very similar.
 */
public class DefaultHighLowDataset extends AbstractXYDataset
        implements OHLCDataset, PublicCloneable {

    /** The series key. */
    private Comparable seriesKey;

    /** Storage for the dates. */
    private Date @SameLen({"this.high", "this.date", "this.low", "this.open", "this.close", "this.volume"}) [] date;

    /** Storage for the high values. */
    private Number @SameLen({"this.high", "this.date", "this.low", "this.open", "this.close", "this.volume"}) [] high;

    /** Storage for the low values. */
    private Number @SameLen({"this.high", "this.date", "this.low", "this.open", "this.close", "this.volume"}) [] low;

    /** Storage for the open values. */
    private Number @SameLen({"this.high", "this.date", "this.low", "this.open", "this.close", "this.volume"}) [] open;

    /** Storage for the close values. */
    private Number @SameLen({"this.high", "this.date", "this.low", "this.open", "this.close", "this.volume"}) [] close;

    /** Storage for the volume values. */
    private Number @SameLen({"this.high", "this.date", "this.low", "this.open", "this.close", "this.volume"}) [] volume;

    /**
     * Constructs a new high/low/open/close dataset.
     * <p>
     * The current implementation allows only one series in the dataset.
     * This may be extended in a future version.
     *
     * @param seriesKey  the key for the series ({@code null} not
     *     permitted).
     * @param date  the dates ({@code null} not permitted).
     * @param high  the high values ({@code null} not permitted).
     * @param low  the low values ({@code null} not permitted).
     * @param open  the open values ({@code null} not permitted).
     * @param close  the close values ({@code null} not permitted).
     * @param volume  the volume values ({@code null} not permitted).
     */
    @SuppressWarnings("samelen") // While initializing object, SameLen invariants between the fields will be broken (because one field must be initialized before the others). The annotations on the parameters guarantee the invariant holds after the constructor finishes executing.
    public DefaultHighLowDataset(Comparable seriesKey, Date @SameLen({"#2", "#3", "#4", "#5", "#6", "#7"}) [] date,
            double @SameLen({"#2", "#3", "#4", "#5", "#6", "#7"}) [] high, double @SameLen({"#2", "#3", "#4", "#5", "#6", "#7"}) [] low,
            double @SameLen({"#2", "#3", "#4", "#5", "#6", "#7"}) [] open, double @SameLen({"#2", "#3", "#4", "#5", "#6", "#7"}) [] close,
            double @SameLen({"#2", "#3", "#4", "#5", "#6", "#7"}) [] volume) {

        Args.nullNotPermitted(seriesKey, "seriesKey");
        Args.nullNotPermitted(date, "date");
        this.seriesKey = seriesKey;
        this.date = date;
        this.high = createNumberArray(high);
        this.low = createNumberArray(low);
        this.open = createNumberArray(open);
        this.close = createNumberArray(close);
        this.volume = createNumberArray(volume);

    }

    /**
     * Returns the key for the series stored in this dataset.
     *
     * @param series  the index of the series (ignored, this dataset supports
     *     only one series and this method always returns the key for series 0).
     *
     * @return The series key (never {@code null}).
     */
    @Override
    public Comparable getSeriesKey(@NonNegative int series) {
        return this.seriesKey;
    }

    /**
     * Returns the x-value for one item in a series.  The value returned is a
     * {@code Long} instance generated from the underlying
     * {@code Date} object.  To avoid generating a new object instance,
     * you might prefer to call {@link #getXValue(int, int)}.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The x-value.
     *
     * @see #getXValue(int, int)
     * @see #getXDate(int, int)
     */
    @Override
    public Number getX(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        @SuppressWarnings("index") // array-list interop: the annotation on this method cannot expose the implementation detail of this class being backed by an array
                Number result =  new Long(this.date[item].getTime());
        return result;
    }

    /**
     * Returns the x-value for one item in a series, as a Date.
     * <p>
     * This method is provided for convenience only.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The x-value as a Date.
     *
     * @see #getX(int, int)
     */
    public Date getXDate(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        @SuppressWarnings("index") // array-list interop: the annotation on this method cannot expose the implementation detail of this class being backed by an array
        Date result = this.date[item];
        return result;
    }

    /**
     * Returns the y-value for one item in a series.
     * <p>
     * This method (from the {@link XYDataset} interface) is mapped to the
     * {@link #getCloseValue(int, int)} method.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The y-value.
     *
     * @see #getYValue(int, int)
     */
    @Override
    public Number getY(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        return getClose(series, item);
    }

    /**
     * Returns the high-value for one item in a series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The high-value.
     *
     * @see #getHighValue(int, int)
     */
    @Override
    public Number getHigh(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        @SuppressWarnings("index") // array-list interop: the annotation on this method cannot expose the implementation detail of this class being backed by an array
                Number result = this.high[item];
        return result;
    }

    /**
     * Returns the high-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The high-value.
     *
     * @see #getHigh(int, int)
     */
    @Override
    public double getHighValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        double result = Double.NaN;
        Number h = getHigh(series, item);
        if (h != null) {
            result = h.doubleValue();
        }
        return result;
    }

    /**
     * Returns the low-value for one item in a series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The low-value.
     *
     * @see #getLowValue(int, int)
     */
    @Override
    public Number getLow(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        @SuppressWarnings("index") // array-list interop: the annotation on this method cannot expose the implementation detail of this class being backed by an array
        Number result = this.low[item];
        return result;
    }

    /**
     * Returns the low-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The low-value.
     *
     * @see #getLow(int, int)
     */
    @Override
    public double getLowValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        double result = Double.NaN;
        Number l = getLow(series, item);
        if (l != null) {
            result = l.doubleValue();
        }
        return result;
    }

    /**
     * Returns the open-value for one item in a series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The open-value.
     *
     * @see #getOpenValue(int, int)
     */
    @Override
    public Number getOpen(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        @SuppressWarnings("index") // array-list interop: the annotation on this method cannot expose the implementation detail of this class being backed by an array
        Number result = this.open[item];
        return result;
    }

    /**
     * Returns the open-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The open-value.
     *
     * @see #getOpen(int, int)
     */
    @Override
    public double getOpenValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        double result = Double.NaN;
        Number open = getOpen(series, item);
        if (open != null) {
            result = open.doubleValue();
        }
        return result;
    }

    /**
     * Returns the close-value for one item in a series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The close-value.
     *
     * @see #getCloseValue(int, int)
     */
    @Override
    public Number getClose(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        @SuppressWarnings("index") // array-list interop: the annotation on this method cannot expose the implementation detail of this class being backed by an array
        Number result = this.close[item];
        return result;
    }

    /**
     * Returns the close-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The close-value.
     *
     * @see #getClose(int, int)
     */
    @Override
    public double getCloseValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        double result = Double.NaN;
        Number c = getClose(series, item);
        if (c != null) {
            result = c.doubleValue();
        }
        return result;
    }

    /**
     * Returns the volume-value for one item in a series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The volume-value.
     *
     * @see #getVolumeValue(int, int)
     */
    @Override
    public Number getVolume(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        @SuppressWarnings("index") // array-list interop: the annotation on this method cannot expose the implementation detail of this class being backed by an array
        Number result = this.volume[item];
        return result;
    }

    /**
     * Returns the volume-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The volume-value.
     *
     * @see #getVolume(int, int)
     */
    @Override
    public double getVolumeValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item) {
        double result = Double.NaN;
        Number v = getVolume(series, item);
        if (v != null) {
            result = v.doubleValue();
        }
        return result;
    }

    /**
     * Returns the number of series in the dataset.
     * <p>
     * This implementation only allows one series.
     *
     * @return The number of series.
     */
    @Override
    public @NonNegative int getSeriesCount() {
        return 1;
    }

    /**
     * Returns the number of items in the specified series.
     *
     * @param series  the index (zero-based) of the series.
     *
     * @return The number of items in the specified series.
     */
    @Override
    public @LengthOf("this.getSeries(#1)") int getItemCount(@NonNegative int series) {
        @SuppressWarnings("index") // the annotation on this method isn't quite sensical, but it's the closest we can get, and is safe.
        @LengthOf("this.getSeries(#1)") int result = this.date.length;
        return result;
    }

    /**
     * Tests this dataset for equality with an arbitrary instance.
     *
     * @param obj  the object ({@code null} permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DefaultHighLowDataset)) {
            return false;
        }
        DefaultHighLowDataset that = (DefaultHighLowDataset) obj;
        if (!this.seriesKey.equals(that.seriesKey)) {
            return false;
        }
        if (!Arrays.equals(this.date, that.date)) {
            return false;
        }
        if (!Arrays.equals(this.open, that.open)) {
            return false;
        }
        if (!Arrays.equals(this.high, that.high)) {
            return false;
        }
        if (!Arrays.equals(this.low, that.low)) {
            return false;
        }
        if (!Arrays.equals(this.close, that.close)) {
            return false;
        }
        if (!Arrays.equals(this.volume, that.volume)) {
            return false;
        }
        return true;
    }

    /**
     * Constructs an array of Number objects from an array of doubles.
     *
     * @param data  the double values to convert ({@code null} not
     *     permitted).
     *
     * @return The data as an array of Number objects.
     */
    public static Number @SameLen("#1") [] createNumberArray(double[] data) {
        Number[] result = new Number[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = new Double(data[i]);
        }
        return result;
    }

}
