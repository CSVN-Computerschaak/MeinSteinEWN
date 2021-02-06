/*
 * MeinStein Copyright (c) 2007-2009 by Theo van der Storm
 *
 * This code is published on the CSVN website (http://www.csvn.nl)
 * to commemorate Theo. This is done with consent of Theo's family.
 *
 * The software is AS IS and under GPL v3, see below.
 *
 * This file is part of MeinStein Connect6.
 *
 * MeinStein is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MeinStein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MeinStein Connect6.  If not, see <http://www.gnu.org/licenses/>.
 */

public class StatV {

    static final String tabs = "\t\t\t\t";
    static final int CAPTURED = 0,  depth = 6;
    static final int IND_5P6 = 5 * 5 * 5 * 5 * 5 * 5;
    static int[] plyOut = new int[10];
    static int[] dist = new int[6];
    static int[] prob = new int[6];
    static int pInd = 0;
    static boolean verbose = true;
    static float[][] pOut = new float[IND_5P6][depth];

    public static void init() {
        verbose = false;
        generate();	// for playing
    }

    public static void main(String[] args) {
        generate();	// for testing the generation process
    }

    public static float winP(int i1, int i2, boolean verb) {
        // i1 to move. Return win chance for i2.
        float p = 0.5f, m = 1f;
        for (int d = 0; d < depth && m > 0; d++) {
            if (verb) {
                System.out.println(pOut[i1][d] + " " + pOut[i2][d]);
            }
            p -= m * pOut[i1][d];
            m *= (1f - pOut[i1][d]) * (1f - pOut[i2][d]);
        }
        return p - m * 0.5f;
    }

    public static float winPs(int i1, int i2, boolean verb) {
        // i1 to move. Return win chance for i2.
        return 0.5f - pOut[i1][0];
    }

    static void generate() {
        for (dist[5] = 0; dist[5] < 5; dist[5]++) {
            for (dist[4] = 0; dist[4] < 5; dist[4]++) {
                for (dist[3] = 0; dist[3] < 5; dist[3]++) {
                    for (dist[2] = 0; dist[2] < 5; dist[2]++) {
                        for (dist[1] = 0; dist[1] < 5; dist[1]++) {
                            for (dist[0] = 0; dist[0] < 5; dist[0]++) {
                                doCalc();
                                pInd++;
                            }
                        }
                    }
                }
            }
        }
    }

    static void doCalc() {
        int e = 0, low = -1, p = 6, rem;
        for (int k = 0; k < plyOut.length; k++) {
            plyOut[k] = 0;
        }
        for (int t = 0; t < dist.length; t++) {
            if (dist[t] == CAPTURED) {
                prob[t] = 0;
                e++;
            } else {
                if (low >= 0) {
                    prob[low] += e;
                }
                prob[low = t] = e + 1;
                e = 0;
            }
        }
        if (low < 0) {
            for (int k = 0; k < depth; k++) {
                pOut[pInd][k] = 0;
            }
            return;	// No stones
        }
        prob[low] += e;
        calc(1);

        for (int k = depth; k >= 1; k--) {
            plyOut[k] *= p;
            p *= 6;
        }
        e = 0;
        for (int k = 1; k < plyOut.length; k++) {
            e += k * plyOut[k];
        }
        rem = p;
        if (verbose) {
            System.out.print("{/*" + dist[5] + dist[4] + dist[3] + dist[2] + dist[1] + dist[0] + "*/ ");
        }
        for (int k = 1; k <= depth; k++) {
            pOut[pInd][k - 1] = rem == 0 ? 1f : plyOut[k] / (float) rem;
            rem -= plyOut[k];
            if (verbose) {
                System.out.print(pOut[pInd][k - 1] + ", ");
            }
        }
        if (verbose) {
            System.out.println("},");
        }
    }

    static void calc(int ply) {
        for (int t = 0; t < 6; t++) {
            int smin = 0, smax = 5;
            for (int k = t; k >= smin; k--) {
                if (dist[k] != CAPTURED) {
                    smin = k;
                }
            }
            for (int k = t; k <= smax; k++) {
                if (dist[k] != CAPTURED) {
                    smax = k;
                }
            }
            if (dist[smin] == CAPTURED) {
                smin = smax;
            } else if (dist[smax] != CAPTURED) {
                if (6 * dist[smax] - prob[smax] < 6 * dist[smin] - prob[smin]) {
                    smin = smax;
                }
            }
            dist[smin]--;
            if (dist[smin] == 0) {
                plyOut[ply]++;
            } else {
                if (ply < depth) {
                    calc(ply + 1);
                } else {
                    plyOut[ply + 1] += 6;
                }
            }
            dist[smin]++;
        }
    }
} // StatV
