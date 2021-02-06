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

public class PacketParent extends DataOutputStream {
    // Server message codes

    public static final byte SP_YOU_ARE = 0;
    public static final byte SP_LOGIN = 1;
    public static final byte SP_JOIN = 2;
    public static final byte SP_LEAVE = 3;
    public static final byte SP_TABLE_PLAYER = 5;
    public static final byte SP_MESSAGE = 6;
    public static final byte SP_TABLE_CLOSE = 8;
    public static final byte SP_GAME_OVER = 10;
    public static final byte SP_NEW_GAME = 12;
    public static final byte SP_PING = 13;
    public static final byte SP_UPDATE = 14;
    public static final byte SP_EXIT = 16;
    public static final byte SP_WAKEUP = 18;
    public static final byte SP_PLAYER_BROKEN_CONNECTION = 24;
    public static final byte BROKEN_CONN_BROKEN = 1;
    public static final byte BROKEN_CONN_KICK = 2;
    public static final byte LOGIN_OK = 0;
    public static final byte LOGIN_TWICE = 1;
    public static final String leave_reasons[] = {
        "disconnected",
        "connection timed out",
        "connection severed on read",
        "connection severed on write",
        "garbage read in"
    };

    // Client message codes
    public static final byte CP_LOGIN = 0;
    public static final byte CP_MESSAGE = 1;
    public static final byte CP_TABLE_JOIN = 2;
    /*
    recording
    02 00 00 00 02  01  00 00 I want to join table 2 mode JOIN_SPEC no passwd
    05 00 00 00 02 00 00 00 02 00 00 00 09 table 2 seat 2 id 9
    0B    FD 00 00 00 00  00 FB 00 FF 00  00 FA 05 04 00  00 00 00 00 00  00 00 01 00 02
    00 02  01   SP_GAME_PROGRESS = 11;  25 squares  29 bytes
    15 05 01 00 00 00 SP_DICE
    09 05 01 01 01 00 SP_MOVE
    09 FD 01 01 01 00 SP_MOVE
     */
    public static final byte CP_NEW_GAME = 3;
    public static final byte CP_QUIT = 5;
    public static final byte CP_TABLE_KICK = 7;
    public static final byte CP_PING = 8;
    public static final byte CP_WAKEUP = 11;
    public static final byte MSG_SERVER = 0;
    public static final byte MSG_TABLE = 1;
    public static final byte MSG_ALL = 2;
    public static final byte MSG_PRIV = 3;
    public static final byte MSG_BROADCAST = 4;
    public static final byte TABLE_LEAVE = -1;
    public static final byte JOIN_PLAY = 0;
    public static final byte JOIN_SPEC = 1;
    public ByteArrayOutputStream b_out;

    protected PacketParent(ByteArrayOutputStream buf) {
        super(buf);
        b_out = buf;
    }

    public byte[] buf() {
        return b_out.toByteArray();
    }

    public static PacketParent New(int size) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(size);
        return new PacketParent(buf);
    }

    public static byte[] SPPlayerBrokenConnection(int table_id, int seat, int type) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_PLAYER_BROKEN_CONNECTION);
            p.writeByte(table_id);
            p.writeByte(seat);
            p.writeByte(type);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPMessage(byte type, int p_id, String text) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_MESSAGE);
            p.writeByte(type);
            p.writeInt(p_id);
            p.writeUTF(text);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPWakeUp(int from_who) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_WAKEUP);
            p.writeByte(from_who);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPExit() {
        try {
            PacketParent p = New(16);
            p.writeByte(16);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPUpdate(int p_id, int newBal) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_UPDATE);
            p.writeInt(p_id);
            p.writeInt(newBal);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPNewGame(int beginner) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_NEW_GAME);
            p.writeByte(beginner);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPPing() {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_PING);
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

    public static byte[] SPTableClose(int t_id) {
        try {
            PacketParent p = New(3);
            p.writeByte(SP_TABLE_CLOSE);
            p.writeInt(t_id);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPTablePlayer(int t_id, int seat, int p_id) {
        try {
            PacketParent p = New(4);
            p.writeByte(SP_TABLE_PLAYER);
            p.writeInt(t_id);
            p.writeInt(seat);
            p.writeInt(p_id);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPYouAre(int id) {
        try {
            PacketParent p = New(2);
            p.writeByte(SP_YOU_ARE);
            p.writeInt(id);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPLogin(byte result) {
        try {
            PacketParent p = New(2);
            p.writeByte(SP_LOGIN);
            p.writeByte(result);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPJoin(int p_id, String name, int user_id, int bal, boolean join) {
        try {
            PacketParent p = New(16);
            p.writeByte(SP_JOIN);
            p.writeInt(p_id);
            p.writeUTF(name);
            p.writeInt(user_id);
            p.writeInt(bal);
            p.writeBoolean(join);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] SPLeave(int p_id) {
        try {
            PacketParent p = New(3);
            p.writeByte(SP_LEAVE);
            p.writeInt(p_id);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPWakeUp(int p_id) {
        try {
            PacketParent p = New(16);
            p.writeByte(CP_WAKEUP);
            p.writeByte(p_id);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPPing() {
        try {
            PacketParent p = New(16);
            p.writeByte(CP_PING);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPQuit() {
        byte buf[] = {
            CP_QUIT
        };
        return buf;
    }

    public static byte[] CPLogin(int id, String pass) {
        try {
            PacketParent p = New(40);
            p.writeByte(CP_LOGIN);
            p.writeInt(id);
            p.writeUTF(pass);	// Added
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPMessage(byte type, String text, int to_player) {
        try {
            PacketParent p = New(16);
            p.writeByte(CP_MESSAGE);
            p.writeByte(type);
            p.writeByte(to_player);
            p.writeUTF(text);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPTableJoin(int t_id, byte mode, String pass) {
        try {
            PacketParent p = New(3);
            p.writeByte(CP_TABLE_JOIN);
            p.writeInt(t_id);
            p.writeByte(mode);
            p.writeUTF(pass);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPNewGame() {
        try {
            PacketParent p = New(16);
            p.writeByte(CP_NEW_GAME);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }

    public static byte[] CPTableKick(int t_id, int p_id, int master_id, boolean permanent) {
        try {
            PacketParent p = New(3);
            p.writeByte(CP_TABLE_KICK);
            p.writeInt(t_id);
            p.writeInt(p_id);
            p.writeInt(master_id);
            p.writeBoolean(permanent);
            return p.buf();
        } catch (IOException ie) {
            System.out.println("PacketParent " + ie.toString());
        }
        return null;
    }
}
