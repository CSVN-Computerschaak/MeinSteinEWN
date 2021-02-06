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

import java.awt.Color;
import java.io.*;
import java.net.Socket;
import java.text.NumberFormat;

public class InetClient implements Runnable {

    final String inetServer = "www.inetplay.de";
    final int portNum = 5663;
    final int TABLESIZE = 24;
    final int TABLES = 16;
    final int USERS = 100;
    final int STATE_INIT = 0;
    final int STATE_PLAYER = 1;
    final int STATE_RECORD = 2;
    MeinCtrl parent; // For calling back the parent (local EinStein WN playing entity).
    Socket sock;
    DataInputStream in;
    DataOutputStream out;
    int appletId;
    String appletPass;
    NumberFormat nf = NumberFormat.getInstance();
    boolean go = true, inetFlip = false, wertung = false;
    int myId = -2, oppId = 0;
    int evalReport = 0, minBalance = 2150, maxBalance = 2350;
    User player[] = new User[USERS];	// indexed by p_id
    Table table[] = new Table[TABLES];	// indexed by tableId
    byte progress[] = new byte[29];	// join table with game in progress
    long moveT = 0;
    int runState = STATE_INIT;
    boolean setupOK = false;

    class User {

        String name;
        int userId;
        int balance;
        boolean joi;
        int tableId, seat;

        User(String p_name, int p_userid, int p_balance, boolean joi) {
            name = p_name;
            userId = p_userid;
            balance = p_balance;
            joi = joi;
            tableId = seat = -2;
            parent.addMessage(name + "(" + balance + ") arrived.");
        }

        void sit(int tableId, int seat) {
            this.tableId = tableId;
            this.seat = seat;
        }

        void stand() {
            tableId = seat = -2;
        }
    }

    class Table {

        User present[] = new User[TABLESIZE];
        int tableId, redWon, yelWon, yelBal, yelBal0;
        boolean rated = true;

        Table(int tableId, boolean rated) {
            this.tableId = tableId;
            this.rated = rated;
            for (int p = 0; p < present.length; p++) {
                present[p] = null;
            }
        }

        void updateSeat(int seat, User u) {
            StringBuffer names = new StringBuffer();
            String s;
            if (seat < 2 && present[0] != null && present[1] != null && u == null && yelWon + redWon > 1) {
                log(true);		// Log and show the final result of the match
            }
            present[seat] = u;
            if (seat < 2 && present[0] != null && present[1] != null) {
                // Second player joins the table.
                redWon = yelWon = 0;
                yelBal = yelBal0 = present[1].balance;
            }
            names.append("Table " + (tableId + 1) + ": ");
            for (int p = 0; p < present.length; p++) {
                if (present[p] != null) {
                    if (p > 1) {
                        names.append(",");
                    }
                    names.append(present[p].name);
                }
                if (p == 0) {
                    names.append("-");
                }
            }
            parent.write(s = names.toString());
            parent.addMessage(s);
        }

        void update(int balance) {	// yellow player balance has been updated
            if (balance > yelBal) {
                yelWon++;
            } else if (balance < yelBal) {
                redWon++;
            }
            yelBal = balance;
            log(false);
            if (player[myId].tableId == tableId && runState == STATE_PLAYER) {
                if (player[myId].balance < minBalance || maxBalance < player[myId].balance) {
                    // Cut loss or fix win!
                    output(PacketParent.CPMessage(PacketParent.MSG_TABLE,
                        "Automatischer Abbruch: Vielen Dank.", 0));
                    output(PacketParent.CPTableJoin(-1, (byte) 2, ""));	// leave the table
                    runState = STATE_INIT;
                }
            }
        }

        void gameOver(int winner) {
            if (!rated) {
                if (player[winner] == present[1]) {
                    yelWon++;
                } else {
                    redWon++;
                }
                if (present[0] != null && present[1] != null) {
                    log(false);
                }
            }
            inetFlip = !inetFlip;
            if (runState == STATE_PLAYER && go) {
                output(PacketParent.CPNewGame());	// I want to play a new game
            }
        }

