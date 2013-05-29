/**
 * Copyright (c) 2007-2009 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and
 * the authors indicated in the
 *
 * @author tags
 *
 * This file is part of SMaRt.
 *
 * SMaRt is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SMaRt is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SMaRt. If not, see <http://www.gnu.org/licenses/>.
 */
package bftsmart.demo.counter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.io.InputStreamReader;

import bftsmart.tom.ServiceProxy;
import bftsmart.tom.server.BatchExecutable;
import bftsmart.tom.server.Recoverable;
import bftsmart.tom.util.Logger;

import hermes.HermesConfig;
import hermes.orchestration.notification.StatNotification;
import hermes.runtime.HermesRuntime;
import hermes.runtime.bft.BFTNode;
import hermes.stats.StatToken;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


import hermes.stats.Stats;

/**
 * Example client that updates a BFT replicated service (a counter).
 *
 */
public final class CounterClient extends Object {

    static protected void hermesCodeInjection(String[] args) {
        String id = args[0];

        //HermesRuntime m_runtime = new HermesRuntime();
        HermesRuntime.getInstance().setID(id);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date();
        dateFormat.format(date);
        String prefix = "replica";
        if (id.compareTo("1001") == 0) {
            System.out.println("\nCLIENT\n");
            prefix = "client";
        } else {
            System.out.println("\nREPLICA\n");
        }
        String logName = HermesConfig.getWorkingDir() + "/node_" + prefix + "_"
                + HermesRuntime.getInstance().getRuntimeID() + "_" + dateFormat.format(date) + ".log";
        HermesConfig.addLoggerFile(logName);
        try {
            HermesRuntime.getInstance().open();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public CounterClient() {
    }

    //@SuppressWarnings("static-access")
    public static void main(String[] args) {// throws IOException {


        if (args.length < 2) {
            System.out.println("Usage: java ...CounterClient <process id> <increment> [<number of operations>]");
            System.out.println("       if <increment> equals 0 the request will be read-only");
            System.out.println("       default <number of operations> equals 1000");
            System.exit(-1);
        }
        hermesCodeInjection(args);
        ServiceProxy counterProxy = new ServiceProxy(Integer.parseInt(args[0]));

        //counterProxy.setInvokeTimeout(1);
        //Stats stats = new Stats();
        long start, end;
        int result = 0;

        try {

            int inc = Integer.parseInt(args[1]);
            int numberOfOps = (args.length > 2) ? Integer.parseInt(args[2]) : 1000;

            boolean wait = false;

            if ((args.length > 2 && args[2].equals("wait"))
                    || (args.length > 3 && args[3].equals("wait"))) {
                wait = true;
            }


            Logger.debug = false;
            BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));

            for (int i = 0; i < numberOfOps; i++) {

                if (wait) {
                    System.out.println("Iteration " + i);
                    System.out.println("Press Enter for next iteration, type 'exit' to exit or type 'go' to run all remaining iterations");

                    String lido = inReader.readLine();

                    if (lido.equals("exit")) {
                        break;
                    } else if (lido.equals("go")) {
                        wait = false;
                    }
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream(4);
                new DataOutputStream(out).writeInt(inc);

                System.out.println("Counter sending: " + i);
                start = System.nanoTime();
                byte[] reply;
                if (inc == 0) {
                    reply = counterProxy.invokeUnordered(out.toByteArray());
                } else {
                    reply = counterProxy.invokeOrdered(out.toByteArray());
                }

                end = System.nanoTime();
                //stats.collect(i, end-start);
                HermesRuntime.getInstance().notification(
                        new StatNotification(HermesRuntime.getInstance().getRuntimeID(),
                        new StatToken(i, end - start)), 10000);

                //System.out.println("delta: " + ((float) end - (float) start) / (float) 1000000);
                if (reply != null) {
                    int newValue = new DataInputStream(new ByteArrayInputStream(reply)).readInt();
                    System.out.println("Counter value: " + newValue);
                    result = 0;
                } else {
                    result = 1;
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = 1;
        } finally {
            counterProxy.close();
            System.exit(result);
        }
    }
}
