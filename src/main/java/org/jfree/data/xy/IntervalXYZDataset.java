/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2013, by Object Refinery Limited and Contributors.
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
 * -----------------------
 * IntervalXYZDataset.java
 * -----------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 31-Oct-2001 : Version 1 (DG);
 *
 */

package org.jfree.data.xy;
import org.checkerframework.checker.index.qual.*;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * An extension of the {@link XYZDataset} interface that allows a range of data
 * to be defined for any of the X values, the Y values, and the Z values.
 */
public interface IntervalXYZDataset extends XYZDataset {

    /**
     * Returns the starting X value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item within a series (zero-based index).
     *
     * @return The starting X value for the specified series and item.
     */
    public Number getStartXValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item);

    /**
     * Returns the ending X value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item within a series (zero-based index).
     *
     * @return The ending X value for the specified series and item.
     */
    public Number getEndXValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item);

    /**
     * Returns the starting Y value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item within a series (zero-based index).
     *
     * @return The starting Y value for the specified series and item.
     */
    public Number getStartYValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item);

    /**
     * Returns the ending Y value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item within a series (zero-based index).
     *
     * @return The ending Y value for the specified series and item.
     */
    public Number getEndYValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item);

    /**
     * Returns the starting Z value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item within a series (zero-based index).
     *
     * @return The starting Z value for the specified series and item.
     */
    public Number getStartZValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item);

    /**
     * Returns the ending Z value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item within a series (zero-based index).
     *
     * @return The ending Z value for the specified series and item.
     */
    public Number getEndZValue(@NonNegative int series, @IndexFor("this.getSeries(#1)") int item);

}