        void log(boolean send) {
            if (present[0] == null || present[1] == null) {
                return;
            }
            int d = yelBal0 - yelBal;
            String s = "T" + (tableId + 1) + " result: " +
                present[0].name + "(" + present[0].balance + ")-" +
                present[1].name + "(" + present[1].balance + ") " +
                redWon + "-" + yelWon + (d >= 0 ? " +" : " ") + d + " " +
                nf.format(100.0 * redWon / (redWon + yelWon)) + "%";
            parent.write(s);
            if (player[myId].tableId == tableId) {
                parent.matchStatus(s);
            }
            if ((yelWon + redWon) % 5 == 0) {
                parent.addMessage(s);
            }
            if (send || player[myId].tableId == tableId && (yelWon + redWon) % 5 == 0) {
                output(PacketParent.CPMessage(PacketParent.MSG_TABLE, s, 0));
            }
        }
    }

    public InetClient(MeinCtrl g, int id, String pass) {
        parent = g;
        appletId = id;
        appletPass = pass;
        try {
            sock = new Socket(inetServer, portNum);
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
        } catch (IOException ie) {
            parent.write("IOException during connect");
            parent.write(ie.toString());
            ie.printStackTrace();
        }
        // Initialisation is complete. Start a thread.
        (new Thread(this)).start();
    }

    String playerName(int p_id) {
        if (p_id < 0 || p_id >= player.length) {
            return "nobody";
        }
        return player[p_id] == null ? "Player" + p_id : player[p_id].name;
    }

    @SuppressWarnings("static-access")
    public synchronized void output(byte p[]) {
        if (p != null && p.length > 0) {
            try {
                out.write(p, 0, p.length);
                if (p[0] != 8) {
                    parent.write("->" + parent.hexBytes(p));
                }
            } catch (IOException e) {
                parent.write("out.write error!");
                e.printStackTrace();
                go = false;
            }
        }
    }

    public void quit() {
        go = false;
        output(PacketParent.CPQuit());
    }

    protected void disconnect() {
        try {
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
        }
    }

    public void userCommand(String cmd) {
        parent.write("userCommand: " + cmd);
        if (cmd.startsWith("!leave")) {		// Leave table
            if (runState != STATE_INIT) {
                output(PacketParent.CPTableJoin(-1, (byte) 2, ""));
                runState = STATE_INIT;
            }
        } else if (cmd.startsWith("!table")) {	// Join table for recording
            if (runState == STATE_INIT) {
                output(PacketParent.CPTableJoin(Integer.parseInt(cmd.substring(6).trim()) - 1, (byte) 1, ""));
                runState = STATE_RECORD;
            }
        } else if (cmd.startsWith("!play")) {	// Create table for playing
            if (runState == STATE_INIT) {
                output(Packet.CPTableCreate(Packet.OPT_ALT, wertung = true, false, ""));
                runState = STATE_PLAYER;
            }
        } else if (cmd.startsWith("!prac")) {	// Create table for practise
            if (runState == STATE_INIT) {
                output(Packet.CPTableCreate(Packet.OPT_ALT, wertung = false, false, ""));
                runState = STATE_PLAYER;
            }
        } else if (cmd.startsWith("!range")) {	// range within to stay playing
            int h2 = cmd.indexOf('-');
            if (h2 > 0) {
                minBalance = Integer.parseInt(cmd.substring(6, h2).trim());
                maxBalance = Integer.parseInt(cmd.substring(h2 + 1).trim());
                parent.addMessage("range: " + minBalance + "-" + maxBalance);
            } else {
                parent.addMessage("range: min-max");
            }
        } else if (cmd.startsWith("!eval hint")) {	// hints to the table
            evalReport = 2;
        } else if (cmd.startsWith("!eval show")) {	// evaluations shown
            evalReport = 1;
        } else if (cmd.startsWith("!eval")) {	// no evaluations shown
            evalReport = 0;
        }
    }

    public int getPlayerID() {
        return myId;
    }

