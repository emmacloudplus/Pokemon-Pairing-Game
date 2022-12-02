package controller;


import common.ConcentrationException;
import common.ConcentrationProtocol;
import model.ConcentrationModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * Client Class
 */
public class ConcentrationController {
    private Socket socket = null;
    private ConcentrationModel board;
    private PrintWriter out;
    private BufferedReader in;
    private int revealMsgCount;

    /**
     * Client Class
     *
     * Constructor
     * creates client game board to show matched cards
     * creates networking sockets
     *
     * @param hostName
     * @param port
     * @param board
     */
    public ConcentrationController(String hostName, int port, ConcentrationModel board) {
        this.board = board;
        try {
            this.socket = new Socket(hostName, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            board.setStatus(ConcentrationModel.Status.ERROR);
        }
        this.revealMsgCount = 0;
    }

    /**
     * Getter for game board dimensions
     */
    public int getBoardDimension() {
        try {
            String board_msg = in.readLine();
            String[] tokens = board_msg.split(" ");
            if (!tokens[0].equals("BOARD_DIM")) {
                throw new ConcentrationException("Incorrect message " + board_msg);
            }
            return Integer.parseInt(tokens[1]);
        } catch (Exception e) {
            System.out.println("Cannot get board dim, exit...");
            System.exit(1);
        }
        return -1;
    }

    /**
     * Creates controller thread
     * The thread will reveal a card
     *
     * @param row coordinate of card
     * @param col coordinate of card
     */
    public void createClientThread(int row, int col) {
        new ConcentrationControllerThread(this, row, col).start();
    }

    /**
     * Client thread run method
     */
    protected synchronized void clientThreadRun(int row, int col) {
        if (!this.board.isValid(row, col) ||
                this.board.getStatus() == ConcentrationModel.Status.GAME_OVER) {
            return;
        }
        out.println(String.format(ConcentrationProtocol.REVEAL_MSG, row, col));
        String response = "";
        try {
            response = in.readLine();
        } catch (IOException e) {
            board.setStatus(ConcentrationModel.Status.ERROR);
        }

        String[] tokens = response.split(" ");
        if (tokens[0].equals(ConcentrationProtocol.CARD)) {
            board.revealCard(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), tokens[3].charAt(0));
            revealMsgCount++;
        } else if (tokens[0].equals(ConcentrationProtocol.ERROR)) {
            board.setStatus(ConcentrationModel.Status.ERROR);
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            board.setStatus(ConcentrationModel.Status.ERROR);
        }

        if (revealMsgCount % 2 == 0) {
            try {
                response = in.readLine();
            } catch (IOException e) {
                board.setStatus(ConcentrationModel.Status.ERROR);
            }
            tokens = response.split(" ");
            if (tokens[0].equals(ConcentrationProtocol.MATCH)) {
                board.setMatch(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
            } else {
                board.setMismatch(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
            }
        }

        if (board.shouldEnd()) {
            try {
                response = in.readLine();
                if (response.equals(ConcentrationProtocol.GAME_OVER)) {
                    board.setStatus(ConcentrationModel.Status.GAME_OVER);
                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (socket != null) socket.close();
                } else {
                    board.setStatus(ConcentrationModel.Status.ERROR);
                }
            } catch (IOException e) {
                board.setStatus(ConcentrationModel.Status.ERROR);
            }
        }
    }
}
