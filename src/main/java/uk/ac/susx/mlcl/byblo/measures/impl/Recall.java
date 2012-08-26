/*
 * Copyright (c) 2010-2012, University of Sussex
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
package uk.ac.susx.mlcl.byblo.measures.impl;

import java.io.Serializable;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import uk.ac.susx.mlcl.byblo.measures.DecomposableMeasure;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.PositiveWeighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * This measure attempts to encapsulate, for each pair of feature vectors A and
 * B, how strongly A is described by B. A high similarity is returned when all
 * the features of A, also occur in B. Conversely a low similarity is returned
 * when fews of A's features occur in B. The resultant similarity will be in the
 * range 0 (completed different) to 1 (exactly the same).
 * <p/>
 * An intuitive analogy is that of the words "orange" and and "fig". "orange" is
 * a very common word and can have several difference senses, e.g: colour,
 * fruit, company name. On the other hand "fig" is relatively infrequent and is
 * nearly always used in the sense of fruit. Hence we can say that "fig" is
 * wholly described by "apple", "apple" recalls fully the senses of "fig", and
 * so recall("fig", "apple") will be high. Conversely "apple" is only partially
 * described by "fig", "fig" does not recall fully the senses of "apple", and
 * recall("apple","fig") will be low.
 * <p/>
 * One complication is that the measure does not just use raw frequencies, but
 * the positive information content of each feature. Intuitively, a feature is
 * set to occur if it's occurrence is significant w.r.t to some base-line
 * probabilities. This is calculated as the positive point-wise mutual
 * information content SI(x,y), where x is the feature vector and y is the
 * context frequencies taken from the whole corpus.
 * <p/>
 * Note that recall(x,y) = precision(y,x) so the precision can be calculated by
 * wrapping an instance of RecallMi in the ReversedProximity decorator.
 * <p/>
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Immutable
@CheckReturnValue
public class Recall extends DecomposableMeasure implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public double shared(SparseDoubleVector A, SparseDoubleVector B) {
		double numerator = 0.0;

		int i = 0;
		int j = 0;
		while (i < A.size && j < B.size) {
			if (A.keys[i] < B.keys[j]) {
				++i;
			} else if (A.keys[i] > B.keys[j]) {
				++j;
			} else {
				if (A.values[i] > 0 && B.values[j] > 0)
					numerator += A.values[i];
				++i;
				++j;
			}
		}

		return numerator;
	}

	@Override
	public double left(SparseDoubleVector A) {
		double denominator = 0.0;
		for (int i = 0; i < A.size; i++) {
			if (A.values[i] > 0)
				denominator += A.values[i];
		}
		return denominator;
	}

	@Override
	public double right(SparseDoubleVector B) {
		return 0;
	}

	@Override
	public double combine(double shared, double left, double right) {
		return shared == 0 ? 0 : shared / left;
	}

	@Override
	public boolean isCommutative() {
		return false;
	}

	@Override
	public double getHomogeneityBound() {
		return 1.0;
	}

	@Override
	public double getHeterogeneityBound() {
		return 0.0;
	}

	@Override
	public Class<? extends Weighting> getExpectedWeighting() {
		return PositiveWeighting.class;
	}

	@Override
	public String toString() {
		return "Recall";
	}

	@Override
	public int hashCode() {
		return 47;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || (obj != null && getClass() == obj.getClass());
	}
}
