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

import java.io.*;

public class Packet extends PacketParent {
    // Server message codes

    public static final byte SP_TABLE_CREATE = 7;
    public static final byte SP_MOVE = 9;
//    public static final byte SP_GAME_OVER = 10;
    public static final byte SP_GAME_PROGRESS = 11;
//    public static final byte SP_NEW_GAME = 12;
    public static final byte SP_FOCUS = 17;
    public static final byte SP_REQ_UNDO = 20;
    public static final byte SP_UNDO = 19;
    public static final byte SP_DICE = 21;
//    public static final byte LOGIN_OK = 0;
//    public static final byte LOGIN_TWICE = 1;
//    public static final String leave_reasons[] = {
//        "disconnected",
//        "connection timed out",
//        "connection severed on read",
//        "connection severed on write",
//        "garbage read in"
//    };

    // Client message codes
    public static final byte MY_TURN_TYPE_ME = 0;
    public static final byte MY_TURN_TYPE_OTHER = 1;
    public static final byte MY_TURN_ME_DICE = 2;
    public static final byte OPT_ME = 0;
    public static final byte OPT_YOU = 1;
    public static final byte OPT_ALT = 2;	// alternate start
    public static final byte CP_MOVE = 4;
    public static final byte CP_TABLE_CREATE = 6;
    public static final byte CP_FOCUS = 10;
    public static final byte CP_REQ_UNDO = 13;
    public static final byte CP_ALLOW_UNDO = 12;
    public static final byte CP_REQ_DICE = 14;

    private Packet(ByteArrayOutputStream buf) {
        super(buf);
    }

    public static PacketParent New(int size) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(size);
        return new Packet(buf);
    }

    public static byte[] SPDice(int value, int who) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_DICE);
            p.writeByte(value);
            p.writeByte(who);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPReqUndo() {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_REQ_UNDO);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPUndo(int xpos, int ypos, int who) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_UNDO);
            p.writeByte(xpos);
            p.writeByte(ypos);
            p.writeByte(who);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPFocus(boolean focus, int seat) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_FOCUS);
            p.writeBoolean(focus);
            p.writeByte(seat);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPTableCreate(int t_id, byte options, boolean wertung, boolean privat) {
        try {
            PacketParent p = New(3);
            p.writeByte(SP_TABLE_CREATE);
            p.writeInt(t_id);
            p.writeByte(options);
            p.writeBoolean(wertung);
            p.writeBoolean(privat);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPMove(int stone, int xpos, int ypos, int who) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_MOVE);
            p.writeByte(stone);
            p.writeByte(xpos);
            p.writeByte(ypos);
            p.writeByte(who);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPGameProgress(int stones[][], boolean playing, byte turnType, int diceVal) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_GAME_PROGRESS);
            for (int i = 0; i < stones.length; i++) {
                for (int j = 0; j < stones[0].length; j++) {
                    p.writeByte(stones[i][j]);
                }

            }

            p.writeBoolean(playing);
            p.writeByte(turnType);
            p.writeByte(diceVal);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPGameOver(int winner) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_GAME_OVER);
            p.writeByte(winner);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPNewGame(int stoneList0[], int stoneList1[], int beginner) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_NEW_GAME);
            for (int i = 0; i < stoneList0.length; i++) {
                p.writeByte(stoneList0[i]);
                p.writeByte(stoneList1[i]);
            }

            p.writeByte(beginner);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPDice() {
        try {
            PacketParent p = New(16);
            p.writeByte(CP_REQ_DICE);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPReqUndo() {
        try {
            PacketParent p = New(16);
            p.writeByte(CP_REQ_UNDO);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPAllowUndo(boolean allow) {
        try {
            PacketParent p = New(16);
            p.writeByte(CP_ALLOW_UNDO);
            p.writeBoolean(allow);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPFocus(boolean focus) {
        try {
            PacketParent p = New(16);
            p.writeByte(CP_FOCUS);
            p.writeBoolean(focus);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPTableCreate(byte options, boolean wertung,
        boolean privat, String password) {
        try {
            PacketParent p = New(3);
            p.writeByte(CP_TABLE_CREATE);
            p.writeByte(options);
            p.writeBoolean(wertung);
            p.writeBoolean(privat);
            p.writeUTF(password);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPMove(int stone, int xpos, int ypos) {
        try {
            PacketParent p = New(16);
            p.writeByte(CP_MOVE);
            p.writeByte(stone);
            p.writeByte(xpos);
            p.writeByte(ypos);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }
}