    public boolean getGo() {
        return go;
    }

    public void run() {
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(1);
        for (int p = 0; p < player.length; p++) {
            player[p] = null;
        }
        for (int t = 0; t < table.length; t++) {
            table[t] = null;
        }

        //	Login and play
        output(PacketParent.CPLogin(appletId, appletPass));
        while (go) {
            try {
                byte tag = in.readByte();
                switch (tag) {
                    case 0:
                        hdYouAre();
                        break;
                    case 1:
                        hdLogin(in.readByte());
                        break;
                    case 2:
                        hdJoin();
                        break;
                    case 3:
                        hdLeave();
                        break;

                    case 5:
                        hdTablePlayer();
                        break;
                    case 6:
                        hdMessage();
                        break;
                    case 7:
                        hdTableCreate();
                        break;
                    case 8:
                        hdTableClose();
                        break;
                    case 9:
                        hdMove();
                        break;
                    case 10:
                        hdGameOver();
                        break;
                    case 11:
                        hdGameProgress();
                        break;
                    case 12:
                        hdNewGame();
                        break;
                    case 13:
                        hdPing();
                        break;
                    case 14:
                        hdUpdate();
                        break;

                    case 16:
                        hdExit();
                        break;
                    case 17:
                        hdFocus();
                        break;
                    case 18:
                        hdWakeUp();
                        break;
                    case 19:
                        hdUndo();
                        break;
                    case 20:
                        hdReqUndo();
                        break;
                    case 21:
                        hdDice();
                        break;

                    case 24:
                        hdPlayerBrokenConnection();
                        break;

                    case 4: // '\004'
                    case 15: // '\017'
                    case 22: // '\026'
                    case 23: // '\027'
                    default:
                        parent.write("Received, not implemented: " + tag);
                        break;
                }
            } catch (IOException e) {
                parent.write("No connection to Server!");
                go = false;
            }
        }
        disconnect();
        parent.write("Verbindung zum Spiel beendet!");
    }

    protected void hdYouAre()
        throws IOException {
        myId = in.readInt();
        parent.write("You are: " + myId);
    }

    void afterLogin() {
        parent.write("Login OK!");
    }

    protected void hdLogin(byte result) throws IOException {
        if (result == 0) {
            afterLogin();
        } else {
            parent.write("Login Error!");
            disconnect();
        }
    }

    protected void hdJoin() throws IOException {
        int p_id = in.readInt();
        String p_name = in.readUTF();
        int p_userid = in.readInt();
        int p_balance = in.readInt();
        boolean joi = in.readBoolean();

        parent.write((p_id == myId ? "Me     " : "Player ") + p_id +
            " " + p_name + " " + p_userid + " " + p_balance + " " + joi);
        if (p_id >= 0 && p_id < player.length) {
            player[p_id] = new User(p_name, p_userid, p_balance, joi);
        }
    }

    protected void hdLeave() throws IOException {
        int p_id = in.readInt();
        String s = "Player left: " + playerName(p_id);
        parent.write(s);
        parent.addMessage(s);
        player[p_id] = null;
    }

    void hdTablePlayer() throws IOException {
        int t_id = in.readInt();
        int seat = in.readInt();
        int p_id = in.readInt();
        String playername = "";

        if (t_id >= 0) {
            table[t_id].updateSeat(seat, p_id >= 0 ? player[p_id] : null);
        }
        if (p_id >= 0) {
            player[p_id].sit(t_id, seat);
        }

        if (p_id != myId && t_id == player[myId].tableId) {
            parent.addMessage("Seat " + seat + ": " + playerName(p_id));
            if (seat < 2 && runState == STATE_PLAYER) {
                parent.write("Opponent Seat " + seat + " Player " + p_id);
                if (p_id >= 0) {
                    oppId = p_id;
                    // Send a message to the table.
                    output(PacketParent.CPMessage(PacketParent.MSG_TABLE,
                        "MeinStein_c ist ein Java Programm von Theo van der Storm. \"_c\" heisst das der Computer Spielt!", 0));
                    output(PacketParent.CPMessage(PacketParent.MSG_TABLE,
                        "Es spielt voellig automatisch. Theo ist meistens NICHT da um Nachrichten zu beantworten.", 0));
                    output(PacketParent.CPMessage(PacketParent.MSG_TABLE,
                        "Wuensche ein schoenes Spiel, " + player[oppId].name + "!", 0));
                    output(PacketParent.CPNewGame());	// I want to play a new game
                } else {
                    // Send a message to all.
                    output(PacketParent.CPMessage(
                        PacketParent.MSG_ALL, "Es gibt einen freien Platz an meinem Tisch.", 0));
                /*		    PacketParent.MSG_ALL, "There is a seat free at my table.", 0)); */
                }
            }
        }
    }

