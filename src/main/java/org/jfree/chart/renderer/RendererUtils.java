/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2017, by Object Refinery Limited and Contributors.
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
 * ------------------
 * RendererUtils.java
 * ------------------
 * (C) Copyright 2007-2017, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 19-Apr-2007 : Version 1 (DG);
 * 27-Mar-2009 : Fixed results for unsorted datasets (DG);
 * 19-May-2009 : Fixed FindBugs warnings, patch by Michal Wozniak (DG);
 * 23-Aug-2012 : Fixed rendering anomaly bug 3561093 (DG);
 * 03-Jul-2013 : Use ParamChecks (DG);
 *
 */

package org.jfree.chart.renderer;

import org.checkerframework.common.value.qual.*;
import org.checkerframework.checker.index.qual.*;

import org.jfree.chart.util.Args;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.XYDataset;

/**
 * Utility methods related to the rendering process.
 *
 * @since 1.0.6
 */
public class RendererUtils {

    /**
     * Finds the lower index of the range of live items in the specified data
     * series.
     *
     * @param dataset  the dataset ({@code null} not permitted).
     * @param series  the series index.
     * @param xLow  the lowest x-value in the live range.
     * @param xHigh  the highest x-value in the live range.
     *
     * @return The index of the required item.
     *
     * @since 1.0.6
     *
     * @see #findLiveItemsUpperBound(XYDataset, int, double, double)
     */
    public static @NonNegative int findLiveItemsLowerBound(XYDataset dataset, @NonNegative int series,
            double xLow, double xHigh) {
        Args.nullNotPermitted(dataset, "dataset");
        if (xLow >= xHigh) {
            throw new IllegalArgumentException("Requires xLow < xHigh.");
        }
        int itemCount = dataset.getItemCount(series);
        if (itemCount <= 1) {
            return 0;
        }
        if (dataset.getDomainOrder() == DomainOrder.ASCENDING) {
            // for data in ascending order by x-value, we are (broadly) looking
            // for the index of the highest x-value that is less than xLow
            @SuppressWarnings("index") // 0 is an index, because the item count is checked above and the function returns if there isn't at least one item
            @IndexFor("dataset.getSeries(series)") int low = 0;
            @IndexFor("dataset.getSeries(series)") int high = itemCount - 1;
            double lowValue = dataset.getXValue(series, low);
            if (lowValue >= xLow) {
                // special case where the lowest x-value is >= xLow
                return low;
            }
            double highValue = dataset.getXValue(series, high);
            if (highValue < xLow) {
                // special case where the highest x-value is < xLow
                return high;
            }
            while (high - low > 1) {
                int mid = (low + high) / 2;
                double midV = dataset.getXValue(series, mid);
                if (midV >= xLow) {
                    high = mid;
                }
                else {
                    low = mid;
                }
            }
            return high;
        }
        else if (dataset.getDomainOrder() == DomainOrder.DESCENDING) {
            // when the x-values are sorted in descending order, the lower
            // bound is found by calculating relative to the xHigh value
            @SuppressWarnings("index") // 0 is an index, because the item count is checked above and the function returns if there isn't at least one item
            @IndexFor("dataset.getSeries(series)") int low = 0;
            @IndexFor("dataset.getSeries(series)") int high = itemCount - 1;
            double lowValue = dataset.getXValue(series, low);
            if (lowValue <= xHigh) {
                return low;
            }
            double highValue = dataset.getXValue(series, high);
            if (highValue > xHigh) {
                return high;
            }
            while (high - low > 1) {
                int mid = (low + high) / 2;
                double midV = dataset.getXValue(series, mid);
                if (midV > xHigh) {
                    low = mid;
                }
                else {
                    high = mid;
                }
            }
            return high;
        }
        else {
            // we don't know anything about the ordering of the x-values,
            // but we can still skip any initial values that fall outside the
            // range...
            @SuppressWarnings("index") // 0 is an index, because the item count is checked above and the function returns if there isn't at least one item
            @IndexFor("dataset.getSeries(series)") int i = 0;
            // skip any items that don't need including...
            double x = dataset.getXValue(series, i);
            int index = i;
            while (index < itemCount && x < xLow) {
                index++;
                if (index < itemCount) {
                    x = dataset.getXValue(series, index);
                }
            }
            return Math.min(Math.max(0, index), itemCount - 1);
        }
    }

