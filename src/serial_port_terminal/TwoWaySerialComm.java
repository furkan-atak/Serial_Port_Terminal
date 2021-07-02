/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial_port_terminal;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pc
 */
public class TwoWaySerialComm {

    public static String lastData;
    public static OutputStream out;
    public TwoWaySerialComm() {
        super();
        lastData = "";
    }

    void connect(String portName,String baudRate) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(Integer.parseInt(baudRate), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                InputStream in = serialPort.getInputStream();
                out = serialPort.getOutputStream();

                (new Thread(new SerialReader(in))).start();

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public static class SerialReader implements Runnable {

        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            String theReceived = "";
            boolean isADC = false;
            int counter = 0;
            try {
                while ((len = this.in.read(buffer)) > -1) {
                    lastData = new String(buffer, 0, len);
                    System.out.print(new String(buffer, 0, len));
                    theReceived += lastData;
                    counter++;
                    if(theReceived.length() > 1 && !theReceived.contains("x")){
                        isADC = true;
                    }
                    if(theReceived.length() == 33 && !isADC){
                        Main.theMain.ReceiveMessage(theReceived);
                        theReceived = "";
                    }else if(theReceived.contains("\n") && isADC){
                        Main.theMain.ReceiveMessage(theReceived);
                        theReceived = "";
                        isADC = false;
                    }else if(theReceived.contains(".")){
                        Main.theMain.ReceiveMessage(theReceived);
                        theReceived = "";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    void SerialWriter(String theTxt) {
        try {
            TwoWaySerialComm.out.write(theTxt.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(TwoWaySerialComm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*public static class SerialWriter implements Runnable {

        OutputStream out;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        public void run() {
            try {
                int c = 0;
                while ((c = System.in.read()) > -1) {
                    this.out.write(c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}
