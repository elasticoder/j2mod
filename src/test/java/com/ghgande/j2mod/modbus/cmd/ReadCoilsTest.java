/*
 * This file is part of j2mod.
 *
 * j2mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * j2mod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses
 */
package com.ghgande.j2mod.modbus.cmd;

import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.msg.ReadCoilsRequest;
import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.ghgande.j2mod.modbus.util.Logger;

/**
 * Class that implements a simple command line tool for reading a digital input.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class ReadCoilsTest {

    private static final Logger logger = Logger.getLogger(ReadCoilsTest.class);

    private static void printUsage() {
        logger.debug("java com.ghgande.j2mod.modbus.cmd.ReadDiscretesTest <connection [String]> <unit [int8]> <register [int16]> <bitcount [int16]> {<repeat [int]>}");
    }

    public static void main(String[] args) {
        ReadCoilsRequest req;
        ReadCoilsResponse res;
        ModbusTransport transport = null;
        ModbusTransaction trans;
        int ref = 0;
        int count = 0;
        int repeat = 1;
        int unit = 0;

        try {

            // 1. Setup the parameters
            if (args.length < 4 || args.length > 5) {
                printUsage();
                System.exit(1);
            }
            else {
                try {
                    transport = ModbusMasterFactory.createModbusMaster(args[0]);

                    if (transport instanceof ModbusSerialTransport) {
                        ((ModbusSerialTransport)transport).setReceiveTimeout(500);
                        if (System.getProperty("com.ghgande.j2mod.modbus.baud") != null) {
                            ((ModbusSerialTransport)transport).setBaudRate(Integer.parseInt(System.getProperty("com.ghgande.j2mod.modbus.baud")));
                        }
                        else {
                            ((ModbusSerialTransport)transport).setBaudRate(19200);
                        }
                    }

					/*
                     * There are a number of devices which won't initialize immediately
					 * after being opened.  Take a moment to let them come up.
					 */
                    Thread.sleep(2000);

                    unit = Integer.parseInt(args[1]);
                    ref = Integer.parseInt(args[2]);
                    count = Integer.parseInt(args[3]);
                    if (args.length == 5) {
                        repeat = Integer.parseInt(args[4]);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    printUsage();
                    System.exit(1);
                }
            }

            req = new ReadCoilsRequest(ref, count);
            req.setUnitID(unit);
            logger.debug("Request: " + req.getHexMessage());

            // 4. Prepare the transaction
            trans = transport.createTransaction();
            trans.setRequest(req);

            if (trans instanceof ModbusTCPTransaction) {
                ((ModbusTCPTransaction)trans).setReconnecting(true);
            }

            // 5. Execute the transaction repeat times
            int k = 0;
            do {
                trans.execute();

                res = (ReadCoilsResponse)trans.getResponse();

                logger.debug("Response: " + res.getHexMessage());

                logger.debug("Digital Inputs Status=" + res.getCoils().toString());

                k++;
            } while (k < repeat);

            // 6. Close the connection
            transport.close();

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        System.exit(0);
    }
}