    /**
     * Finds the upper index of the range of live items in the specified data
     * series.
     *
     * @param dataset  the dataset ({@code null} not permitted).
     * @param series  the series index.
     * @param xLow  the lowest x-value in the live range.
     * @param xHigh  the highest x-value in the live range.
     *
     * @return The index of the required item.
     *
     * @since 1.0.6
     *
     * @see #findLiveItemsLowerBound(XYDataset, int, double, double)
     */
    public static @NonNegative int findLiveItemsUpperBound(XYDataset dataset, @NonNegative int series,
            double xLow, double xHigh) {
        Args.nullNotPermitted(dataset, "dataset");
        if (xLow >= xHigh) {
            throw new IllegalArgumentException("Requires xLow < xHigh.");
        }
        int itemCount = dataset.getItemCount(series);
        if (itemCount <= 1) {
            return 0;
        }
        if (dataset.getDomainOrder() == DomainOrder.ASCENDING) {
            @SuppressWarnings("index") // 0 is an index, because the item count is checked above and the function returns if there isn't at least one item
            @IndexFor("dataset.getSeries(series)") int low = 0;
            @IndexFor("dataset.getSeries(series)") int high = itemCount - 1;
            double lowValue = dataset.getXValue(series, low);
            if (lowValue > xHigh) {
                return low;
            }
            double highValue = dataset.getXValue(series, high);
            if (highValue <= xHigh) {
                return high;
            }
            int mid = (low + high) / 2;
            while (high - low > 1) {
                double midV = dataset.getXValue(series, mid);
                if (midV <= xHigh) {
                    low = mid;
                }
                else {
                    high = mid;
                }
                mid = (low + high) / 2;
            }
            return mid;
        }
        else if (dataset.getDomainOrder() == DomainOrder.DESCENDING) {
            // when the x-values are descending, the upper bound is found by
            // comparing against xLow
            @SuppressWarnings("index") // 0 is an index, because the item count is checked above and the function returns if there isn't at least one item
            @IndexFor("dataset.getSeries(series)") int low = 0;
            @IndexFor("dataset.getSeries(series)") int high = itemCount - 1;
            int mid = (low + high) / 2;
            double lowValue = dataset.getXValue(series, low);
            if (lowValue < xLow) {
                return low;
            }
            double highValue = dataset.getXValue(series, high);
            if (highValue >= xLow) {
                return high;
            }
            while (high - low > 1) {
                double midV = dataset.getXValue(series, mid);
                if (midV >= xLow) {
                    low = mid;
                }
                else {
                    high = mid;
                }
                mid = (low + high) / 2;
            }
            return mid;
        }
        else {
            // we don't know anything about the ordering of the x-values,
            // but we can still skip any trailing values that fall outside the
            // range...
            int index = itemCount - 1;
            @IndexFor("dataset.getSeries(series)") int indexTmp = index;          // skip any items that don't need including...
            double x = dataset.getXValue(series, indexTmp);
            while (index >= 0 && x > xHigh) {
                index--;
                if (index >= 0) {
                    x = dataset.getXValue(series, index);
                }
            }
            return Math.max(index, 0);
        }
    }

    /**
     * Finds a range of item indices that is guaranteed to contain all the
     * x-values from x0 to x1 (inclusive).
     *
     * @param dataset  the dataset ({@code null} not permitted).
     * @param series  the series index.
     * @param xLow  the lower bound of the x-value range.
     * @param xHigh  the upper bound of the x-value range.
     *
     * @return The indices of the boundary items.
     */
    public static @NonNegative int @ArrayLen(2) [] findLiveItems(XYDataset dataset, @NonNegative int series,
                                                 double xLow, double xHigh) {
        // here we could probably be a little faster by searching for both
        // indices simultaneously, but I'll look at that later if it seems
        // like it matters...
        @NonNegative int i0 = findLiveItemsLowerBound(dataset, series, xLow, xHigh);
        @NonNegative int i1 = findLiveItemsUpperBound(dataset, series, xLow, xHigh);
        if (i0 > i1) {
            i0 = i1;
        }
        return new @NonNegative int[] {i0, i1};
    }

}
