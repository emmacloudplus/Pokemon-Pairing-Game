package server;

import common.ConcentrationException;
import common.ConcentrationProtocol;
import game.ConcentrationBoard;
import game.ConcentrationCard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
/**
 * Thread class for Server-Client operations
 *
 */
public class ConcentrationClientServerThread extends Thread {
    private Socket socket = null;
    private ConcentrationBoard board;
    private int clientId;

    /**
     * Constructs thread variables
     * @param socket the unique socket for the thread
     * @param dimension dimension of the gameboard
     * @param clientId the unique ID of the client
     */
    public ConcentrationClientServerThread(Socket socket, int dimension, int clientId) throws ConcentrationException {
        this.socket = socket;
        this.board = new ConcentrationBoard(dimension);
        this.clientId = clientId;
    }



    /**
     * Client Thread Class
     *Handles responses and communication
     */
    public void run() {
        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
        {
            System.out.println(String.format("Client #%d: Client %d connected: %s", clientId, clientId, socket.toString()));
            out.println(String.format(ConcentrationProtocol.BOARD_DIM_MSG , board.getDIM()));
            System.out.println(String.format("Client #%d: Client started...", clientId));
            System.out.println("Client #" + clientId + ":");
            while (!board.gameOver()) {
                String request = in.readLine();
                String response = "";
                System.out.println(String.format("Client #%d: received: %s", clientId, request));
                String[] tokens = request.split(" ");
                if (tokens.length != 3 || !tokens[0].equals(ConcentrationProtocol.REVEAL)) {
                    System.out.println("Unexpected request: " + request);
                    break;
                }

                int row, col;
                try {
                    row = Integer.parseInt(tokens[1]);
                    col = Integer.parseInt(tokens[2]);
                } catch (Exception e) {
                    out.println("ERROR Invalid coordinate");
                    continue;
                }
                try {
                    ConcentrationCard card = board.getCard(row, col);
                    response = String.format(ConcentrationProtocol.CARD_MSG, row, col ,card.getLetter());
                    out.println(response);
                    System.out.println(String.format("Client #%d: sending: %s", clientId, response));
                    ConcentrationBoard.CardMatch cardMatch = board.reveal(row, col);
                    System.out.println(board);
                    if (cardMatch.isReady()) {
                        if (cardMatch.isMatch()) {
                            response = String.format(ConcentrationProtocol.MATCH_MSG, cardMatch.getCard1().getRow(),
                                    cardMatch.getCard1().getCol(), cardMatch.getCard2().getRow(),
                                    cardMatch.getCard2().getCol());
                        } else {
                            try {
                                Thread.sleep(1000);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                            response = String.format(ConcentrationProtocol.MISMATCH_MSG, cardMatch.getCard1().getRow(),
                                    cardMatch.getCard1().getCol(), cardMatch.getCard2().getRow(),
                                    cardMatch.getCard2().getCol());
                        }
                        out.println(response);
                        System.out.println(String.format("Client #%d: sending: %s", clientId, response));
                    }
                } catch (ConcentrationException e) {
                    out.println("ERROR Coordinates out of bounds" + "[" + tokens[1] + "]" + "[" + tokens[2] + "]");
                    continue;
                }

                if (board.gameOver()) {
                    out.println("GAME_OVER");
                    System.out.println(String.format("Client #%d: sending: %s", clientId, "GAME_OVER"));
                    System.out.println(String.format("Client #%d: Client ending...", clientId));
                    socket.close();
                }

            }
        } catch (IOException e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

}
