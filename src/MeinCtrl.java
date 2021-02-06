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

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;		// Use swing Timer
import java.io.*;
import java.text.NumberFormat;
import java.util.Random;	// NOT: util Timer
import java.util.Arrays;
import java.util.Calendar;

public class MeinCtrl extends JPanel implements ActionListener {

    static final String tabs = "\t\t\t\t";
    static final float WON = 0.5f,  LOST = -0.5f;
    static final int[] pow5 = {
        0, 1, 5, 25, 125, 625, 3125, 15625, 78125
    };
    static final int[] pow6 = {
        1, 6, 36, 216, 1296, 7776, 46656, 279936, 1679616, 10077696
    };
    static final int[] sqValue = {
        360, 36, 36, 36, 36,
        36, 360, 360, 360, 360,
        36, 360, 3600, 3600, 3600,
        36, 360, 3600, 36000, 36000,
        36, 360, 3600, 36000, 360000
    };
    static final int[] sqFreeQ = {
        -6, -6, -6, 999, 9999,
        -6, -6, -6, 3, 3,
        -6, -6, 2, 2, 2,
        999, 3, 2, 2, 2,
        9999, 3, 2, 2, 2,};
    static final int[] sqEcc = {
        0, 1, 2, 3, 4,
        1, 0, 1, 2, 3,
        2, 1, 0, 1, 2,
        3, 2, 1, 0, 1,
        4, 3, 2, 1, 0
    };
    static final int[] sqDist = {
        0, 1, 2, 3, 4,
        1, 1, 2, 3, 4,
        2, 2, 2, 3, 4,
        3, 3, 3, 3, 4,
        4, 4, 4, 4, 4
    };
    static final int[][] stoneVal = { // indexed by position (0-24) and mobility (1-6)
        /*  0 */{0, 999, 999, 999, 999, 999, 999},
        /*  1 */ {0, 132, 77, 54, 42, 34, 29},
        /*  2 */ {0, 271, 158, 112, 86, 70, 59},
        /*  3 */ {0, 413, 241, 170, 131, 107, 90},
        /*  4 */ {0, 554, 323, 228, 176, 144, 121},
        /*  5 */ {0, 132, 77, 54, 42, 34, 29},
        /*  6 */ {0, 143, 83, 59, 45, 37, 31},
        /*  7 */ {0, 279, 163, 115, 89, 72, 61},
        /*  8 */ {0, 418, 244, 172, 133, 108, 91},
        /*  9 */ {0, 559, 326, 230, 178, 145, 122},
        /* 10 */ {0, 271, 158, 112, 86, 70, 59},
        /* 11 */ {0, 279, 163, 115, 89, 72, 61},
        /* 12 */ {0, 286, 167, 118, 91, 74, 63},
        /* 13 */ {0, 423, 247, 174, 135, 110, 93},
        /* 14 */ {0, 563, 328, 232, 179, 146, 123},
        /* 15 */ {0, 413, 241, 170, 131, 107, 90},
        /* 16 */ {0, 418, 244, 172, 133, 108, 91},
        /* 17 */ {0, 423, 247, 174, 135, 110, 93},
        /* 18 */ {0, 429, 250, 176, 136, 111, 94},
        /* 19 */ {0, 567, 331, 234, 180, 147, 124},
        /* 20 */ {0, 554, 323, 228, 176, 144, 121},
        /* 21 */ {0, 559, 326, 230, 178, 145, 122},
        /* 22 */ {0, 563, 328, 232, 179, 146, 123},
        /* 23 */ {0, 567, 331, 234, 180, 147, 124},
        /* 24 */ {0, 571, 333, 235, 182, 148, 125},};
    static int sqFreePar = 6,  sqFreeVal[] = new int[25];
    static int[] reachXS = new int[25];	// X stone on a square can reach
    static int[] reachYS = new int[25];	// Y stone on a square can reach
    static int logLevel = 3;
    private static final long serialVersionUID = -7304886522873754605L;
    MeinDisplay disp;
    JTextArea gameNotation = new JTextArea("game notation", 34, 34);
    JTextArea messText = new JTextArea();
    JScrollPane messages = new JScrollPane(messText);
    JTextField opponent = new JTextField("stein234");
    JTextField freePar = new JTextField("MeinStein_c");
    JTextField cutTime = new JTextField("3500");
    JTextField matLen = new JTextField("50");
    JButton newB = new JButton("New");
    JButton setupB = new JButton("Norm / Setup");	// Normal   / Setup mode
    JButton grayB = new JButton("Colo / Gray");	// Coloured / Gray mode
    JButton anaB = new JButton("Play / Ana");	// Play     / Analyse mode
    JButton flipB = new JButton("Norm / Flip");	// Normal   / flipped board mode
    JButton engB = new JButton("Game/EngMatch");
    JButton backB = new JButton("Back");
    JButton forwB = new JButton("Forward");
    JButton castB = new JButton("Cast");
    JButton pasteB = new JButton("Paste");
    JButton annotB = new JButton("Annotate");
    JTextField movTime = new JTextField("0");
    JTextField curScore = new JTextField("");
    JButton currB = new JButton("Colour");
    JButton d1B = new JButton("1");
    JButton d2B = new JButton("2");
    JButton d3B = new JButton("3");
    JButton d4B = new JButton("4");
    JButton d5B = new JButton("5");
    JButton d6B = new JButton("6");
    JButton calcB = new JButton("Calc");
    JButton exitB = new JButton("Exit");
    JTextField inetIO = new JTextField("Input");
    JButton inetB = new JButton("inetPlay");
    JPanel aPan = new JPanel(new GridLayout(7, 1, 2, 2));
    JPanel bPan = new JPanel(new GridLayout(7, 3, 2, 2));
    Random rnd = new Random();