    public void sendMessage(byte type, String s, int p_id) {
        if (type == PacketParent.MSG_TABLE && runState == STATE_INIT) {
            type = PacketParent.MSG_ALL;
        }
        output(PacketParent.CPMessage(type, s, p_id));
    }

    protected void hdMessage() throws IOException {
        byte type = in.readByte();
        int p_id = in.readInt();
        String author, msg, text = in.readUTF();
        Color col = Color.black;

        author = type % 4 == 0 ? "" : ":" + p_id + "=" + playerName(p_id);
        msg = type + author + ": " + text;
        parent.write("Msg" + msg);
        if (type != 0 || text.indexOf("chte spielen.") < 0) {
            parent.addMessage(msg);
        }
        if (text.indexOf("Du hast bereits gew") >= 0) {
            output(PacketParent.CPMessage(PacketParent.MSG_TABLE,
                "Error detected. Please join me at my new table.", 0));
            output(PacketParent.CPTableJoin(-1, (byte) 2, ""));
            output(Packet.CPTableCreate(Packet.OPT_ALT, wertung, false, ""));
        }
    }

    protected void hdTableCreate() throws IOException {
        int table_id = in.readInt();
        byte options = in.readByte();
        boolean wert = in.readBoolean();
        boolean priv = in.readBoolean();
        parent.write("Table created " + table_id + " options " + options + " wert " + wert + " priv " + priv);
        table[table_id] = new Table(table_id, wert);
    }

    protected void hdTableClose()
        throws IOException {
        int tableId = in.readInt();
        parent.write("Table closed " + tableId);
        table[tableId] = null;
        if (player[myId].tableId == tableId) {
            runState = STATE_INIT;
        }
    }

    void hdMove() //  9
        throws IOException {
        int stone = in.readByte();
        int xpos = in.readByte();
        int ypos = in.readByte();
        int who = in.readByte();
        int stat, p2;
        long mT = System.currentTimeMillis();

        if (progress[ 0] == 11) {
            int t = player[myId].tableId;
            progress[27] = (byte) who;
            inetFlip = parent.setup(progress,
                table[t].present[0].name, table[t].present[1].name);
            progress[ 0] = 0;
            setupOK = true;
        }
        parent.write("stone " + stone + " X " + xpos + " Y " + ypos + " who " + who);
        parent.showMovTime(nf.format((mT - moveT) / 1000f) + "s");
        moveT = mT;
        if (runState == STATE_PLAYER && who == player[myId].seat) {
            return;				// Receiving my own move back. Ignore.
        }
        p2 = inetFlip ? 5 * ypos + xpos : 5 * (4 - ypos) + 4 - xpos;
        stat = parent.tryMoveN(Math.abs(stone), p2);
        if (stat == p2) {
            parent.write("ERROR move rejected " + stone + " to " + p2);
        }
        if (runState == STATE_PLAYER) {		// Receiving opponent move
            if (stat == -1) // Roll my die
            {
                output(Packet.CPDice());
            } else if (stat == -2) // Play my EinStein
            {
                analyse(true);
            }
        // else game over. Wait for result.
        } else if (stat == -2 && setupOK) {
            analyse(false);
        }
    }

