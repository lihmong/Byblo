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
package uk.ac.susx.mlcl.testing;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.Assert.*;

public class AbstractTest {
    @Rule
    public final TestName testName = new TestName();

    // An irritating problem is that is that there is no way to insure that
    // subclasses don't override setup and tear-down methods, especially the
    // static ones.

    @Before
    public void setUp() throws Exception {
        System.out.println(MessageFormat.format("Running unit-test: {0}#{1}",
                this.getClass().getName(), testName.getMethodName()));
    }


    /**
     * TODO: Probably useful enough to move to a general purpose library
     *
     * @param obj
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    protected static <T> T cloneWithSerialization(final T obj) {

        ObjectOutputStream objectsOut = null;
        ObjectInputStream ois = null;
        try {
            try {
                final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                objectsOut = new ObjectOutputStream(bytesOut);

                objectsOut.writeObject(obj);
                objectsOut.flush();

                final byte[] bytes = bytesOut.toByteArray();

                ois = new ObjectInputStream(new ByteArrayInputStream(bytes));

                return (T) ois.readObject();
            } finally {
                if (objectsOut != null)
                    objectsOut.close();
                if (ois != null)
                    ois.close();
            }
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }


    protected static <T> T clone(T obj) {
        try {

            assertTrue("doesn't implement Cloneable", obj instanceof Cloneable);

            final Method cloneMethod = obj.getClass().getMethod("clone");

            assertTrue("clone() is not public", Modifier.isPublic(cloneMethod.getModifiers()));
            assertFalse("clone() is abstract", Modifier.isAbstract(cloneMethod.getModifiers()));
            assertFalse("clone() is static", Modifier.isStatic(cloneMethod.getModifiers()));

            final Object result = cloneMethod.invoke(obj);

            assertEquals("cloned instance class different", result.getClass(), obj.getClass());
            assertEquals("cloned object not equal to original", obj, result);

            return (T) result;

        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }

    }

    protected static void assertExhaustedIterator(Iterator<?> it) {
        try {
            it.next();
            fail("Expected iterator to be exhausted by next() succeeded.");
        } catch (NoSuchElementException e) {
            // this is supposed to happen
        }
    }


    protected static Random newRandom() {
        Random rand = new Random();
        final int seed = rand.nextInt();
        System.out.println(" > random seed = " + seed);
        rand = new Random(seed);
        return rand;
    }

    protected static IntArrayList randomIntArrayList(Random rand, int maxValue,
                                                     int size) {
        return IntArrayList.wrap(randomIntArray(rand, maxValue, size));

    }

    private static int[] randomIntArray(Random rand, int maxValue, int size) {
        final int[] arr = new int[size];
        for (int i = 0; i < size; i++)
            arr[i] = rand.nextInt(maxValue);
        return arr;
    }

}