    //	X is White, Y is Black.
    // Mode variables
    boolean anaMode = false, flippedMode = true, coloMode = true;
    boolean matchMode = false, whiteSetupMode = false, annotMode = false;
    int setupStone = 0;
    int highFR = -1, highTO = -1;
    NumberFormat nf = NumberFormat.getInstance();
    LogWriter htmlLog;
    Position cur = new Position();
    Game curGame;
    Move curMove, bestMove = null;
    boolean bestExact = true;
    int matchRemain = 0, round = 0;
    int won1X = 0, won1Y = 0, won2X = 0, won2Y = 0;
    int currentEval = 1, matchLength = 50;
    long time0, timeE, cutOffTime = 3500L;
    int thrown;
    int countGoalVal, countNodeVal, countMoveVal, countBetaVal;
    boolean inetFlip = false;
    int inetPlayer = 0, inetTable = 0, inetSeat = 0;
    InetClient client = null;
    String myEval, appletPass = null;
    int inetID = 0;
    TranspositionTable ht;
    String pasteString = null, pgnSaved = "";
    int pasteIndex;
    Timer moveTimer = new Timer(20, new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
            int c;
            // System.out.println(new Date().toString());

            if ((curMove = ana()) == null) {
                System.out.println("timer code bug");
                moveTimer.stop();
                matchRemain = 0;
                return;
            }
            play(curMove);
            if (curGame.result.length() > 1) {
                if ((cur.ply + matchRemain) % 2 == 0) {
                    if (cur.ply % 2 == 0) {
                        won2Y++;
                    } else {
                        won2X++;
                    }
                } else if (cur.ply % 2 == 0) {
                    won1Y++;
                } else {
                    won1X++;
                }
                matchStatus(won1X + "+" + won1Y + " - " + won2X + "+" + won2Y);
                if (matchMode && --matchRemain > 0) {
                    // Continue with the next game at the next timer event.
                    engineMatch(null, curGame.black, curGame.white);
                } else {
                    matchMode = false;
                    moveTimer.stop();
                    if (logLevel >= 1) {
                        System.out.println(curGame.black + padInt(won1X, 4) + padInt(won1Y, 4));
                        System.out.println(curGame.white + padInt(won2X, 4) + padInt(won2Y, 4));
                    }
                    curGame.log(curGame.black + "-" +
                        curGame.white + ": " + (won1X + won1Y) + "-" + (won2X + won2Y));
                }
            }
        }
    });

    public MeinCtrl(MeinDisplay d) {
        disp = d;
        disp.refCtrl(this);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(1);
        setPreferredSize(new Dimension(750, 650));
        setLayout(new BorderLayout());

        gameNotation.setFont(new Font("Monospaced", Font.PLAIN, 10));
        gameNotation.setEditable(true);
        messText.setFont(new Font(null, Font.PLAIN, 10));
        messText.setEditable(true);

        gameNotation.setPreferredSize(new Dimension(200, 378));
        aPan.setPreferredSize(new Dimension(334, 378));
        bPan.setPreferredSize(new Dimension(310, 378));
        messages.setPreferredSize(new Dimension(500, 200));

        add(gameNotation, BorderLayout.WEST);
        add(aPan, BorderLayout.CENTER);
        add(bPan, BorderLayout.EAST);
        add(messages, BorderLayout.SOUTH);

        aPan.add(newB);
        bPan.add(pasteB);
        bPan.add(setupB);
        bPan.add(currB);
        aPan.add(flipB);
        bPan.add(backB);
        bPan.add(forwB);
        bPan.add(d1B);
        aPan.add(grayB);
        bPan.add(opponent);
        bPan.add(annotB);
        bPan.add(d2B);
        aPan.add(engB);
        bPan.add(movTime);
        bPan.add(anaB);
        bPan.add(d3B);
        aPan.add(curScore);
        bPan.add(cutTime);
        bPan.add(castB);
        bPan.add(d4B);
        aPan.add(freePar);
        bPan.add(matLen);
        bPan.add(inetB);
        bPan.add(d5B);
        aPan.add(inetIO);
        bPan.add(exitB);
        bPan.add(calcB);
        bPan.add(d6B);

        annotB.addActionListener(this);
        pasteB.addActionListener(this);
        newB.addActionListener(this);
        setupB.addActionListener(this);
        castB.addActionListener(this);
        exitB.addActionListener(this);
        calcB.addActionListener(this);
        flipB.addActionListener(this);
        grayB.addActionListener(this);
        backB.addActionListener(this);
        forwB.addActionListener(this);
        engB.addActionListener(this);
        anaB.addActionListener(this);
        opponent.addActionListener(this);
        freePar.addActionListener(this);
        matLen.addActionListener(this);
        cutTime.addActionListener(this);
        currB.addActionListener(this);
        inetIO.addActionListener(this);
        inetB.addActionListener(this);
        d1B.addActionListener(this);
        d1B.setForeground(Color.white);
        d2B.addActionListener(this);
        d2B.setForeground(Color.white);
        d3B.addActionListener(this);
        d3B.setForeground(Color.white);
        d4B.addActionListener(this);
        d4B.setForeground(Color.white);
        d5B.addActionListener(this);
        d5B.setForeground(Color.white);
        d6B.addActionListener(this);
        d6B.setForeground(Color.white);
        colorThrow(0);
        validate();
        ht = new TranspositionTable();
    }

    public void setCurrentEval(int f) {
        if (logLevel >= 5) {
            System.out.println("in:  " + currentEval);
        }
        currentEval = f;
        if (logLevel >= 5) {
            System.out.println("out: " + currentEval);
        }
    }

    public long setTime(long cot) {
        long c = cutOffTime;
        cutOffTime = cot;
        return c;		// Return old value;
    }

    public static String padInt(int i, int width) {
        String s = String.valueOf(i);
        if (width >= s.length()) {
            return "                       ".substring(0, width - s.length()) + s;
        } else {
            return " " + s;
        }
    }

    public void engineMatch(Timer t, String X, String Y) {
        cur.newStartingPos();
        curGame = new Game("@Theo", "Amsterdam NED", "", String.valueOf(++round),
            X, Y, "*", cur.toFEN(), cur.toString());
        drawBoard();
        colorThrow(rnd.nextInt(6) + 1);
        if (t != null) {
            // Timer to be started provided at the start of the match only.
            won1X = won1Y = won2X = won2Y = 0;
            t.start();
        }
    }

    float nodeValue(float max[]) {
        float e = 0f, m[] = (float[]) max.clone();
        for (int low = 0, k = 1; k < 8; k++) {
            if (logLevel >= 5) {
                System.out.print(max[k] + " ");
            }
            if (m[k] != -1f) {
                for (int f = low + 1; f < k; f++) {
                    m[f] = Math.max(m[low], m[k]);
                }
                low = k;
            }
        }
        for (int k = 1; k <= 6; k++) {
            e += m[k];
        }
        e /= 6;
        if (logLevel >= 5) {
            System.out.println(e);
        }
        return e;
    }

    /**
     * Performs a recursive search of the game tree with calculations
     * of statistical expectance.
     * @param	depth	The search depth >= 1
     * @return	the value for the best move.
     */
    float negaStat(int depth, Move[] boardMove, boolean x, float a, float b) {
        float e, a1 = LOST, gmax = -9f, max[] = new float[8];
        Stone st[] = x ? cur.xStone : cur.yStone;
        Move[] moveL;
        int n;

        countNodeVal++;
        if (boardMove == null) {
            moveL = new Move[18];
            n = cur.generate(moveL, 0, x);
        } else {
            moveL = boardMove;
            n = (int) moveL[0].val;
        }
        for (int k = 0; k < 7; k++) {
            max[k] = st[k].pos >= 0 ? LOST : -1f;	// Worst case initialisation
        }
        max[7] = LOST;

        for (int i = 0; i < n && timeE != 0L; i++) {
            if (moveL[i].to == 0 || moveL[i].to == 24) {
                countGoalVal++;
                if (boardMove != null) {
                    bestMove = moveL[i];
                    bestExact = true;
                    return WON;	// Immediate win. Caller should stop searching.
                }
                e = WON;
            } else if (depth > 1) {
                long p;
                cur.makeMove(moveL[i]);
                p = ht.calcPos(cur.xStone, cur.yStone, !x);
                e = ht.get(p, depth);
                if (e == Float.MIN_VALUE) {	// No hashtable hit
                    e = -negaStat(depth - 1, null, !x, -b, -a1);
                    if (timeE != 0L) // Store only results from non-interrupted tree search
                    {
                        ht.add(p, depth, e);
                    }
                } else {
                    bestExact = false;
                }
                cur.unMakeMove(moveL[i]);
            } else {
                countMoveVal++;
                e = moveL[i].val;
                if (!moveL[i].exact) {
                    bestExact = false;
                }
            }
            if (timeE == 0L) {
                break;
            }
            if (boardMove == null) {
                if (max[moveL[i].thrown] < e) {
                    max[moveL[i].thrown] = e;
                }
            } else {
                long t = System.currentTimeMillis();
                moveL[i].val = e;
                if (gmax < e) {
                    gmax = a1 = e;
                    bestMove = moveL[i];

                    curScore.setText(bestMove.toString() + " " + nf.format((e + WON) * 100f) + "%");
                    curScore.paintImmediately(0, 0, curScore.getWidth(), curScore.getHeight());
                    if (highFR >= 0) {
                        highLight(highFR, false);
                    }
                    if (highTO >= 0) {
                        highLight(highTO, false);
                    }
                    highLight(highFR = bestMove.fr, true);
                    highLight(highTO = bestMove.to, true);

                    if (logLevel >= 4 || anaMode) {
                        System.out.println("Ana," + padInt(depth, 2) + "," + (i + 1) + "," +
                            moveL[i].toString() + "," + padInt((int) (t - time0), 8) + "," + (e + WON) * 100f);
                    }
                } else if (anaMode) {
                    System.out.println("Ana," + padInt(depth, 2) + "," + (i + 1) + "," +
                        moveL[i].toString() + "," + padInt((int) (t - time0), 8) + "," + (e + WON) * 100f);
                }
            }
            if (depth > 3) {
                if (System.currentTimeMillis() >= timeE) {
                    timeE = 0L;
                }
            }
        }
        if (boardMove == null) {
            e = n > 0 ? nodeValue(max) : LOST;
        } else {
            e = gmax;
            if (n == 0) {
                bestMove = null;
            }
        }
        return e;
    }

    void highLight(int p, boolean high) {
        if (cur.square[p] == null) {
            disp.drawSquare(false, 0, p % 5, p / 5, high);
        } else {
            disp.drawSquare(cur.square[p].x, cur.square[p].num, p % 5, p / 5, high);
        }
    }

    public Move ana() {
        return ana(null);
    }

    public synchronized Move ana(Move played) {
        int n, d, dDepth = 1, begDepth, minDepth, maxDepth, prevBest = 0;
        long dt;
        Move[] moveL = new Move[18];
        float e, ePrev;

        if (setupStone > 0) {
            return null;
        }

        maxDepth = 20;
        minDepth = 6;	// was 4
        begDepth = minDepth;
        myEval = "";

        time0 = System.currentTimeMillis();
        timeE = time0 + (annotMode ? 1800000 : 60000);	// 60s to reach minDepth
        if (cur.ply % 2 == 0 && curGame.white.equals("eval2") ||
            cur.ply % 2 == 1 && curGame.black.equals("eval2")) {
            setCurrentEval(2);
        } else {
            setCurrentEval(1);
        }
        if ((n = cur.generate(moveL, thrown == 0 ? 1 : thrown, cur.ply % 2 == 0)) <= 1) {
            return n == 1 ? moveL[0] : null;
        }
        for (int i = 0; i < n; i++) {
            moveL[i].thrown = thrown;
        }
        countGoalVal = 0;
        countNodeVal = 0;
        countMoveVal = 0;
        countBetaVal = 0;
        for (ePrev = e = 0, d = begDepth;
            (d <= minDepth || timeE > 0L) && d <= maxDepth; d += dDepth) {
            if (d > minDepth && !annotMode) // Set cutoff time
            {
                timeE = time0 + cutOffTime;
            }
            if (bestMove != null) {
                prevBest = 25 * bestMove.fr + bestMove.to;
            }
            Arrays.sort((Object[]) moveL, 0, n, null);
            moveL[0].val = n;
            bestExact = true;
            ePrev = e;
            e = negaStat(d, moveL, cur.ply % 2 == 0, LOST, WON);
            if (bestMove == null || bestExact) {
                return bestMove;
            }
            if (annotMode) {
                // Finish whole iteration for annotation purposes, so test time afterwards.
                if (System.currentTimeMillis() >= time0 + cutOffTime) {
                    timeE = 0L;
                }
            }
        }
        if (e == -9f) {
            e = ePrev;	// first move analyses aborted due to time-out.
            d -= dDepth;
        }
        if ((d -= dDepth) > begDepth) {
            if (prevBest != 25 * bestMove.fr + bestMove.to) {
                bestMove.setComment(String.valueOf(d));
            }
        }
        timeE = 1 + System.currentTimeMillis() - time0;
        myEval = curScore.getText() + " d" + padInt(d, 2) + " " + padInt((int) timeE, 6) + "ms.";
        if (logLevel >= 3) {
            System.out.println(myEval +
                " g" + countGoalVal + " n" + countNodeVal + " m" + countMoveVal + " b" + countBetaVal + " " + bestExact + " kN/s" + countNodeVal / timeE);
        }
        if (annotMode) {
            Arrays.sort((Object[]) moveL, 0, n, null);
            if (played == null) {
                addMessage(",[FEN \"" + cur.toFEN(thrown) + "\"]," + moveL[0].toString() + "," + (moveL[0].val + WON) * 100f);
                for (int i = 1; i < n; i++) {
                    addMessage(padInt(d, 2) + "(" + (i + 1) + "/" + n + ") " +
                        moveL[i].toString() + " " + (moveL[i].val + WON) * 100f);
                }
            } else {
                for (int i = 0; i < n; i++) {
                    if (played.equals(moveL[i])) {
                        played.val = moveL[i].val;
                    }
                }
                if (moveL[0].val > played.val + 0.001 || freePar.getText().charAt(0) == 'a') {	// 0.1%
                    String csv, playedBy = cur.ply % 2 == 0 ? curGame.white : curGame.black;

                    htmlLog.add("<tr><td>\n");	// game table

                    htmlLog.add("<table style=\"width: 100%; text-align: left;\" border=\"1\" cellpadding=\"1\" cellspacing=\"0\">\n" +
                        "<caption><a href=\"http://www.littlegolem.net/jsp/tournament/tournament.jsp?trnid=" + curGame.event + "\">" + curGame.event + "</a><br>\n" +
                        curGame.round + ": " + curGame.white + " - " + curGame.black + "<br>\n" +
                        "[FEN \"" + cur.toFEN(thrown) + "\"]<br></caption>\n");
                    csv = "[FEN \"" + cur.toFEN(thrown) + "\"]," + curGame.white + "," + curGame.black + "," + curGame.round + "," + (cur.ply + 1) + "," +
                        played.toString() + "," + (played.val + WON) * 100f + "," + moveL[0].toString() + "," + d + "," + (moveL[0].val + WON) * 100f;
                    addMessage(csv);
                    htmlLog.add("<!--," + csv + ",-->\n");
                    htmlLog.add("<tbody style=\"font-family: monospace;\">\n" +
                        "<tr><th style=\"text-align: center;\">Move<br></th>\n" +
                        "<th style=\"text-align: center;\">" + d + " Ply %<br></th>\n" + // Ply Depth
                        "<th style=\"text-align: center;\">Evaluation (bar/cell ratio) </th>\n" +
                        "<th style=\"text-align: center;\">Player<br></th>   </tr>\n");
                    for (int i = 0; i < n; i++) {
                        htmlLog.add("<tr><td style=\"width: 80px;\">" + moveL[i].toString() + "</td>\n" +
                            "    <td style=\"width: 80px;\">" + (moveL[i].val + WON) * 100f + "</td>\n" +
                            "    <td><hr style=\"width: " + (moveL[i].val + WON) * 100f + "%;\" align=\"" +
                            (cur.ply % 2 == 0 ? "left" : "right") + "\"></td>\n" +
                            "    <td style=\"width: 90px;\">" +
                            (played.equals(moveL[i]) ? playedBy : "") + "<br></td> </tr>\n");
                    }
                    htmlLog.add("</tbody></table>\n");

                    htmlLog.add("</td><td style=\"width: 155px;\">\n");	// game table

                    htmlLog.add(cur.toHTML((cur.ply % 2 == 0 ? "White: " : "Black: ") + thrown));	// board position

                    htmlLog.add("</td></tr>\n");				// game table
                }
            }
            paintImmediately(0, 0, getWidth(), getHeight());
        }
        ht.report(true);
        return bestMove;
    }

    public String myEval() {
        return myEval;
    }

    public int anaPlay(boolean playMove) {	// called from InetClient
        Move m = ana();
        if (playMove) {
            play(m);
            return m == null ? 0 : (cur.square[m.to].num * 25 + m.to);
        }
        return 0;
    }

    public void showGame(String pgn) {
        gameNotation.setText(pgn);
    }

    public int play(Move cm) {
        int r = -3;		// No move. Game over
        if (cm == null) {
            return r;
        }

        cm.thrown = thrown;
        if (logLevel >= 3) {
            System.out.println(cm.toString());
        }
        cur.makeMove(cm);
        if (logLevel >= 4) {
            cur.debugOut();
        }
        disp.drawSquare(false, 0, cm.fr % 5, cm.fr / 5);
        disp.drawSquare(cur.square[cm.to].x, cur.square[cm.to].num, cm.to % 5, cm.to / 5);
        curGame.add(cm);
        if (cm.to == 0 || cm.to == 24 ||
            cur.xStone[0].hig > 6 || cur.yStone[0].hig > 6) {
            // Opponent reached the goal or captured my last stone.
            curGame.setResult(cur.ply % 2 == 0 ? "0-1" : "1-0");
            if (!anaMode) {
                curGame.save();
                disp.flipBoard(flippedMode = !flippedMode);
                flipB.setText(flippedMode ? "Flip / Norm" : "Norm / Flip");
                drawBoard();
            }
            if (logLevel >= 1) {
                System.out.println("Game over.");
            }
        } else {
            boolean x = cur.ply % 2 == 0;
            if (x && cur.xStone[cur.xStone[0].hig].hig <= 6 ||
                !x && cur.yStone[cur.yStone[0].hig].hig <= 6) {
                if (client == null) {
                    colorThrow(rnd.nextInt(6) + 1);
                }
                r = -1;	// Roll
            } else {
                colorThrow(0);
                r = -2;	// Einstein
            }
        }
        showGame(curGame.getPgn());
        return r;
    }

    public int tryMoveN(int num, int p2) {
        int p1 = (cur.ply % 2 == 0 ? cur.xStone : cur.yStone)[num].pos;
        return tryMove(p1, p2, true);
    }

    public int tryMove(int p1, int p2) {
        return tryMove(p1, p2, false);
    }

    public int tryMove(int p1, int p2, boolean debug) {
        if (setupStone > 0) {
            if (p2 >= 0 && p2 < 25) {
                cur.add(whiteSetupMode, setupStone, p2);
                if (cur.square[p2] == null) {
                    disp.drawSquare(false, 0, p2 % 5, p2 / 5);
                } else {
                    disp.drawSquare(whiteSetupMode, cur.square[p2].num, p2 % 5, p2 / 5);
                    colorThrow(setupStone == 6 ? 1 : setupStone + 1);
                }
            }
        } else {
            Move chosen = null, moveL[] = new Move[18];
            int n, match = 0;
            if ((n = cur.generate(moveL, thrown == 0 ? 1 : thrown, cur.ply % 2 == 0)) <= 0) {
                return -1;
            }
            for (int i = 0; i < n; i++) {
                if (debug) {
                    System.out.print(" " + moveL[i].toString());
                    if (i % 6 == 5 || i == n - 1) {
                        System.out.println();
                    }
                }
                if ((p1 < 0 || moveL[i].fr == p1) && moveL[i].to == p2 ||
                    moveL[i].fr == p2 && moveL[i].to == p1) {
                    chosen = moveL[i];
                    match++;
                }
            }
            if (match == 1) {
                return play(cur.newMove(cur.square[chosen.fr], chosen.to));
            // -1 Caller should forget about the p2 click
            }
        }
        return p2;
    }

    void colorThrow(int t) {
        if (t == 0) {
            thrown = 0;
        } else if (logLevel >= 3) {
            System.out.println("Throw: " + t);
        }
        switch (thrown) {
            case 0:
            case 1:
                d1B.setBackground(Color.gray);
                if (thrown > 0) {
                    break;
                }
            case 2:
                d2B.setBackground(Color.gray);
                if (thrown > 0) {
                    break;
                }
            case 3:
                d3B.setBackground(Color.gray);
                if (thrown > 0) {
                    break;
                }
            case 4:
                d4B.setBackground(Color.gray);
                if (thrown > 0) {
                    break;
                }
            case 5:
                d5B.setBackground(Color.gray);
                if (thrown > 0) {
                    break;
                }
            case 6:
                d6B.setBackground(Color.gray);
                if (thrown > 0) {
                    break;
                }
        }
        switch (thrown = t) {
            case 1:
                d1B.setBackground(cur.ply % 2 == 0 ? disp.lightStone : disp.darkStone);
                break;
            case 2:
                d2B.setBackground(cur.ply % 2 == 0 ? disp.lightStone : disp.darkStone);
                break;
            case 3:
                d3B.setBackground(cur.ply % 2 == 0 ? disp.lightStone : disp.darkStone);
                break;
            case 4:
                d4B.setBackground(cur.ply % 2 == 0 ? disp.lightStone : disp.darkStone);
                break;
            case 5:
                d5B.setBackground(cur.ply % 2 == 0 ? disp.lightStone : disp.darkStone);
                break;
            case 6:
                d6B.setBackground(cur.ply % 2 == 0 ? disp.lightStone : disp.darkStone);
                break;
        }
        if (setupStone > 0) {
            setupStone = t;
        }
    }

    void golemText(String gs) {
        char c[] = new char[25];
        String hEvent, hSite, hBlue, hRed, hResult;
        String hWhite, hBlack, fen, g1;

        int i = 0, roundx = 1, e0 = 0, e1 = 0, flip;
        System.out.println("golemText: " + gs);

        while (e1 >= 0 && (e0 = gs.indexOf("[Event", e1)) >= 0) {
            if ((e1 = gs.indexOf("[Event", e0 + 1)) >= 0) {
                g1 = gs.substring(e0, e1);
            } else {
                g1 = gs.substring(e0);
            }
            hEvent = parseHeader(g1, "Event");
            if (hEvent.startsWith("Tournament ")) {
                hEvent = hEvent.substring(11);
            }
            hSite = parseHeader(g1, "Site");
            hBlue = parseHeader(g1, "Blue");
            hRed = parseHeader(g1, "Red");
            hResult = parseHeader(g1, "Result");
            System.out.println("golemGame: " + g1);
            i = 0;
            while ((i = g1.indexOf(" [", i)) >= 0) {
                //	[FEN "cda2/eb3/f3E/3AB/2CDF w 1"]
                //	    -10123456789012345678901234
                //	    1. [213456,642351]
                if (g1.charAt(i - 1) == '.') {
                    // Odd ply number: Blue starts.
                    flip = 0;
                    hWhite = hBlue;
                    hBlack = hRed;
                } else {
                    flip = 7;
                    hWhite = hRed;
                    hBlack = hBlue;
                }
                c[0] = (char) ('a' + g1.charAt(i + 9 - flip) - '1');
                c[1] = (char) ('a' + g1.charAt(i + 10 - flip) - '1');
                c[2] = (char) ('a' + g1.charAt(i + 11 - flip) - '1');
                c[3] = '2';
                c[4] = '/';
                c[5] = (char) ('a' + g1.charAt(i + 12 - flip) - '1');
                c[6] = (char) ('a' + g1.charAt(i + 13 - flip) - '1');
                c[7] = '3';
                c[8] = '/';
                c[9] = (char) ('a' + g1.charAt(i + 14 - flip) - '1');
                c[10] = '3';
                c[11] = (char) ('A' + g1.charAt(i + 7 + flip) - '1');
                c[12] = '/';
                c[13] = '3';
                c[14] = (char) ('A' + g1.charAt(i + 6 + flip) - '1');
                c[15] = (char) ('A' + g1.charAt(i + 5 + flip) - '1');
                c[16] = '/';
                c[17] = '2';
                c[18] = (char) ('A' + g1.charAt(i + 4 + flip) - '1');
                c[19] = (char) ('A' + g1.charAt(i + 3 + flip) - '1');
                c[20] = (char) ('A' + g1.charAt(i + 2 + flip) - '1');
                c[21] = ' ';
                c[22] = 'w';
                c[23] = ' ';
                c[24] = '1';
                i += 2;
                fen = new String(c);
                if (cur.newStartingPos(fen)) {
                    curGame = new Game(
                        hEvent, hSite, "", String.valueOf(roundx++),
                        hWhite, hBlack, hResult, cur.toFEN(), cur.toString());
                    anaMode = true;
                    anaB.setText("Ana / Play");
                    parseMoves(g1.substring(i), flip == 0);
                    drawBoard();
                }
            }
        }
    }

    int golemHTML(String gs) {
        final String DIE = "/soccer/die";
        final String SRC = "/source/";
        final String COL = "font color=#";
        byte b[] = new byte[26];

        int i = gs.indexOf(DIE);
        if (i >= 0) {
            i += DIE.length();
            int d = gs.charAt(i) - '0';
            System.out.println("die " + d);
            colorThrow(d);
            gs = gs.substring(i);
        } else {
            colorThrow(0);
        }
        i = gs.lastIndexOf(COL);
        b[0] = (byte) (i >= 0 ? (gs.charAt(i + COL.length()) - '0') : 0);
        if (b[0] == 0) {
            System.out.println("B aan zet");
        } else {
            System.out.println("R aan zet");
        }
        i = 0;
        for (int f = 1; f <= 25; f++) {
            i = gs.indexOf(SRC, i) + SRC.length();
            if (gs.charAt(i) == '0') {
                System.out.print(" --");
            } else {
                System.out.print(" " + gs.charAt(i + 9) + gs.charAt(i + 11));
                if (gs.charAt(i + 9) == 'r') {
                    b[f] = (byte) (gs.charAt(i + 11) - '0');
                } else {
                    b[f] = (byte) ('0' - gs.charAt(i + 11));
                }
            }
            if (f % 5 == 0) {
                System.out.println();
            }
        }
        setup(b, "P0", "P1");
        return 1;
    }

    public static String parseHeader(String gameNot, String tag) {
        int q1, q2, i = gameNot.indexOf("[" + tag);
        if (i >= 0) {
            q1 = gameNot.indexOf('"', i) + 1;
            q2 = gameNot.indexOf('"', q1);
            if (q1 >= 0 && q2 >= 0) {
                if (logLevel >= 3) {
                    System.out.println(tag + " = " + gameNot.substring(q1, q2));
                }
                return gameNot.substring(q1, q2);
            }
        }
        return "";
    }

    public int parseMoves(String gameString) {
        return parseMoves(gameString, false);
    }

    public int parseMoves(String gameString, boolean flipped) {
        char[] ch = gameString.toCharArray();
        boolean comment = false;
        int plies = 0;
        Move m;
        for (int i = 9; i < ch.length; i++) {
            if (ch[i - 1] == ' ' && ch[i] == '[') {
                break;
            }
            if (ch[i] >= '1' && ch[i] <= '5' &&
                ch[i - 1] >= 'a' && ch[i - 1] <= 'e' &&
                (ch[i - 2] == '-' || ch[i - 2] == 'x') &&
                ch[i - 3] >= '1' && ch[i - 3] <= '5' &&
                ch[i - 4] >= 'a' && ch[i - 4] <= 'e' && !comment) {
                colorThrow((ch[i - 5] == '/' ? ch[i - 6] : ch[i - 5]) - '0');
                int fr = (ch[i - 4] - 'a') + 5 * ('5' - ch[i - 3]);
                int to = (ch[i - 1] - 'a') + 5 * ('5' - ch[i]);
                if (flipped) {
                    fr = 24 - fr;
                    to = 24 - to;
                }
                if (tryMove(fr, to) < 0) {
                    plies++;
                } else {
                    break;	// Invalid move
                }
            }
            if (ch[i] == '{') {
                comment = true;
            }
            if (ch[i] == '}') {
                comment = false;
            }
        }
        addMessage(pgnSaved = curGame.getPgn());
        while ((m = curGame.back()) != null) {
            cur.unMakeMove(m);
        }
        drawBoard();
        showGame(curGame.getPgn());
        if (plies > 0) {
            colorThrow(curGame.nextThrow());
        }
        return plies;
    }

    boolean loadNextPasteGame() {
        String fen, hEvent, gameString;
        String hWhite, hBlack, hRound;
        boolean pgn, loaded = false;
        int i1, i0;

        if (pasteString == null || pasteIndex >= pasteString.length()) {
            return false;
        }
        i0 = pasteString.indexOf("[Event", pasteIndex);
        if (i0 >= 0) {
            i1 = pasteString.indexOf("[Event", i0 + 1);
            if (i1 >= 0) {
                pasteIndex = i1;
            } else {
                pasteIndex = i1 = pasteString.length();
            }
            pgn = true;
        } else {
            pgn = false;
            i0 = pasteString.indexOf("[FEN", pasteIndex);
            if (i0 < 0) {
                return false;
            }
            i1 = pasteString.indexOf("[FEN", i0 + 4);
            if (i1 >= 0) {
                pasteIndex = i1;
            } else {
                pasteIndex = i1 = pasteString.length();
            }
        }
        gameString = pasteString.substring(i0, i1);
        fen = parseHeader(gameString, "FEN");
        if (pgn) {
            hEvent = parseHeader(gameString, "Event");
            if (hEvent.startsWith("Tournament ")) {
                hEvent = hEvent.substring(11);
            }
            hWhite = parseHeader(gameString, "White");
            hBlack = parseHeader(gameString, "Black");
            hRound = parseHeader(gameString, "Round");
        } else {
            int w, b, r, m;
            hEvent = hWhite = hBlack = hRound = "";
            if ((w = gameString.indexOf(',') + 1) > 0) {
                if ((b = gameString.indexOf(',', w) + 1) > 0) {
                    if ((r = gameString.indexOf(',', b) + 1) > 0) {
                        if ((m = gameString.indexOf(',', r)) > 0) {
                            hWhite = gameString.substring(w, b - 1);
                            hBlack = gameString.substring(b, r - 1);
                            hRound = gameString.substring(r, m);
                        }
                    }
                }
            }
        }
        if (cur.newStartingPos(fen)) {
            curGame = new Game(
                hEvent, parseHeader(gameString, "Site"),
                parseHeader(gameString, "Date"), hRound, hWhite, hBlack,
                parseHeader(gameString, "Result"), cur.toFEN(), cur.toString());
            loaded = anaMode = true;
            anaB.setText("Ana / Play");
            if (parseMoves(gameString) == 0) {
                colorThrow(thrown);		// throw from FEN
                curGame.setPly(cur.ply);	// Ply from FEN
            }
        }
        drawBoard();
        return loaded;
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (logLevel >= 5) {
            System.out.println(e.toString());
        }

        if (src == pasteB) {
            if (logLevel >= 4) {
                System.out.println("Paste game or position");
            }
            try {
                Clipboard sysCB = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable transGame = sysCB.getContents(this);
                pasteString = (String) transGame.getTransferData(DataFlavor.stringFlavor);
                pasteIndex = 0;
                if (pasteString.indexOf("Little Golem") >= 0) {
                    if (golemHTML(pasteString) > 0) {
                        anaMode = true;
                        anaB.setText("Ana / Play");
                        ana();
                    }
                }
                if (pasteString.indexOf("[Blue") >= 0) {
                    golemText(pasteString);
                } else {
                    System.out.println(pasteString);
                    loadNextPasteGame();
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        } else if (src == newB) {
            if (logLevel >= 4) {
                System.out.println("New game");
            }
            cur.newStartingPos();
            curGame = new Game("New", "Amsterdam NED", "", String.valueOf(++round),
                "Me", "MeinStein", "*", cur.toFEN(), cur.toString());
            colorThrow(rnd.nextInt(6) + 1);
            drawBoard();
        } else if (src == setupB) {
            if (logLevel >= 4) {
                System.out.println("Setup");
            }
            if (setupStone == 0) {		// Starting setup
                cur.clear();
                colorThrow(setupStone = 1);
                drawBoard();
                whiteSetupMode = false;
                currB.setBackground(Color.gray);
                setupB.setText("Setup / Norm");
            } else {				// Finishing setup
                cur.startStatVal();
                curGame = new Game("Setup", "Amsterdam NED", "", String.valueOf(++round),
                    "Opponent", "MeinStein", "*", cur.toFEN(), cur.toString());
                colorThrow(rnd.nextInt(6) + 1);
                setupStone = 0;
                drawBoard();
                setupB.setText("Norm / Setup");
            }
        } else if (src == flipB) {
            if (logLevel >= 4) {
                System.out.println("Flip board");
            }
            disp.flipBoard(flippedMode = !flippedMode);
            flipB.setText(flippedMode ? "Flip / Norm" : "Norm / Flip");
            drawBoard();
        } else if (src == grayB) {
            if (logLevel >= 4) {
                System.out.println("Gray/Colour");
            }
            disp.setColStone(coloMode = !coloMode);
            grayB.setText(coloMode ? "Colo / Gray" : "Gray / Colo");
            drawBoard();
        } else if (src == currB) {
            if (logLevel >= 4) {
                System.out.println("Current colour");
            }
            whiteSetupMode = !whiteSetupMode;
            currB.setBackground(whiteSetupMode ? Color.white : Color.gray);
            colorThrow(setupStone = 1);
        } else if (src == anaB) {
            if (logLevel >= 4) {
                System.out.println("Analyse/Play mode");
            }
            anaB.setText((anaMode = !anaMode) ? "Ana / Play" : "Play / Ana");
        } else if (src == annotB) {
            Move m;
            if (logLevel >= 4) {
                System.out.println("Annotate mode");
            }
            annotMode = true;
            htmlLog = new LogWriter("html");
            htmlLog.add("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
                "<html><head>\n" +
                "<meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\">\n" +
                "<title>EinStein Analysis</title>\n" +
                "<meta content=\"Author\" name=\"author\"><meta content=\"EinStein Wuerfelt nicht.\" name=\"description\">\n" +
                "<style type=\"text/css\"><!-- HR {height: 12px; background-color: rgb(102, 102, 102);}\n" +
                "TD {vertical-align: middle;} TD.empty {height: 24px; width: 24px;}\n" +
                "TD.black {height: 24px; width: 24px; background-color: rgb(102, 102, 102); color: rgb(255, 255, 255); text-align: center; font-family: arial black;}\n" +
                "TD.white {height: 24px; width: 24px; background-color: rgb(255, 255, 255); color: rgb(102, 102, 102); text-align: center; font-family: arial black;}\n" +
                "--></style></head>\n" +
                "<body style=\"background-color: white; color: black;\" alink=\"#000099\" link=\"#000099\" vlink=\"#990099\">\n");
            do {
                htmlLog.add(null);
                htmlLog.add("<pre>" + pgnSaved + "</pre><br>\n");
                htmlLog.add("<table style=\"width: 100%;\" border=\"1\" cellpadding=\"2\" cellspacing=\"2\"><tbody>\n");
                while ((m = curGame.played()) != null) {
                    colorThrow(curGame.nextThrow());
                    ana(m);
                    cur.makeMove(m);
                    disp.drawSquare(false, 0, m.fr % 5, m.fr / 5);
                    disp.drawSquare(cur.square[m.to].x, cur.square[m.to].num, m.to % 5, m.to / 5);
                    showGame(curGame.getPgn());
                    curGame.forward();
                }
                htmlLog.add("</tbody></table>\n");
            } while (loadNextPasteGame());
            htmlLog.add("</body></html>\n");
            htmlLog.add(null);
            annotMode = false;
        } else if (src == engB) {
            if (logLevel >= 4) {
                System.out.println("engineMatch");
            }
            engB.setText((matchMode = !matchMode) ? "EngMatch/Game" : "Game/EngMatch");
            if (matchMode) {
                matchRemain = matchLength;
                round = 0;
                engineMatch(moveTimer, "eval", "eval2");
            }
        } else if (src == backB) {
            if (logLevel >= 4) {
                System.out.println("Back");
            }
            Move m = curGame.back();
            if (m != null) {
                cur.unMakeMove(m);
                disp.drawSquare(cur.square[m.fr].x, cur.square[m.fr].num,
                    m.fr % 5, m.fr / 5);
                if (cur.square[m.to] == null) {
                    disp.drawSquare(false, 0, m.to % 5, m.to / 5);
                } else {
                    disp.drawSquare(cur.square[m.to].x, cur.square[m.to].num, m.to % 5, m.to / 5);
                }
                showGame(curGame.getPgn());
                colorThrow(curGame.nextThrow());
                if (logLevel >= 4) {
                    cur.debugOut();
                }
            }
        } else if (src == forwB) {
            if (logLevel >= 4) {
                System.out.println("Forward");
            }
            Move m = curGame.forward();
            if (m != null) {
                cur.makeMove(m);
                disp.drawSquare(false, 0, m.fr % 5, m.fr / 5);
                disp.drawSquare(cur.square[m.to].x, cur.square[m.to].num, m.to % 5, m.to / 5);
                showGame(curGame.getPgn());
                colorThrow(curGame.nextThrow());
                if (logLevel >= 4) {
                    cur.debugOut();
                }
            }
        } else if (src == opponent) {
            if (logLevel >= 1) {
                System.out.println(opponent.getText());
            }
        } else if (src == freePar) {
            System.out.println("freepar: " + freePar.getText());
        } else if (src == castB) {
            if (logLevel >= 4) {
                System.out.println("Cast");
            }
            colorThrow(rnd.nextInt(6) + 1);
        } else if (src == cutTime) {
            long f = (long) Integer.parseInt(cutTime.getText());
            cutOffTime = f > 0 ? f : 1000L;
            if (logLevel >= 1) {
                System.out.println("cutOffTime: " + cutOffTime);
            }
        } else if (src == matLen) {
            int f = Integer.parseInt(matLen.getText());
            matchLength = f > 0 ? f : 50;
            if (logLevel >= 1) {
                System.out.println("matchLength: " + matchLength);
            }
        } else if (src == inetB) {
            if (logLevel >= 1) {
                System.out.println("inetPlay");
            }
            if (appletPass == null) {
                InetHttpClient ihc = new InetHttpClient();
                String sid = ihc.logon(freePar.getText(), opponent.getText());
                System.out.println("inetID=" + sid);
                inetID = Integer.parseInt(sid);
                appletPass = ihc.getAppletPass();
            }
            if (appletPass == null) {
                System.out.println("inetPlay: no appletPass");
                return;
            }
            if (logLevel >= 4) {
                System.out.println("inetPlay: appletPass: " + appletPass);
            }
            if (client != null) {
                if (client.getGo()) {
                    client.quit();
                    try {
                        Thread.sleep(4000L);
                    } catch (InterruptedException ie) {
                    }
                }
                client = null;
                if (logLevel >= 1) {
                    System.out.println("InetPlay disconnected");
                }
                return;
            }
            System.out.println(freePar.getText() + " made connection!");
            client = new InetClient(this, inetID, appletPass);
        /* InetClient spawns a thread */
        } else if (src == inetIO) {
            String t = inetIO.getText();
            if (client == null && t.startsWith("!golem")) {
                GolemHttpClient ghc = new GolemHttpClient();
                String sid = ghc.logon("thstorm", "ein852");
            }
            // Send a message to the table or a private message
            if (client != null && client.getGo()) {
                switch (t.charAt(0)) {
                    case '!':
                        client.userCommand(t);
                        break;
                    case '#':	// #12#isuda?
                        int h2 = t.indexOf('#', 1);
                        if (h2 > 0) {
                            int p = Integer.parseInt(t.substring(1, h2));
                            client.sendMessage(PacketParent.MSG_PRIV, t.substring(h2 + 1), p);
                        }
                        break;
                    default:
                        client.sendMessage(PacketParent.MSG_TABLE, t, 0);
                }
            }
        } else if (src == calcB) {
            if (anaMode) {
                ana();
            } else {
                play(ana());
            }
        } else if (src == d1B) {
            colorThrow(1);
        } else if (src == d2B) {
            colorThrow(2);
        } else if (src == d3B) {
            colorThrow(3);
        } else if (src == d4B) {
            colorThrow(4);
        } else if (src == d5B) {
            colorThrow(5);
        } else if (src == d6B) {
            colorThrow(6);
        } else if (src == exitB) {
            System.exit(0);
        } else {
            System.out.println("unknown source of event");
        }
    }

    public static byte[] parseBytes(String s) {
        int n = (s.length() + 1) / 3;
        byte b[] = new byte[n];
        for (int i = 0; i < n; i++) {
            int v = Integer.parseInt(s.substring(3 * i, 3 * i + 2), 16);
            b[i] = (byte) (v < 0x80 ? v : v - 0x100);
        }
        return b;
    }

    public static String hexBytes(byte b[]) {
        final byte hex[] = {'0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'};
        byte b4[] = new byte[4 * b.length];
        for (int i = 0; i < b.length; i++) {
            b4[3 * i] = hex[(b[i] >> 4) & 0xf];
            b4[3 * i + 1] = hex[b[i] & 0xf];
            b4[3 * i + 2] = '.';
        }
        for (int i = 0; i < b.length; i++) {
            b4[3 * b.length + i] = b[i] < ' ' ? (byte) '.' : b[i];
        }
        return new String(b4);
    }

    public String timeString() {
        Calendar now = Calendar.getInstance();
        return nf.format(now.get(Calendar.DAY_OF_MONTH)) + "-" +
            nf.format(now.get(Calendar.HOUR_OF_DAY)) + ":" +
            nf.format(now.get(Calendar.MINUTE)) + " ";
    }

    public void write(String s) {
        if (logLevel >= 2) {
            System.out.println(timeString() + s);
        }
    }

    public void matchStatus(String s) {
        freePar.setText(s);
    }

    public void showMovTime(String s) {
        movTime.setText(s);
    }

    public void addMessage(String s) {
        messText.append(timeString() + s + "\r\n");
        messText.scrollRectToVisible(new Rectangle(0, messText.getHeight() - 10, 1, 12));
    }

    public void wakeUp(int from_who) {
        System.out.println("Wakeup from " + from_who);
    }

    public boolean setup(byte b[], String name0, String name1) {
        cur.clear();
        if (b[0] == 12) {
            inetFlip = b[13] != 0;
            cur.add(true, b[inetFlip ? 2 : 1], 24);
            cur.add(true, b[inetFlip ? 4 : 3], 19);
            cur.add(true, b[inetFlip ? 6 : 5], 14);
            cur.add(true, b[inetFlip ? 8 : 7], 23);
            cur.add(true, b[inetFlip ? 10 : 9], 18);
            cur.add(true, b[inetFlip ? 12 : 11], 22);
            cur.add(false, b[inetFlip ? 1 : 2], 0);
            cur.add(false, b[inetFlip ? 3 : 4], 5);
            cur.add(false, b[inetFlip ? 5 : 6], 10);
            cur.add(false, b[inetFlip ? 7 : 8], 1);
            cur.add(false, b[inetFlip ? 9 : 10], 6);
            cur.add(false, b[inetFlip ? 11 : 12], 2);
            colorThrow(0);
        } else if (b[0] == 11) {
            inetFlip = b[27] != 0;
            for (int x = 0, i = 0; x < 5; x++) {
                for (int y = 0; y < 25; y += 5) {
                    if (b[++i] != 0) {
                        if (inetFlip) {
                            cur.add(b[i] > 0, Math.abs((int) b[i]), x + y);
                        } else {
                            cur.add(b[i] < 0, Math.abs((int) b[i]), 24 - (x + y));
                        }
                    }
                }
            }
            System.out.println(cur.toFEN());
            System.out.println(cur.toString());
        } else {
            inetFlip = b[0] != 0;
            disp.flipBoard(flippedMode = !inetFlip);
            flipB.setText(flippedMode ? "Flip / Norm" : "Norm / Flip");
            for (int i = 0; i < 25;) {
                if (b[++i] != 0) {
                    if (inetFlip) {
                        cur.add(b[i] > 0, Math.abs((int) b[i]), i - 1);
                    } else {
                        cur.add(b[i] < 0, Math.abs((int) b[i]), 25 - i);
                    }
                }
            }
            System.out.println(cur.toFEN());
            System.out.println(cur.toString());
        }
        cur.startStatVal();
        if (inetFlip) {
            curGame = new Game("Inet", "InetPlay", "", String.valueOf(++round),
                name1, name0, "*", cur.toFEN(), cur.toString());
        } else {
            curGame = new Game("Inet", "InetPlay", "", String.valueOf(++round),
                name0, name1, "*", cur.toFEN(), cur.toString());
        }
        opponent.setText(name0.startsWith("MeinStein") ? name1 : name0);
        drawBoard();
        return inetFlip;
    }

    public void drawBoard() {
        for (int i = 0; i < 25; i++) {
            if (cur.square[i] == null) {
                disp.drawSquare(false, 0, i % 5, i / 5);
            } else {
                disp.drawSquare(cur.square[i].x, cur.square[i].num, i % 5, i / 5);
            }
        }
    }

    /**
     * The Game class holds the header information and moves of a game.
     * It implements methods to move back and forward in the game,
     * to add moves to it and to create PGN (portable game notation).
     *
     * Example PGN header:
     * [Event "Champion vs Computer"]
     * [Site "Philadelphia"]
     * [Date "1996.02.10"]
     * [Round "01"]
     * [White "MeinStein"]
     * [Black "Keinstein"]
     * [Result "1-0"]
     */
    class Game {

        String event, site, date, round;
        String white, black, result, fen, start;
        int ply;
        Move move[];

        public Game(String event, String site, String date, String round,
            String white, String black, String result, String fen, String start) {
            Calendar now = Calendar.getInstance();
            if (true == event.isEmpty()) {
                this.event = event + " @ " +
                    now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
            } else {
                this.event = event;
            }
            if (true == date.isEmpty()) {
                this.date = now.get(Calendar.YEAR) + "." + (1 + now.get(Calendar.MONTH)) + "." +
                    now.get(Calendar.DAY_OF_MONTH);
            } else {
                this.date = date;
            }
            this.site = site;
            this.round = round;
            this.white = white;
            this.black = black;
            this.result = result;
            this.fen = fen;
            this.start = start;
            ply = 0;
            move = new Move[32];
        }

        /**
         * setResult	At the end of the game this method should be called
         * to finalise this object for logging to a file.
         * @param	result	the result to set, i.e. "1-0" or "0-1"
         */
        public void setResult(String result) {
            this.result = result;
        }

        public int nextThrow() {
            if (move[ply] != null) {
                return move[ply].thrown;
            }
            return 0;
        }

        public void add(Move playedMove) {
            move[ply++] = playedMove;

            // After replacing a move, the replacement becomes
            // the final move of the game.
            move[ply] = null;
        }

        public Move back() {
            result = "*";
            return ply > 0 ? move[--ply] : null;
        }

        public Move forward() {
            if (move[ply] != null) {
                return move[ply++];
            }
            return null;
        }

        public Move played() {
            return move[ply];
        }

        public void setPly(int ply) {
            this.ply = ply;
        }

        String getPgn() {
            StringBuffer strBuf = new StringBuffer();
            String c;
            // Add header
            strBuf.append("[Event \"" + event + "\"]\n" +
                "[Site \"" + site + "\"]\n" +
                "[Date \"" + date + "\"]\n" +
                "[Round \"" + round + "\"]\n" +
                "[White \"" + white + "\"]\n" +
                "[Black \"" + black + "\"]\n" +
                "[Result \"" + result + "\"]\n" +
                "[FEN \"" + fen + "\"]\n");

            // Add starting position
            strBuf.append(start);

            // Add moves and comments
            for (int i = 0; i < ply; i++) {
                strBuf.append(move[i].toString());
                if ((c = move[i].comment) != null) {
                    strBuf.append(" {" + c + "}");
                }
                strBuf.append(i % 2 == 0 ? " " : "\n");
            }
            strBuf.append(result + "\n");
            return strBuf.toString();
        }

        /**
         * Save this game or a string to the log file.
         *
         * @param   s	the string to be logged. If null the whole game is logged.
         */
        public void save() {
            log(null);
        }

        public void log(String s) {
            LogWriter lw = new LogWriter("pgn");
            lw.add(s == null ? getPgn() + "\n" : "{ " + s + "}\n");
            lw.add(null);
        }
    }

    public class Stone {

        int num, pos, bit, prb, low, hig, val;
        boolean x;
        byte fen;
        String name;

        public Stone(boolean x, int num) {
            // Construct as a stone not placed on the board.
            this.x = x;
            this.num = num;
            fen = (byte) ((x ? 'A' : 'a') + num - 1);
            name = (x ? "W" : "B") + num;
            pos = -1;
            bit = 0;
            prb = 0; // 6*probability the stone can play
            low = 0; // index of next lower  stone on the board
            hig = 7; // index of next higher stone on the board
            val = 0;
        }
    }

    public class Move implements Comparable {

        int ply, thrown, fr, to;
        Stone capt;
        int statIndexX, statIndexY;
        float val;
        boolean exact;	// exact value or not due to unseen tactics
        String comment;

        public Move(int ply, int thrown, int fr, int to, Stone capt,
            int statIndexX, int statIndexY, float val, boolean exact) {
            this.ply = ply;
            this.thrown = thrown;
            this.fr = fr;
            this.to = to;
            this.capt = capt;
            this.statIndexX = statIndexX;
            this.statIndexY = statIndexY;
            this.val = val;
            this.exact = exact;
        }

        /**
         * Create algebraic notation for this move.
         *
         * @return	String with algebraic notation for this move.
         *		the white move includes the move number.
         */
        @Override
        public String toString() {
            byte[] alg = new byte[6];

            alg[0] = (byte) (thrown > 0 ? '0' + thrown : 'E');
            alg[1] = (byte) ('a' + fr % 5);
            alg[2] = (byte) ('5' - fr / 5);
            alg[3] = (byte) (capt == null ? '-' : 'x');
            alg[4] = (byte) ('a' + to % 5);
            alg[5] = (byte) ('5' - to / 5);
            if (ply % 2 == 0) {
                return String.valueOf(1 + ply / 2) + ". " + new String(alg);
            } else {
                return new String(alg);
            }
        }

        public void setComment(String c) {
            comment = c;
        }

        public int compareTo(Object m) {
            float f = ((Move) m).val - val;	// For descending order of move values
            if (f == 0f) {
                return 0;
            }
            return f > 0f ? 1 : -1;
        }

        public boolean equals(Move m) {
            if (m == null) {
                return false;
            }
            return this.fr == m.fr && this.to == m.to;
        }
    } // class Move

    class Position {

        int[] sqHit = new int[25];	// square hit by enemy
        int statIndexX = 0, statIndexY = 0, reachX, reachY;
        int ply = 0, freeVal = 0;
        long bitPos = 0;
        Stone mins;
        Stone[] xStone = new Stone[7];
        Stone[] yStone = new Stone[7];
        Stone[] square = new Stone[25];	// which stone on a square

        // Square:
        //  0  1  2  3  4
        //  5  6  7  8  9
        // 10 11 12 13 14
        // 15 16 17 18 19
        // 20 21 22 23 24
        //  a  b  c  d  e
        public void clear() {
            if (logLevel >= 5) {
                for (int i = 0; i < 25; i++) {
                    reachXS[i] = 0;
                    reachYS[i] = 0;
                    for (int c = i % 5; c < 5; c++) {
                        for (int r = i / 5; r < 5; r++) {
                            reachXS[i] |= 1 << (5 * r + c);
                        }
                    }
                    for (int c = i % 5; c >= 0; c--) {
                        for (int r = i / 5; r >= 0; r--) {
                            reachYS[i] |= 1 << (5 * r + c);
                        }
                    }
                    System.out.println("reach " + i + ": " + Integer.toBinaryString(reachXS[i]) +
                        " - " + Integer.toBinaryString(reachYS[i]));
                }
            }
            for (int i = 0; i < 25; i++) {
                square[i] = null;
            }
            for (int i = 0; i <= 6; i++) {
                xStone[i] = new Stone(true, i);
                yStone[i] = new Stone(false, i);
            }
            ply = 0;
            xStone[0].hig = 7;
            yStone[0].hig = 7;
            bitPos = 0;
        }

        public void add(boolean x, int num, int pos) {
            Stone[] st = x ? xStone : yStone;
            Stone s = st[num];

            if (square[pos] == null) { // Square was empty
                if (s.pos >= 0) // Already on the board
                {
                    return;
                }
                s.pos = pos;
                s.bit = 1 << pos;
                s.low = 0;
                square[pos] = s;
                if (st[0].hig > num) {
                    st[0].hig = num;
                }
                for (int i = num - 1; i > 0; i--) {
                    if (st[i].pos >= 0) {
                        st[i].prb -= st[i].hig - num;
                        st[i].hig = num;
                        s.low = i;
                        break;
                    }
                }
                s.hig = 7;
                for (int i = num + 1; i <= 6; i++) {
                    if (st[i].pos >= 0) {
                        st[i].prb -= num - st[i].low;
                        st[i].low = num;
                        s.hig = i;
                        break;
                    }
                }
                s.prb = s.hig - s.low - 1;
            } else {			// Square was occupied. Make it empty.
                s = square[pos];
                square[pos] = null;
                st[s.low].prb += s.hig - s.num;
                st[s.low].hig = s.hig;
                if (s.hig < 7) {
                    st[s.hig].prb += s.num - s.low;
                    st[s.hig].low = s.low;
                }
                s.pos = -1;
                s.bit = 0;
                s.prb = 0;
                s.low = 0;
                s.hig = 7;
            }
        }

        public void newStartingPos() {
            int pos[] = new int[6];
            clear();
            pos[0] = 0;
            pos[1] = 1;
            pos[2] = 2;
            pos[3] = 5;
            pos[4] = 6;
            pos[5] = 10;
            for (int h, j, i = 0; i < 6; i++) {
                j = rnd.nextInt(6);
                h = pos[i];
                pos[i] = pos[j];
                pos[j] = h;
            }
            for (int i = 0; i < 6; i++) {
                add(false, i + 1, pos[i]);
            }
            pos[5] = 14;
            pos[3] = 18;
            pos[4] = 19;
            pos[0] = 22;
            pos[1] = 23;
            pos[2] = 24;
            for (int h, j, i = 0; i < 6; i++) {
                j = rnd.nextInt(6);
                h = pos[i];
                pos[i] = pos[j];
                pos[j] = h;
            }
            for (int i = 0; i < 6; i++) {
                add(true, i + 1, pos[i]);
            }
            startStatVal();
            ply = 0;
        }

        public boolean newStartingPos(String fen) {	// [FEN "fdb2/1e3/a3F/3A1/2BD1 b 2 2"]

            boolean w = false, b = false;
            int pos = 0, i = 0;
            System.out.println(fen);
            clear();
            for (; i < fen.length() && pos < 25; i++) {
                switch (fen.charAt(i)) {
                    case 'a':
                        add(false, 1, pos++);
                        b = true;
                        break;
                    case 'b':
                        add(false, 2, pos++);
                        b = true;
                        break;
                    case 'c':
                        add(false, 3, pos++);
                        b = true;
                        break;
                    case 'd':
                        add(false, 4, pos++);
                        b = true;
                        break;
                    case 'e':
                        add(false, 5, pos++);
                        b = true;
                        break;
                    case 'f':
                        add(false, 6, pos++);
                        b = true;
                        break;
                    case '1':
                        pos += 1;
                        break;
                    case '2':
                        pos += 2;
                        break;
                    case '3':
                        pos += 3;
                        break;
                    case '4':
                        pos += 4;
                        break;
                    case '5':
                        pos += 5;
                        break;
                    case 'A':
                        add(true, 1, pos++);
                        w = true;
                        break;
                    case 'B':
                        add(true, 2, pos++);
                        w = true;
                        break;
                    case 'C':
                        add(true, 3, pos++);
                        w = true;
                        break;
                    case 'D':
                        add(true, 4, pos++);
                        w = true;
                        break;
                    case 'E':
                        add(true, 5, pos++);
                        w = true;
                        break;
                    case 'F':
                        add(true, 6, pos++);
                        w = true;
                        break;
                }
            }
            startStatVal();
            System.out.println("col,movenr: " + fen.charAt(i + 1) + fen.charAt(i + 3));
            if (fen.length() > i + 5) {
                thrown = fen.charAt(i + 5) - '0';
                System.out.println("thrown parsed: " + thrown);
            }
            ply = 2 * (fen.charAt(i + 3) - '1') + (fen.charAt(i + 1) == 'b' ? 1 : 0);
            return b && w;
        }

        @Override
        public String toString() {
            StringBuffer strBuf = new StringBuffer();
            strBuf.append("{");
            for (int i = 0; i < 25; i++) {
                strBuf.append((square[i] == null ? "--" : square[i].name));
                if (i % 5 == 4) {
                    strBuf.append(i == 24 ? "}\n" : "\n ");
                } else {
                    strBuf.append(" ");
                }
            }
            return strBuf.toString();
        }

        public String toHTML(String caption) {
            StringBuffer strBuf = new StringBuffer();
            strBuf.append("<table style=\"background-color: rgb(204, 204, 204);\" border=\"1\" cellpadding=\"0\" cellspacing=\"3\">");
            strBuf.append("<caption align=\"" + (caption.charAt(0) == 'W' ? "bottom" : "") + "\">" + caption + "</caption><tbody>\n");
            for (int i = 0; i < 25; i++) {
                if (i % 5 == 0) {
                    strBuf.append("<tr>");
                }
                strBuf.append("<td");
                if (square[i] == null) {
                    strBuf.append(" class=\"empty\"><br>");
                } else {
                    if (square[i].x) {
                        strBuf.append(" class=\"white\">" + square[i].num);
                    } else {
                        strBuf.append(" class=\"black\">" + square[i].num);
                    }
                }
                strBuf.append("</td>");
                if (i % 5 == 4) {
                    strBuf.append("</tr>\n");
                }
            }
            strBuf.append("</tbody></table><br>\n");
            return strBuf.toString();
        }

        /*
        [Event "1 @ 16:55"]
        [Site "Amsterdam NED"]
        [Date "2005.7.1"]
        [Round "1"]
        [White "Opponent"]
        [Black "MeinStein"]
        [Result "0-1"]
        [FEN "dec2/ba3/f3B/3EA/2DCF w 1"]
        {B4 B5 B3 -- --
        B2 B1 -- -- --
        B6 -- -- -- W2
        -- -- -- W5 W1
        -- -- W4 W3 W6}
        1. 2e3-d3 1b4-c3
        2. 3d1xc1 4a5xa4
        3. 5d2xc3 4a4-b4
        4. 6e1xe2 4b4xc3
        5. 2d3xc3 1c5-d4
        6. 6e2-d3 4d4-e3 {9}
        7. 2c3-b4 5b5xb4
        8. 2c1-b2 1e3-e2
        9. 1b2xa3 5b4-c3
        10. 1a3-a4 1e2-e1
        0-1
         */
        public String toFEN() {
            return toFEN(0);
        }

        public String toFEN(int thrown) {
            byte[] fen = new byte[40];
            int empty = 0, c = 0;
            for (int i = 0; i < 25; i++) {
                if (square[i] == null) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen[c++] = (byte) ('0' + empty);
                        empty = 0;
                    }
                    fen[c++] = square[i].fen;
                }
                if (i % 5 == 4) {
                    if (empty > 0) {
                        fen[c++] = (byte) ('0' + empty);
                        empty = 0;
                    }
                    if (i < 24) {
                        fen[c++] = '/';
                    }
                }
            }
            fen[c++] = ' ';
            fen[c++] = (byte) (ply % 2 == 0 ? 'w' : 'b');
            fen[c++] = ' ';
            return new String(fen, 0, c) + (1 + ply / 2) + (thrown > 0 ? " " + thrown : "");
        }

        Move newMove(Stone play, int to) {
            boolean exact;
            float v;
            int ix = statIndexX, iy = statIndexY;
            Stone capt = square[to];

            if (capt != null) {
                if (capt.x) {
                    ix -= sqDist[to] * pow5[capt.num];
                } else {
                    iy -= sqDist[24 - to] * pow5[capt.num];
                }
            }
            if (play.x) {
                if (sqDist[to] != sqDist[play.pos]) {
                    ix -= pow5[play.num];
                }
            } else {
                if (sqDist[24 - to] != sqDist[24 - play.pos]) {
                    iy -= pow5[play.num];
                }
            }

            if (to == 0 || to == 24) {
                exact = true;
                v = WON;
            } else {
                // Normal probability algorithm
                v = play.x ? StatV.winP(iy, ix, false)
                    : StatV.winP(ix, iy, false);
                exact = false;
            }
            return new Move(ply, play.num, play.pos, to, capt, ix, iy, v, exact);
        }

        public void setFreeVal() {
            freeVal = 0;
        }

        /**
         * Generates a list of legal moves in this position
         *
         * @param  moveList array of moves to be filled in
         * @param  thr	   the number thrown or 0 for generating all anticipated moves
         * @return the number of moves generated
         */
        public int generate(Move moveList[], int thr, boolean x) {
            int v, minv, n = 0;
            if (x) {
                int smin = xStone[0].hig, smax = 6;
                if (thr > 0) {
                    for (int i = smin; i <= smax; i = xStone[i].hig) {
                        if (i <= thr) {
                            smin = i;
                        }
                        if (i >= thr) {
                            smax = i;
                            break;
                        }
                    }
                }
                for (int i = smin; i <= smax; i = xStone[i].hig) {
                    int p = xStone[i].pos;
                    boolean hor = p % 5 > 0, ver = p / 5 > 0;
                    if (hor && ver) {
                        moveList[n++] = newMove(xStone[i], p - 6);
                    }
                    if (hor) {
                        moveList[n++] = newMove(xStone[i], p - 1);
                    }
                    if (ver) {
                        moveList[n++] = newMove(xStone[i], p - 5);
                    }
                }
            } else {
                int smin = yStone[0].hig, smax = 6;
                if (thr > 0) {
                    for (int i = smin; i <= smax; i = yStone[i].hig) {
                        if (i <= thr) {
                            smin = i;
                        }
                        if (i >= thr) {
                            smax = i;
                            break;
                        }
                    }
                }
                for (int i = smin; i <= smax; i = yStone[i].hig) {
                    int p = yStone[i].pos;
                    boolean hor = p % 5 < 4, ver = p / 5 < 4;
                    if (hor && ver) {
                        moveList[n++] = newMove(yStone[i], p + 6);
                    }
                    if (hor) {
                        moveList[n++] = newMove(yStone[i], p + 1);
                    }
                    if (ver) {
                        moveList[n++] = newMove(yStone[i], p + 5);
                    }
                }
            }
            return n;
        }

        public int makeMove(Move m) {
            Stone play = square[m.fr], capt = square[m.to];
            int i;
            i = statIndexX;
            statIndexX = m.statIndexX;
            m.statIndexX = i;
            i = statIndexY;
            statIndexY = m.statIndexY;
            m.statIndexY = i;
            if (m.capt != capt) {
                System.out.println("makeMove capt bug");
            }
            square[m.to] = play;
            square[m.fr] = null;
            play.pos = m.to;
            play.bit = 1 << m.to;
            if (capt != null) {
                int n;
                Stone[] st = capt.x ? xStone : yStone;
                if ((n = capt.low) > 0) {
                    st[n].prb += capt.hig - capt.num;
                }
                st[n].hig = capt.hig;
                if ((n = capt.hig) <= 6) {
                    st[n].prb += capt.num - capt.low;
                    st[n].low = capt.low;
                }
                capt.pos = -1;
            }
            ply++;
            return play.x ? play.num : -play.num;
        }

        public int unMakeMove(Move m) {
            Stone play = square[m.to], capt = m.capt;
            int i;
            i = statIndexX;
            statIndexX = m.statIndexX;
            m.statIndexX = i;
            i = statIndexY;
            statIndexY = m.statIndexY;
            m.statIndexY = i;
            square[m.fr] = play;
            square[m.to] = capt;
            play.pos = m.fr;
            play.bit = 1 << m.fr;
            if (capt != null) {
                int n;
                Stone[] st = capt.x ? xStone : yStone;
                if ((n = capt.low) > 0) {
                    st[n].prb -= capt.hig - capt.num;
                }
                st[n].hig = capt.num;
                if ((n = capt.hig) <= 6) {
                    st[n].prb -= capt.num - capt.low;
                    st[n].low = capt.num;
                }
                capt.pos = m.to;
            }
            ply--;
            return play.x ? play.num : -play.num;
        }

        void startStatVal() {
            int x, y;
            statIndexX = 0;
            for (int i = 6; i > 0; i--) {
                statIndexX = statIndexX * 5;
                statIndexX += sqDist[xStone[i].pos < 0 ? 0 : xStone[i].pos];
            }
            if (logLevel >= 1) {
                System.out.println("X " + Integer.toString(statIndexX, 5));
            }

            statIndexY = 0;
            for (int i = 6; i > 0; i--) {
                statIndexY = statIndexY * 5;
                statIndexY += sqDist[yStone[i].pos < 0 ? 0 : 24 - yStone[i].pos];
            }
            if (logLevel >= 1) {
                System.out.println("Y " + Integer.toString(statIndexY, 5));
            }
        }

        public int eval1(Move m) {
            Move moveL[] = new Move[18];
            int s = 0, n = generate(moveL, 0, ply % 2 == 0);
            for (int i = 0; i < n; i++) {
                s += m.val;
            }
            return n > 0 ? s / n : 1000000;
        }

        public int eval3() {
            int p, v, score = 0;
            int reachXSA = 0, bestX = 0, maxVX = 0;
            int reachYSA = 0, bestY = 0, maxVY = 0;

            for (int i = xStone[0].hig; i <= 6; i = xStone[i].hig) {
                if ((p = xStone[i].pos) >= 0) {
                    score += (v = sqValue[p] * xStone[i].prb);
                    if (maxVX < v) {
                        maxVX = v;
                        bestX = p;
                    }
                    if (xStone[i].prb > 1) {
                        reachXSA |= reachXS[p];
                    }
                }
            }
            for (int i = yStone[0].hig; i <= 6; i = yStone[i].hig) {
                if ((p = yStone[i].pos) >= 0) {
                    score -= (v = sqValue[24 - p] * yStone[i].prb);
                    if (maxVY < v) {
                        maxVY = v;
                        bestY = p;
                    }
                    if (yStone[i].prb > 1) {
                        reachYSA |= reachYS[p];
                    }
                }
            }
            if ((reachYSA & (1 << bestX)) == 0) // Best X stone not reachable by Y.
            {
                score += maxVX / 2;
            }
            if ((reachXSA & (1 << bestY)) == 0) // Best Y stone not reachable by X.
            {
                score -= maxVY / 2;
            }
            return ply % 2 == 1 ? score : -score;
        }

        public int eval4() {
            int p, v, score = 0, reachXSA = 0, reachYSA = 0, bestX = 0, bestY = 0;

            for (int maxv = 0, i = xStone[0].hig; i <= 6; i = xStone[i].hig) {
                if ((p = xStone[i].pos) >= 0) {
                    score += (v = sqValue[p] * xStone[i].prb);
                    if (maxv < v) {
                        maxv = v;
                        bestX = p;
                    }
                    reachXSA |= reachXS[p];
                }
            }
            for (int maxv = 0, i = yStone[0].hig; i <= 6; i = yStone[i].hig) {
                if ((p = yStone[i].pos) >= 0) {
                    score -= (v = sqValue[24 - p] * yStone[i].prb);
                    if (maxv < v) {
                        maxv = v;
                        bestY = p;
                    }
                    reachYSA |= reachYS[p];
                }
            }
            return ply % 2 == 1 ? score : -score;
        }

        int probCap(boolean xToPlay, int p) {	// Probability player can capture on square p.
            boolean hor, ver;
            Stone s;
            short[] r = new short[7];
            int prb = 0;

            for (int i = 0; i < r.length; i++) {
                r[i] = 0;
            }
            if (xToPlay) {		// Cannot use sum of stone prb due to overlap.
                hor = p % 5 < 4;
                ver = p / 5 < 4;
                if (hor && ver && (s = square[p + 6]) != null) {
                    if (s.x) {
                        for (int i = s.low + 1; i < s.hig; i++) {
                            if (++r[i] == 1) {
                                prb++;
                            }
                        }
                    }
                }
                if (hor && (s = square[p + 1]) != null) {
                    if (s.x) {
                        for (int i = s.low + 1; i < s.hig; i++) {
                            if (++r[i] == 1) {
                                prb++;
                            }
                        }
                    }
                }
                if (ver && (s = square[p + 5]) != null) {
                    if (s.x) {
                        for (int i = s.low + 1; i < s.hig; i++) {
                            if (++r[i] == 1) {
                                prb++;
                            }
                        }
                    }
                }
            } else {
                hor = p % 5 > 0;
                ver = p / 5 > 0;
                if (hor && ver && (s = square[p - 6]) != null) {
                    if (!s.x) {
                        for (int i = s.low + 1; i < s.hig; i++) {
                            if (++r[i] == 1) {
                                prb++;
                            }
                        }
                    }
                }
                if (hor && (s = square[p - 1]) != null) {
                    if (!s.x) {
                        for (int i = s.low + 1; i < s.hig; i++) {
                            if (++r[i] == 1) {
                                prb++;
                            }
                        }
                    }
                }
                if (ver && (s = square[p - 5]) != null) {
                    if (!s.x) {
                        for (int i = s.low + 1; i < s.hig; i++) {
                            if (++r[i] == 1) {
                                prb++;
                            }
                        }
                    }
                }
            }
            return prb;
        }

        void debugOut() {
            Move moveL[] = new Move[18];
            int ind, p, n, v, minv, pCap;
            Stone minsx = null;
            float winCap, winNoc;

            for (int i = 0; i < 25; i++) {
                System.out.print(square[i] == null ? " --" : " " + square[i].name);
                if (i % 5 == 4) {
                    System.out.println();
                }
            }

            for (int i = 1; i <= 6; i++) {
                System.out.print(padInt(xStone[i].pos, 4));
            }
            System.out.println();

            for (int i = 1; i <= 6; i++) {
                System.out.print(padInt(yStone[i].pos, 4));
            }
            System.out.println();

            minv = 900;
            for (int i = xStone[0].hig; i <= 6; i = xStone[i].hig) {
                System.out.print(" " + xStone[i].num + ":" + xStone[i].prb);
                v = stoneVal[xStone[i].pos][xStone[i].prb];
                if (minv > v) {
                    minv = v;
                    minsx = xStone[i];
                }
            }
            if (minsx == null) {
                return;
            }
            pCap = probCap(false, minsx.pos);
            System.out.println(" Best X stone: " + minsx.num + " p" + minsx.pos + " m" + minsx.prb + " v" + minv);
            System.out.println("pcap = " + pCap);

            ind = statIndexX - sqDist[minsx.pos] * pow5[minsx.num];
            winNoc = StatV.winP(statIndexY, statIndexX, false);
            System.out.println("Noc " + 100f * (WON + winNoc));
            if (pCap > 0) {
                winCap = StatV.winP(statIndexY, ind, false);
                System.out.println("Cap " + 100f * (WON + winCap));
                if (winCap < winNoc) {
                    winNoc = ((6 - pCap) * winNoc + pCap * winCap) / 6f;
                }
            }
            System.out.println("P " + 100f * (WON + winNoc));

            minv = 900;
            for (int i = yStone[0].hig; i <= 6; i = yStone[i].hig) {
                System.out.print(" " + yStone[i].num + ":" + yStone[i].prb);
                v = stoneVal[24 - yStone[i].pos][yStone[i].prb];
                if (minv > v) {
                    minv = v;
                    minsx = yStone[i];
                }
            }
            if (minsx == null) {
                return;
            }
            pCap = probCap(true, minsx.pos);
            System.out.println(" Best Y stone: " + minsx.num + " p" + minsx.pos + " m" + minsx.prb + " v" + minv);
            System.out.println("pcap = " + pCap);

            ind = statIndexY - sqDist[24 - minsx.pos] * pow5[minsx.num];
            winNoc = StatV.winP(statIndexX, statIndexY, false);
            System.out.println("Noc " + 100f * (WON + winNoc));
            if (pCap > 0) {
                winCap = StatV.winP(statIndexX, ind, false);
                System.out.println("Cap " + 100f * (WON + winCap));
                if (winCap < winNoc) {
                    winNoc = ((6 - pCap) * winNoc + pCap * winCap) / 6f;
                }
            }
            System.out.println("P " + 100f * (WON + winNoc));
        }
    } // Position

    class TranspositionTable {

        int getHit = 0, getNoHit = 0, hashSize;
        long[] hashTab;

        public TranspositionTable() {
            hashSize = primeLE((int) ((memReport() - 30000000) / 18));
            if (hashSize < 400000) {
                hashSize = 359987;
            }
            if (logLevel >= 2) {
                System.out.println("TranspositionTable size = " + hashSize);
            }
            hashTab = new long[2 * hashSize];
            for (int i = 0; i < 2 * hashSize; i++) {
                hashTab[i] = 0;
            }
            memReport();
        }

        public int primeLE(int upperBound) {
            // Return the biggest prime number <= upperBound.
            if (upperBound % 2 == 0) {
                upperBound--;
            }
            int f, r = upperBound, s = Math.round((float) Math.sqrt(r));
            for (; r > 0; r -= 2) {
                for (f = 3; f <= s; f += 2) {
                    if (r % f == 0) {
                        break;	// found divisor
                    }
                }
                if (r % f != 0) {
                    break;		// found prime
                }
            }
            return r;
        }

        public long memReport() {
            Runtime r = Runtime.getRuntime();
            long used = r.totalMemory() - r.freeMemory();
            System.out.println("Used/Free: " + used + "/" + r.freeMemory() +
                " of " + r.totalMemory() + ". Max: " + r.maxMemory());
            return r.maxMemory() - used;
        }

        public void report(boolean reset) {
            if (logLevel >= 3) {
                System.out.println("getHit " + getHit + " getNoHit " + getNoHit);
            }
            if (reset) {
                getHit = getNoHit = 0;
            }
        }

        public float get(long pos, int depth) {
            int i = 2 * (int) ((pos + depth % 2) % hashSize);
            if (hashTab[i] == pos) {
                int hashDepth = (int) hashTab[i + 1] & 0xff;
                if (hashDepth >= depth) {
                    getHit++;
                    return Float.intBitsToFloat((int) (hashTab[i + 1] >> 8));
                }
            }
            getNoHit++;
            return Float.MIN_VALUE;
        }

        public void add(long pos, int depth, float val) {
            int i = 2 * (int) ((pos + depth % 2) % hashSize);
            int hashDepth = (int) hashTab[i + 1] & 0xff;
            if (pos == hashTab[i] && depth <= hashDepth) {
                return;	// Do not replace deeper analysis
            }
            hashTab[i] = pos;
            hashTab[i + 1] = ((long) Float.floatToIntBits(val) << 8) | depth;
        }

        public long calcPos(Stone[] xStone, Stone[] yStone, boolean xMove) {
            long x, y, p = xMove ? 2 : 0;
            for (int i = 1; i <= 6; i++) {
                x = xStone[i].pos >= 0 ? xStone[i].pos : 25;
                y = yStone[i].pos >= 0 ? yStone[i].pos : 25;
                p |= (x << (5 * i + 27)) | (y << (5 * i - 3));
            }
            return p;
        }
    }

    class LogWriter {

        StringBuffer strBuf;
        String fileExtension;

        public LogWriter(String fileExtension) {
            this.fileExtension = "." + fileExtension;
            strBuf = new StringBuffer();
        }

        public void add(String s) {
            if (s == null) // Flush
            {
                try {
                    Calendar now = Calendar.getInstance();
                    FileWriter fw = new FileWriter("MeinStein_" +
                        now.get(Calendar.YEAR) + "_" +
                        (now.get(Calendar.MONTH) + 1) + "_" +
                        now.get(Calendar.DAY_OF_MONTH) + fileExtension, true);
                    fw.write(strBuf.toString(), 0, strBuf.length());
                    fw.close();
                    strBuf = new StringBuffer();
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            } else {
                strBuf.append(s);
            }
        }
    }
} // MeinCtrl
