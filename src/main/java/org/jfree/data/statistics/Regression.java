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
 * ---------------
 * Regression.java
 * ---------------
 * (C) Copyright 2002-2016, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Peter Kolb (patch 2795746);
 *
 * Changes
 * -------
 * 30-Sep-2002 : Version 1 (DG);
 * 18-Aug-2003 : Added 'abstract' (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 29-May-2009 : Added support for polynomial regression, see patch 2795746
 *               by Peter Kolb (DG);
 * 03-Jul-2013 : Use ParamChecks (DG);
 *
 */

package org.jfree.data.statistics;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.index.qual.*;


import org.checkerframework.checker.index.qual.NonNegative;


import org.jfree.chart.util.Args;
import org.jfree.data.xy.XYDataset;

/**
 * A utility class for fitting regression curves to data.
 */
public abstract class Regression {

    /**
     * Returns the parameters 'a' and 'b' for an equation y = a + bx, fitted to
     * the data using ordinary least squares regression.  The result is
     * returned as a double[], where result[0] --&gt; a, and result[1] --&gt; b.
     *
     * @param data  the data.
     *
     * @return The parameters.
     */
    public static double @ArrayLen(2) [] getOLSRegression(double @MinLen(2) [] @MinLen(2) [] data) {

        int n = data.length;
        if (n < 2) {
            throw new IllegalArgumentException("Not enough data.");
        }

        double sumX = 0;
        double sumY = 0;
        double sumXX = 0;
        double sumXY = 0;
        for (int i = 0; i < n; i++) {
            double x = data[i][0];
            double y = data[i][1];
            sumX += x;
            sumY += y;
            double xx = x * x;
            sumXX += xx;
            double xy = x * y;
            sumXY += xy;
        }
        double sxx = sumXX - (sumX * sumX) / n;
        double sxy = sumXY - (sumX * sumY) / n;
        double xbar = sumX / n;
        double ybar = sumY / n;

        double[] result = new double[2];
        result[1] = sxy / sxx;
        result[0] = ybar - result[1] * xbar;

        return result;

    }

    /**
     * Returns the parameters 'a' and 'b' for an equation y = a + bx, fitted to
     * the data using ordinary least squares regression. The result is returned
     * as a double[], where result[0] --&gt; a, and result[1] --&gt; b.
     *
     * @param data  the data.
     * @param series  the series (zero-based index).
     *
     * @return The parameters.
     */
    public static double @ArrayLen(2) [] getOLSRegression(XYDataset data, @NonNegative int series) {

        int n = data.getItemCount(series);
        if (n < 2) {
            throw new IllegalArgumentException("Not enough data.");
        }

        double sumX = 0;
        double sumY = 0;
        double sumXX = 0;
        double sumXY = 0;
        for (int i = 0; i < n; i++) {
            double x = data.getXValue(series, i);
            double y = data.getYValue(series, i);
            sumX += x;
            sumY += y;
            double xx = x * x;
            sumXX += xx;
            double xy = x * y;
            sumXY += xy;
        }
        double sxx = sumXX - (sumX * sumX) / n;
        double sxy = sumXY - (sumX * sumY) / n;
        double xbar = sumX / n;
        double ybar = sumY / n;

        double[] result = new double[2];
        result[1] = sxy / sxx;
        result[0] = ybar - result[1] * xbar;

        return result;

    }

    /**
     * Returns the parameters 'a' and 'b' for an equation y = ax^b, fitted to
     * the data using a power regression equation.  The result is returned as
     * an array, where double[0] --&gt; a, and double[1] --&gt; b.
     *
     * @param data  the data.
     *
     * @return The parameters.
     */
    public static double @ArrayLen(2) [] getPowerRegression(double @MinLen(2) [] @MinLen(2) [] data) {

        int n = data.length;
        if (n < 2) {
            throw new IllegalArgumentException("Not enough data.");
        }

        double sumX = 0;
        double sumY = 0;
        double sumXX = 0;
        double sumXY = 0;
        for (int i = 0; i < n; i++) {
            double x = Math.log(data[i][0]);
            double y = Math.log(data[i][1]);
            sumX += x;
            sumY += y;
            double xx = x * x;
            sumXX += xx;
            double xy = x * y;
            sumXY += xy;
        }
        double sxx = sumXX - (sumX * sumX) / n;
        double sxy = sumXY - (sumX * sumY) / n;
        double xbar = sumX / n;
        double ybar = sumY / n;

        double[] result = new double[2];
        result[1] = sxy / sxx;
        result[0] = Math.pow(Math.exp(1.0), ybar - result[1] * xbar);

        return result;

    }

    /**
     * Returns the parameters 'a' and 'b' for an equation y = ax^b, fitted to
     * the data using a power regression equation.  The result is returned as
     * an array, where double[0] --&gt; a, and double[1] --&gt; b.
     *
     * @param data  the data.
     * @param series  the series to fit the regression line against.
     *
     * @return The parameters.
     */
    public static double @ArrayLen(2) [] getPowerRegression(XYDataset data, @NonNegative int series) {

        int n = data.getItemCount(series);
        if (n < 2) {
            throw new IllegalArgumentException("Not enough data.");
        }

        double sumX = 0;
        double sumY = 0;
        double sumXX = 0;
        double sumXY = 0;
        for (int i = 0; i < n; i++) {
            double x = Math.log(data.getXValue(series, i));
            double y = Math.log(data.getYValue(series, i));
            sumX += x;
            sumY += y;
            double xx = x * x;
            sumXX += xx;
            double xy = x * y;
            sumXY += xy;
        }
        double sxx = sumXX - (sumX * sumX) / n;
        double sxy = sumXY - (sumX * sumY) / n;
        double xbar = sumX / n;
        double ybar = sumY / n;

        double[] result = new double[2];
        result[1] = sxy / sxx;
        result[0] = Math.pow(Math.exp(1.0), ybar - result[1] * xbar);

        return result;

    }

    /**
     * Returns the parameters 'a0', 'a1', 'a2', ..., 'an' for a polynomial 
     * function of order n, y = a0 + a1 * x + a2 * x^2 + ... + an * x^n,
     * fitted to the data using a polynomial regression equation.
     * The result is returned as an array with a length of n + 2,
     * where double[0] --&gt; a0, double[1] --&gt; a1, .., double[n] --&gt; an.
     * and double[n + 1] is the correlation coefficient R2
     * Reference: J. D. Faires, R. L. Burden, Numerische Methoden (german
     * edition), pp. 243ff and 327ff.
     *
     * @param dataset  the dataset ({@code null} not permitted).
     * @param series  the series to fit the regression line against (the series
     *         must have at least order + 1 non-NaN items).
     * @param order  the order of the function (&gt; 0).
     *
     * @return The parameters.
     *
     * @since 1.0.14
     */
    public static double[] getPolynomialRegression(XYDataset dataset,
            @NonNegative int series, @Positive int order) {
        Args.nullNotPermitted(dataset, "dataset");
        int itemCount = dataset.getItemCount(series);
        if (itemCount < order + 1) {
            throw new IllegalArgumentException("Not enough data.");
        }
        double[][] data = new double[2][itemCount];
        @SuppressWarnings("index") // validItems will only be used as a index if there is at least one item
        @LTLengthOf(value={"data[0]","data[1]"}, offset={"0","0"}) @NonNegative int validItems = 0;
        for(int item = 0; item < itemCount; item++){
            double x = dataset.getXValue(series, item);
            double y = dataset.getYValue(series, item);
            if (!Double.isNaN(x) && !Double.isNaN(y)){
                data[0][validItems] = x;
                data[1][validItems] = y;
                @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/219: validItems is incremented at most as often as item, which is a valid index
                @LTLengthOf(value={"data[0]","data[1]"}, offset={"0","0"}) @NonNegative int validItemsTmp = validItems + 1;
                validItems = validItemsTmp;
            }
        }
        if (validItems < order + 1) {
            throw new IllegalArgumentException("Not enough data.");
        }
        @Positive int equations = order + 1;
        @Positive int coefficients = order + 2;
        double[] result = new double[equations + 1];
        double[] @MinLen(1) [] matrix = new double[equations][coefficients];
        double sumX = 0.0;
        double sumY = 0.0;

        for(int item = 0; item < validItems; item++){
            sumX += data[0][item];
            sumY += data[1][item];
            for(int eq = 0; eq < equations; eq++){
                for(int coe = 0; coe < matrix[eq].length - 1; coe++){
                    matrix[eq][coe] += Math.pow(data[0][item],eq + coe);
                }
                @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/202: coefficients is positive, and matrix's subarrays are all exactly `coefficients` long
                @IndexFor("matrix[eq]") int coe1 = coefficients - 1;
                matrix[eq][coe1] += data[1][item]
                        * Math.pow(data[0][item],eq);
            }
        }
        double[][] subMatrix = calculateSubMatrix(matrix);
        for (int eq = 1; eq < equations; eq++) {
            matrix[eq][0] = 0;
            for (int coe = 1; coe < matrix[eq].length; coe++) {
                @SuppressWarnings("index") // subMatrix is a reduced version of matrix, with rows and columns shifted so that subtracting one is always safe
                double subMatrixEntry = subMatrix[eq - 1][coe - 1];
                matrix[eq][coe] = subMatrixEntry;
            }
        }
        for (int eq = equations - 1; eq > -1; eq--) {
            double value = matrix[eq][matrix[eq].length - 1];
            for (int coe = eq; coe < matrix[eq].length -1; coe++) {
                @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/202: coe is LTOM for matrix[eq], which means that it is LTL for result[eq], which is one smaller in each dimension
                double resultCoefficient = result[coe];
                value -= matrix[eq][coe] * resultCoefficient;
            }
            @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/158 equations < coeffecients (equations = order + 1, coeffecients = order + 2)
            double diag = matrix[eq][eq];
            result[eq] = value / diag;
        }
        double meanY = sumY / validItems;
        double yObsSquare = 0.0;
        double yRegSquare = 0.0;
        for (int item = 0; item < validItems; item++) {
            double yCalc = 0;
            for (int eq = 0; eq < equations; eq++) {
                yCalc += result[eq] * Math.pow(data[0][item],eq);
            }
            yRegSquare += Math.pow(yCalc - meanY, 2);
            yObsSquare += Math.pow(data[1][item] - meanY, 2);
        }
        double rSquare = yRegSquare / yObsSquare;
        result[equations] = rSquare;
        return result;
    }

    /**
     * Returns a matrix with the following features: (1) the number of rows
     * and columns is 1 less than that of the original matrix; (2)the matrix
     * is triangular, i.e. all elements a (row, column) with column &gt; row are
     * zero.  This method is used for calculating a polynomial regression.
     * 
     * @param matrix  the start matrix.
     *
     * @return The new matrix.
     */
    private static double[][] calculateSubMatrix(double @MinLen(1) [] @MinLen(1) [] matrix){
        @Positive int equations = matrix.length;
        @Positive int coefficients = matrix[0].length;
        double[][] result = new double[equations - 1][coefficients - 1];
        for (int eq = 1; eq < equations; eq++) {
            double factor = matrix[0][0] / matrix[eq][0];
            // I added the second condition here - matrix is rectangular, and this is the easiest way to guarantee this typechecks
            for (int coe = 1; coe < matrix[eq].length && coe < matrix[0].length; coe++) {
                int resultEq = eq - 1;
                @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/202: result is one smaller in both dimensions than matrix, and coe is an index for matrix. Also note the use of a temporary here: the Index Checker's java expression parser chokes on "result[eq - 1]"
                @IndexFor("result[resultEq]") int resultCoe = coe -1;
                result[resultEq][resultCoe] = matrix[0][coe] - matrix[eq][coe]
                        * factor;
            }
        }
        if (equations == 1) {
            return result;
        }
        @SuppressWarnings({"value", "index"}) // https://github.com/kelloggm/checker-framework/issues/158: equations != 1 -> equations >= 2 -> result is minlen(1)
        double @MinLen(1) [] @MinLen(1) [] result1 = result;

        // check for zero pivot element
        if (result1[0][0] == 0) {
            boolean found = false;
            for (int i = 0; i < result1.length; i ++) {
                if (result1[i][0] != 0) {
                    found = true;
                    double[] temp = result1[0];
                    @SuppressWarnings("index") // result1 is a rectangular array
                    @IndexOrHigh({"result1[i]", "result1[0]"}) int result1ILength = result1[i].length;
                    System.arraycopy(result1[i], 0, result1[0], 0, 
                            result1ILength);
                    @SuppressWarnings("index") // result1 is a rectangular array
                    @IndexOrHigh({"result1[i]", "temp", "result1[0]"}) int tempLen = temp.length;
                    System.arraycopy(temp, 0, result1[i], 0, tempLen);
                    break;
                }
            }
            if (!found) {
                //System.out.println("Equation has no solution!");
                return new double[equations - 1][coefficients - 1];
            }
        }
        double[][] subMatrix = calculateSubMatrix(result1);
        for (int eq = 1; eq < equations -  1; eq++) {
            result1[eq][0] = 0;
            for (int coe = 1; coe < result1[eq].length; coe++) {
                @SuppressWarnings("index") // subMatrix is always one smaller than result1
                double submatrixresult1 = subMatrix[eq - 1][coe - 1];
                result1[eq][coe] = submatrixresult1;
            }
        }
        return result1;
    }

}
