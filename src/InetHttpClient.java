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

import java.net.*;
import java.io.*;

public class InetHttpClient {

    final String inetServer = "www.inetplay.de";
    final int portNum = 80;
    String cookie = "";
    String logUser, inetID = null, appletPass = null;

    public String logon(String user, String passwd) {
        //	normal HTTP flow, one POST, forget about images.
        logUser = user;
        try {
            hitSock(g, null);
            hitSock(log1, null);
            hitSock(log2, "user=" + user + "&passwd=" + passwd + "&submit=Absenden");
            hitSock(log3, null);
            hitSock(getGames, null);
            hitSock(getGameOnline7, null);
            if (inetID == null) {
                System.out.println("No inetID found");
            } else {
                hitSock(getSpiel0 + getSpiel, null);
                hitSock(getMapplet0 + getMapplet, null);
            }
        } catch (Exception e) {
            System.out.println("InetHttpClient logon exception");
            System.out.println(e.toString());
        }
        return inetID;
    }

    public String getAppletPass() {
        return appletPass;
    }

    void addCook(String part) {
        if (cookie.length() == 0) {
            cookie = "Cookie: " + part + "\r\n";
        } else {
            cookie = cookie.substring(1, cookie.length() - 2) + "; " + part + "\r\n";
        }
    }

    void hitSock(String reqHeader, String postData) throws Exception {
        Socket s = new Socket(inetServer, portNum);
        InputStream is = s.getInputStream();
        OutputStream os = s.getOutputStream();
        byte bRead[] = new byte[1024];
        String r;
        int n;
        boolean log1x = true;
        final String setCook = "Set-Cookie: ";
        final String passVal = "pass\" value =\"";
        final String inetIDM = "&inetID=";

        if (postData == null) {
            r = reqHeader + cookie + "\r\n";
        } else {
            r = reqHeader +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: " + postData.length() + "\r\n" +
                "Pragma: no-cache\r\n" +
                cookie +
                "\r\n" + postData;
        }
        System.out.println();
        System.out.println(r);
        os.write(r.getBytes());
        while ((n = is.read(bRead)) > 0) {
            if (log1x) {
                System.out.write(bRead, 0, n);
                log1x = false;
            }
            r = new String(bRead);
            int j, i = r.indexOf(setCook);
            if (i >= 0) {
                i += setCook.length();
                j = r.indexOf(';', i);
                addCook(r.substring(i, j));	// Session cookie
            }
            i = r.indexOf(passVal);
            if (i >= 0) {
                i += passVal.length();
                j = r.indexOf('"', i);
                appletPass = r.substring(i, j);	// applet password
            }
            i = r.indexOf(inetIDM);
            if (i >= 0 && inetID == null) {
                i += inetIDM.length();
                j = r.indexOf('"', i);
                inetID = r.substring(i, j);
                getSpiel0 = "GET /spielframe.php?game_id=-7&server=-1&user=" + logUser +
                    "&inetID=" + inetID + " HTTP/1.1\r\n";
                getMapplet0 = "GET /mapplet.php?user=" + logUser +
                    "&inetID=" + inetID + "&game_id=7&server=-1 HTTP/1.1\r\n";
            }
        }
        os.close();
        is.close();
        s.close();
    }
    static String g =
        "GET / HTTP/1.1\r\n" +
        "Host: www.inetplay.de\r\n" +
        "User-Agent: Mozilla/5.0\r\n" +
        "Accept: text/html;q=0.9,text/plain;q=0.8,image/png,*/" + "*;q=0.5\r\n" +
        "Accept-Language: de\r\n" +
        "Connection: Keep-Alive\r\n\r\n";
    static String log1 =
        "GET /login.php HTTP/1.1\r\n" +
        "Host: www.inetplay.de\r\n" +
        "Referer: http://www.inetplay.de/\r\n" +
        "User-Agent: Mozilla/5.0\r\n" +
        "Accept: text/html;q=0.9,text/plain;q=0.8,image/png,*/" + "*;q=0.5\r\n" +
        "Accept-Language: de\r\n" +
        "Connection: Keep-Alive\r\n";
    static String log2 =
        "POST /login.php HTTP/1.1\r\n" +
        "Host: www.inetplay.de\r\n" +
        "Referer: http://www.inetplay.de/login.php\r\n" +
        "User-Agent: Mozilla/5.0\r\n" +
        "Accept: text/html;q=0.9,text/plain;q=0.8,image/png,*/" + "*;q=0.5\r\n" +
        "Accept-Language: de\r\n" +
        "Connection: Keep-Alive\r\n";
    static String log3 =
        "GET /index.php HTTP/1.1\r\n" +
        "Host: www.inetplay.de\r\n" +
        "Referer: http://www.inetplay.de/login.php\r\n" +
        "User-Agent: Mozilla/5.0\r\n" +
        "Accept: text/html;q=0.9,text/plain;q=0.8,image/png,*/" + "*;q=0.5\r\n" +
        "Accept-Language: de\r\n" +
        "Connection: Keep-Alive\r\n";
    static String getGames =
        "GET /games.php HTTP/1.1\r\n" +
        "Host: www.inetplay.de\r\n" +
        "Referer: http://www.inetplay.de/index.php\r\n" +
        "User-Agent: Mozilla/5.0\r\n" +
        "Accept: text/html;q=0.9,text/plain;q=0.8,image/png,*/" + "*;q=0.5\r\n" +
        "Accept-Language: de\r\n" +
        "Connection: Keep-Alive\r\n";
    static String getGameOnline7 =
        "GET /gameonline.php?game=7 HTTP/1.1\r\n" +
        "Host: www.inetplay.de\r\n" +
        "Referer: http://www.inetplay.de/games.php\r\n" +
        "User-Agent: Mozilla/5.0\r\n" +
        "Accept: text/html;q=0.9,text/plain;q=0.8,image/png,*/" + "*;q=0.5\r\n" +
        "Accept-Language: de\r\n" +
        "Connection: Keep-Alive\r\n";
    static String getSpiel0 = null,  getSpiel =
        "Host: www.inetplay.de\r\n" +
        "Referer: http://www.inetplay.de/gameonline.php?game=7\r\n" +
        "User-Agent: Mozilla/5.0\r\n" +
        "Accept: text/html;q=0.9,text/plain;q=0.8,image/png,*/" + "*;q=0.5\r\n" +
        "Accept-Language: de\r\n" +
        "Connection: Keep-Alive\r\n";
    static String getMapplet0 = null,  getMapplet =
        "Host: www.inetplay.de\r\n" +
        "Referer: http://www.inetplay.de/spielframe.php\r\n" +
        "User-Agent: Mozilla/5.0\r\n" +
        "Accept: */*\r\n" +
        "Accept-Language: de\r\n" +
        "Connection: Keep-Alive\r\n";
}
