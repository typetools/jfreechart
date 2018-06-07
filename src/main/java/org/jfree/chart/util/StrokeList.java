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
 */

package org.jfree.chart.util;

import org.checkerframework.checker.index.qual.NonNegative;

import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A table of {@link Stroke} objects.
 */
public class StrokeList extends AbstractObjectList {

    /**
     * Creates a new list.
     */
    public StrokeList() {
        super();
    }

    /**
     * Returns a {@link Stroke} object from the list.
     *
     * @param index the index (zero-based).
     *
     * @return The object.
     */
    public Stroke getStroke(@NonNegative int index) {
        return (Stroke) get(index);
    }

    /**
     * Sets the {@link Stroke} for an item in the list.  The list is expanded if necessary.
     *
     * @param index  the index (zero-based).
     * @param stroke  the {@link Stroke}.
     */
    public void setStroke(@NonNegative int index, Stroke stroke) {
        set(index, stroke);
    }

    /**
     * Returns an independent copy of the list.
     * 
     * @return A clone.
     * 
     * @throws CloneNotSupportedException if an item in the list cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    /**
     * Tests the list for equality with another object (typically also a list).
     *
     * @param o  the other object.
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        
        if (o == this) {
            return true;
        }
        
        if (o instanceof StrokeList) {
            return super.equals(o);
        }

        return false;

    }
    
    /**
     * Returns a hash code value for the object.
     *
     * @return the hashcode
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {

        stream.defaultWriteObject();
        final int count = size();
        stream.writeInt(count);
        for (int i = 0; i < count; i++) {
            final Stroke stroke = getStroke(i);
            if (stroke != null) {
                stream.writeInt(i);
                SerialUtils.writeStroke(stroke, stream);
            }
            else {
                stream.writeInt(-1);
            }
        }

    }
    
    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    @SuppressWarnings("index") // stream must have been constructed from an instance of this class
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        int count = stream.readInt();
        for (int i = 0; i < count; i++) {
            int index = stream.readInt();
            if (index != -1) {
                setStroke(index, SerialUtils.readStroke(stream));
            }
        }
        
    }

}


