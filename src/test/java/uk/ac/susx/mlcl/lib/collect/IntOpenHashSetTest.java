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
package uk.ac.susx.mlcl.lib.collect;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class IntOpenHashSetTest {

    @Test
    @Ignore(value = "Currently fails because of a bug in IntOpenHashSet")
    public void testStrangeRetainAllCase() {

        IntArrayList initialElements = IntArrayList.wrap(new int[]{586, 940,
                1086, 1110, 1168, 1184, 1185, 1191, 1196, 1229, 1237, 1241,
                1277, 1282, 1284, 1299, 1308, 1309, 1310, 1314, 1328, 1360,
                1366, 1370, 1378, 1388, 1392, 1402, 1406, 1411, 1426, 1437,
                1455, 1476, 1489, 1513, 1533, 1538, 1540, 1541, 1543, 1547,
                1548, 1551, 1557, 1568, 1575, 1577, 1582, 1583, 1584, 1588,
                1591, 1592, 1601, 1610, 1618, 1620, 1633, 1635, 1653, 1654,
                1655, 1660, 1661, 1665, 1674, 1686, 1688, 1693, 1700, 1705,
                1717, 1720, 1732, 1739, 1740, 1745, 1746, 1752, 1754, 1756,
                1765, 1766, 1767, 1771, 1772, 1781, 1789, 1790, 1793, 1801,
                1806, 1823, 1825, 1827, 1828, 1829, 1831, 1832, 1837, 1839,
                1844, 2962, 2969, 2974, 2990, 3019, 3023, 3029, 3030, 3052,
                3072, 3074, 3075, 3093, 3109, 3110, 3115, 3116, 3125, 3137,
                3142, 3156, 3160, 3176, 3180, 3188, 3193, 3198, 3207, 3209,
                3210, 3213, 3214, 3221, 3225, 3230, 3231, 3236, 3240, 3247,
                3261, 4824, 4825, 4834, 4845, 4852, 4858, 4859, 4867, 4871,
                4883, 4886, 4887, 4905, 4907, 4911, 4920, 4923, 4924, 4925,
                4934, 4942, 4953, 4957, 4965, 4973, 4976, 4980, 4982, 4990,
                4993, 6938, 6949, 6953, 7010, 7012, 7034, 7037, 7049, 7076,
                7094, 7379, 7384, 7388, 7394, 7414, 7419, 7458, 7459, 7466,
                7467});

        IntArrayList retainElements = IntArrayList.wrap(new int[]{586});

        // Initialize both implementations with the same data
        IntOpenHashSet instance = new IntOpenHashSet(initialElements);
        IntRBTreeSet referenceInstance = new IntRBTreeSet(initialElements);

        instance.retainAll(retainElements);
        referenceInstance.retainAll(retainElements);

        // print the correct result {586}
        System.out.println("ref: " + referenceInstance);

        // prints {586, 7379}, which is clearly wrong
        System.out.println("ohm: " + instance);

        // Fails
        assertEquals(instance, referenceInstance);
    }

}
