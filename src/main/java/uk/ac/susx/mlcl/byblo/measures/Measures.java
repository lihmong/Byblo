/*
 * Copyright (c) 2010-2013, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo.measures;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.BybloSettings;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.NullWeighting;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static java.lang.Math.min;

/**
 * Static utility class providing common functionality to various similarity
 * measures.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Immutable
@CheckReturnValue
public abstract class Measures {

    private static final Log LOG = LogFactory.getLog(Measures.class);

    /**
     * Static utility class should not be instantiated.
     */
    private Measures() {
    }

    /**
     * Produce the multi-set intersection of the feature vectors
     * <tt>vectorA</tt> and <tt>vectorB</tt>. Calculated as the sum of the of
     * the minimum value at each index.
     *
     * @param vectorA first feature vector
     * @param vectorB second feature vector
     * @return multi-set intersection of feature vectors
     */
    public static double intersection(final SparseDoubleVector vectorA,
                                      final SparseDoubleVector vectorB) {
        Checks.checkNotNull("vectorA", vectorA);
        Checks.checkNotNull("vectorB", vectorB);
        double shared = 0;
        int i = 0;
        int j = 0;
        while (i < vectorA.size && j < vectorB.size) {
            if (vectorA.keys[i] < vectorB.keys[j]) {
                i++;
            } else if (vectorA.keys[i] > vectorB.keys[j]) {
                j++;
            } else {
                shared += min(vectorA.values[i], vectorB.values[j]);
                i++;
                j++;
            }
        }
        return shared;
    }

    /**
     * Produce the multi-set union of the feature vectors <tt>vectorA</tt> and
     * <tt>vectorB</tt>. Calculated as the sum of the multi-set cardinality of
     * both vectors, minute the intersection.
     *
     * @param vectorA first feature vector
     * @param vectorB second feature vector
     * @return multi-set union of vectors
     */
    public static double union(final SparseDoubleVector vectorA,
                               final SparseDoubleVector vectorB) {
        Checks.checkNotNull("vectorA", vectorA);
        Checks.checkNotNull("vectorB", vectorB);
        return cardinality(vectorA) + cardinality(vectorB)
                - intersection(vectorA, vectorB);
    }

    /**
     * Return the multi-set cardinality of the vector, which is the sum.
     *
     * @param vector feature vector to calculate cardinality of
     * @return cardinality
     */
    public static double cardinality(final SparseDoubleVector vector) {
        Checks.checkNotNull("vector", vector);
        return vector.sum;
    }

    /**
     * Calculate the inner product of vectors <tt>vectorA</tt> and
     * <tt>vectorB</tt>.
     *
     * @param vectorA first feature vector
     * @param vectorB second feature vector
     * @return inner product
     */
    public static double dotProduct(final SparseDoubleVector vectorA,
                                    final SparseDoubleVector vectorB) {
        Checks.checkNotNull("vectorA", vectorA);
        Checks.checkNotNull("vectorB", vectorB);
        double numerator = 0;
        int i = 0;
        int j = 0;
        while (i < vectorA.size && j < vectorB.size) {
            if (vectorA.keys[i] < vectorB.keys[j]) {
                i++;
            } else if (vectorA.keys[i] > vectorB.keys[j]) {
                j++;
            } else {
                numerator += vectorA.values[i] * vectorB.values[j];
                i++;
                j++;
            }
        }
        return numerator;
    }

    /**
     * Calculate the squared length of the vector; i.e the inner product of the
     * vector with itself.
     *
     * @param vector vector to calculate the squared length of
     * @return squared length
     */
    public static double lengthSquared(final SparseDoubleVector vector) {
        Checks.checkNotNull("vector", vector);
        double normSquared = 0;
        for (int i = 0; i < vector.size; i++)
            normSquared += vector.values[i] * vector.values[i];
        return normSquared;
    }

    /**
     * Return the length of the vector (i.e the vector normal) calculated as the
     * square-root of the inner product of the vector with itself.
     *
     * @param vector vector to calculate the length of
     * @return length
     */
    public static double length(final SparseDoubleVector vector) {
        Checks.checkNotNull("vector", vector);
        return (vector.size == 0) ? 0
                : Math.sqrt(lengthSquared(vector));
    }

    public static Measure autoWeighted(Measure measure, Weighting weighting) {
        Checks.checkNotNull("measure", measure);
        if (weighting.getClass().equals(NullWeighting.class)) {
            return measure;
        } else {
            return new AutoWeightingMeasure(measure, weighting);
        }
    }

    public static Measure reverse(Measure measure) {
        Checks.checkNotNull("measure", measure);
        if (measure.isCommutative()) {
            if (LOG.isWarnEnabled())
                LOG.warn("Attempting to reverse a commutative measure.");
            return measure;
        } else {
            return new ReversedMeasure(measure);
        }

    }

    /**
     * Constant to aid conversion to base 2 logarithms.
     * <p/>
     * Conceptually it doesn't really matter what base is used, but 2 is the
     * standard base for most information theoretic approaches.
     * <p/>
     * TODO: Move to mlcl-lib/MathUtil
     */
    private static final double LOG_2 = Math.log(2.0);

    /**
     * Return the base 2 logarithm of the parameter v.
     * <p/>
     * TODO: Move to mlcl-lib/MathUtil
     *
     * @param v some values
     * @return logarithm of the value
     */
    public static double log2(final double v) {
        return Math.log(v) / LOG_2;
    }

    /**
     * Small value used to measure equality of double precision floating point
     * numbers while avoiding floating point errors.
     * <p/>
     * TODO: Move to mlcl-lib
     */
    private static final double DEFAULT_EPSILON = 0.0000001;

    /**
     * Check that two floating point numbers are equal within error epsilon.
     * <p/>
     * TODO: Move to mlcl-lib
     *
     * @param a       first value
     * @param b       second value
     * @param epsilon maximum difference error
     * @return true if operands are within <tt>epsilon</tt>.
     */
    public static boolean epsilonEquals(final double a, final double b,
                                        final double epsilon) {
        return Double.compare(a, b) == 0
                || Math.abs(a - b) <= epsilon;
    }

    /**
     * Check that two floating point numbers are equal within
     * {@link #DEFAULT_EPSILON }.
     * <p/>
     * TODO: Move to mlcl-lib
     *
     * @param a first value
     * @param b second value
     * @return true if operands are within {@link #DEFAULT_EPSILON}.
     */
    public static boolean epsilonEquals(final double a, final double b) {
        return epsilonEquals(a, b, DEFAULT_EPSILON);
    }

    public static Map<String, Class<? extends Measure>> loadMeasureAliasTable()
            throws ClassNotFoundException {

        // Map that will store measure aliases to class
        final Map<String, Class<? extends Measure>> classLookup = new HashMap<String, Class<? extends Measure>>();

        // There should be a properties file in the same package as the Measure interface
        final ResourceBundle res = ResourceBundle.getBundle(Measure.class.getPackage().getName() + ".measures");

        for (String measure : res.getString("measures").split(",")) {
            measure = measure.trim();

            final String classKey = "measure." + measure + ".class";
            final String aliasesKey = "measure." + measure + ".aliases";

            final String className = res.getString(classKey);
            if (!Measure.class.isAssignableFrom(Class.forName(className))) {
                throw new ClassCastException("Class does not implement Measure interface.");
            }

            final Class<? extends Measure> clazz = (Class<? extends Measure>) Class.forName(className);
            if (classLookup.put(measure.toLowerCase(BybloSettings.getLocale()), clazz) != null) {
                throw new IllegalStateException("Duplicate measure name: " + measure);
            }

            if (res.containsKey(aliasesKey)) {
                for (String alias : res.getString(aliasesKey).split(",")) {
                    alias = alias.toLowerCase(BybloSettings.getLocale()).trim();
                    final Class<? extends Measure> prevClass = classLookup.put(alias, clazz);
                    if (prevClass != null && prevClass != clazz) {
                        throw new IllegalStateException("Duplicate measure name \"" + measure
                                + "\"; old class = " + prevClass.getName() + ", new class = " + clazz);
                    }
                }
            }
        }
        return classLookup;
    }
}
