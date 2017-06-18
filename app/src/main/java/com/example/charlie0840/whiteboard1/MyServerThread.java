package com.example.charlie0840.whiteboard1;

/**
 * Created by WMS_12 on 8/1/2016.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;

public class MyServerThread extends Thread {

    private static final String LOG_TAG = "MyServerThread";

    private NetworkActivity m_activityMain;

    private UIHandler mUIHandler;

    public boolean closeCLient = false;

    Thread serverThread = null;

    private Map<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();

    public MyServerThread(NetworkActivity activityMain, UIHandler handler) {
        super();

        // Save the activity
        m_activityMain = activityMain;
        mUIHandler = handler;
    }

    @Override
    public void run() {
//        System.out.println("thread testing: start to run");
//        while (true)
//        {
//            System.out.println("thread testing: looping");
//            try
//            {
//                // Wait for new client connection
//                Log.i(LOG_TAG, "Waiting for client connection...");
//                Socket socketClient = m_activityMain.m_serverSocket.accept();                    end


//                String clientSentence;
//                String capitalizedSentence;
//                while(true)
//                {
//                    if (m_activityMain.m_serverSocket.isClosed())
//                    {
//                        break;
//                    }
//                    Socket connectionSocket = m_activityMain.m_serverSocket.accept();
//                    BufferedReader inFromClient =
//                            new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
//                    DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
//                    clientSentence = inFromClient.readLine();
//                    System.out.println("Received: " + clientSentence);
//                    capitalizedSentence = clientSentence.toUpperCase() + '\n';
//                    outToClient.writeBytes(capitalizedSentence);
//                    connectionSocket.close(); //this line was part of the solution              end
//                }


//                System.out.println("thread testing: connected to " + socketClient.getRemoteSocketAddress());
//                DataInputStream in = new DataInputStream(socketClient.getInputStream());
//                System.out.println(in.readUTF());
//
//                Log.i(LOG_TAG, "Accepted connection from " + socketClient.getInetAddress().getHostAddress());
//
//                // Read input from client socket
//                InputStream is = socketClient.getInputStream();
//                OutputStream os = socketClient.getOutputStream();
//                DataInputStream dis = new DataInputStream(is);
//                while (!socketClient.isClosed())
//                {
//                    System.out.println("received message from php: inner loop");
//                    // Read a line
//                    String sLine = dis.readLine();
//                    Log.i(LOG_TAG, "received message from php: Read client socket=[" + sLine + "]");
//                    if (sLine == null)
//                    {
//                        break;
//                    }
//                }
//
//                // Close streams
//                dis.close();
//                os.close();
//                is.close();
//
//                // Close client socket
//                Log.i(LOG_TAG, "Read data from client ok. Close connection from " + socketClient.getInetAddress().getHostAddress());
//
//                if(closeCLient)
//                    socketClient.close();
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//
//            // Stop loop when server socket is closed
//            if (m_activityMain.m_serverSocket.isClosed())
//            {
//                System.out.println("serverSocket is closed why?!?!?!?!?!?!?!?!?!?!");
//                break;
//            }
//        }
//    }
        while(!m_activityMain.inSession) {
            if(m_activityMain.inSession)
                break;
        }
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        while(true) {
            Bitmap bitmap = null;
            ClientReceive bkw = new ClientReceive();
            bkw.execute(m_activityMain.session_id);
            //bitmap = m_activityMain.drawView.getDrawingCache();
            try {
                bitmap = bkw.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (bitmap != null) {
                m_activityMain.curr = Bitmap.createBitmap(bitmap);

                m_activityMain.drawView.updateBitmap(m_activityMain.curr, m_activityMain);
                m_activityMain.breakCount = m_activityMain.breakCount + 1;

                if(m_activityMain.getRole().equals("instructor"))
                    break;

                if(m_activityMain.breakCount == 1) {
                    Message msg = mUIHandler.obtainMessage(UIHandler.RESET_CANVAS);
                    mUIHandler.sendMessage(msg);
                    m_activityMain.breakCount = 0;
                }
            }
            try {
                currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(m_activityMain.breakOut)
                break;
        }

    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = m_activityMain.serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(m_activityMain.breakOut) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();
                    System.out.println("received message: " + read + " !!!!!!!!!!!!!!!!!");
                    if(read != null) {
                        if (read.equals("update")) {
                            Bitmap bitmap;
                            ClientReceive bkw = new ClientReceive();
                            bkw.execute(m_activityMain.session_id);
                            bitmap = m_activityMain.drawView.getDrawingCache();
                            try {
                                bitmap = bkw.get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            if (bitmap != null) {

                                m_activityMain.curr = Bitmap.createBitmap(bitmap);
                                m_activityMain.drawView.updateBitmap(m_activityMain.curr, m_activityMain);
                                m_activityMain.breakCount = m_activityMain.breakCount + 1;

                                Message msg = mUIHandler.obtainMessage(UIHandler.UPDATE_CANVAS);
                                mUIHandler.sendMessage(msg);

                            }
                            System.out.println("testing connection from remote app: " + read);

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(m_activityMain.breakOut) {
                    break;
                }

            }

        }
    }
}