    void hdGameOver() // 10
        throws IOException {
        int winner = in.readByte();
        table[player[myId].tableId].gameOver(winner);
        setupOK = false;
    }

    @SuppressWarnings("static-access")
    void hdGameProgress() // 11
        throws IOException {
        progress[0] = 11;
        for (int i = 1; i < progress.length; i++) {
            progress[i] = in.readByte();
        }
        parent.write("game progress");
        parent.write(parent.hexBytes(progress));
        parent.colorThrow(progress[28]);		// Current die value.
        moveT = System.currentTimeMillis();
    }

    @SuppressWarnings("static-access")
    void hdNewGame() throws IOException // 12
    {
        int t = player[myId].tableId;
        byte b[] = new byte[14];
        b[0] = 12;
        for (int i = 1; i < b.length; i++) {
            b[i] = in.readByte();
        }
        parent.write(parent.hexBytes(b));
        inetFlip = parent.setup(b, table[t].present[0].name, table[t].present[1].name);
        setupOK = true;
        output(Packet.CPFocus(true));
        if (runState == STATE_PLAYER &&
            (!inetFlip && player[myId].seat == 0 || inetFlip && player[myId].seat == 1)) {
            output(Packet.CPDice());	// I begin. Roll die.
        }
        moveT = System.currentTimeMillis();
    }

    protected void hdPing() throws IOException {
        output(PacketParent.CPPing());	// Actually PONG
    }

    protected void hdUpdate() throws IOException {
        int p_id = in.readInt();
        int newBal = in.readInt();
        if (p_id >= 0) {
            int t = player[p_id].tableId;
            player[p_id].balance = newBal;
            if (t >= 0 && player[p_id].seat == 1) {
                table[t].update(newBal);
            }
        }
    }

    void hdExit() throws IOException {
        quit();
    }

    protected void hdWakeUp() throws IOException {
        int from_who = in.readByte();
        parent.wakeUp(from_who);
    }

    void hdUndo() throws IOException {
        int xpos = in.readByte();
        int ypos = in.readByte();
        int who = in.readByte();
        parent.write("undoing: " + xpos + " " + ypos + " " + who);
    }

    void hdReqUndo() {
    }

    void hdFocus() throws IOException {
        boolean focus = in.readBoolean();
        int seat = in.readByte();
    }

    void analyse(boolean makeMove) {
        long t = System.currentTimeMillis();
        int m = parent.anaPlay(makeMove);
        if (evalReport == 1) {
            parent.addMessage(parent.myEval());
        } else if (evalReport == 2) {
            output(PacketParent.CPMessage(PacketParent.MSG_TABLE,
                "Eval: " + parent.myEval(), 0));
        }
        if (makeMove) {
            if (m == 0) {
                output(PacketParent.CPMessage(PacketParent.MSG_TABLE,
                    "Error: Cannot produce move.", 0));
                return;
            }
            int n = m / 25;
            m %= 25;
            t += 750 - System.currentTimeMillis();
            if (t > 0) // Sleep to show the die to the opponent
            {
                try {
                    Thread.sleep(t);
                } catch (InterruptedException ie) {
                }
            }
            output(Packet.CPMove(player[myId].seat == 0 ? -n : n,
                inetFlip ? m % 5 : 4 - m % 5, inetFlip ? m / 5 : 4 - m / 5));
        }
    }

    void hdDice() throws IOException {
        parent.colorThrow(in.readByte());		// Received die value.
        int who = in.readByte();
        if (runState == STATE_PLAYER && who == player[myId].seat) {	// my throw
            analyse(true);
        } else if (setupOK) {
            if (runState == STATE_PLAYER) {
                long c = parent.setTime(1000L);	// be ready before opponent moves.
                analyse(false);
                parent.setTime(c);
            } else {
                analyse(false);
            }
        }
    }

    void hdPlayerBrokenConnection() throws IOException {
        int table_id = in.readByte();
        int seat = in.readByte();
        int type = in.readByte();
        parent.write("BrokenConnection table " + table_id + " seat " + seat);
    }
}

