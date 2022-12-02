package model;

import java.util.LinkedList;
import java.util.List;

/**
 * A class that create the game board on the client side of the game.
 */
public class ConcentrationModel {

    public static final char HIDDEN = '.';

    public enum Status {
        OK,
        ERROR,
        GAME_OVER
    }

    private char[][] cards;
    private int dim;
    private Status status;

    private int moves;
    private int matches;

    /**
     * the observers of this model
     */
    private List<Observer<ConcentrationModel>> observers;

    public ConcentrationModel() {
        this.status = Status.OK;
        this.moves = 0;
        this.matches = 0;
        this.observers = new LinkedList<>();
    }

    public void initBoard(int dim) {
        this.dim = dim;
        cards = new char[dim][dim];
        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
                cards[row][col] = HIDDEN;
            }
        }
    }

    public int getDim() {
        return dim;
    }

    public void setStatus(Status status) {
        this.status = status;
        notifyObservers();
    }

    public Status getStatus() {
        return status;
    }

    public int getMoves() {
        return moves;
    }

    public int getMatches() {
        return matches;
    }

    public boolean isHidden(int row, int col) {
        return cards[row][col] == HIDDEN;
    }

    public char getLetter(int row, int col) {
        if (isHidden(row, col)) {
            return HIDDEN;
        } else {
            return cards[row][col];
        }
    }

    /**
     * The view calls this method to add themselves as an observer of the model.
     *
     * @param observer the observer
     */
    public void addObserver(Observer<ConcentrationModel> observer) {
        this.observers.add(observer);
    }

    /**
     * When the model changes, the observers are notified via their update() method
     */
    private void notifyObservers() {
        for (Observer<ConcentrationModel> obs : this.observers) {
            obs.update(this);
        }
    }

    /**
     * Method to reveal a card
     * the card with [row][col] position is replaced with a new ConcentrationCard with the same [row][col] and a new value [letter]
     *
     * @param row    the row of the revealed card
     * @param col    the col of the revealed card
     * @param letter the value the revealed card will be
     */
    public void revealCard(int row, int col, char letter) {
        cards[row][col] = letter;
        moves++;
//        System.out.println(String.format("Updating the UI with row: %d, col: %d, letter: %c", row, col, letter));
        notifyObservers();
    }

    /**
     * Method to change status of two client chosen cards from hidden -> reveal (isHidden = False)
     * when card 1 and card 2 match we use this method to reveal them
     *
     * @param row1 the row of card 1
     * @param col1 the col of card 1
     * @param row2 the row of card 2
     * @param col2 the col of card 2
     */
    public void setMatch(int row1, int col1, int row2, int col2) {
        this.matches++;
        notifyObservers();
    }

    /**
     * Method to reset the "hidden" status of client two chosen cards
     * when card 1 and card 2 dont match we use this method to reset the hidden method
     *
     * @param row1 the row of card 1
     * @param col1 the col of card 1
     * @param row2 the row of card 2
     * @param col2 the col of card 2
     */
    public void setMismatch(int row1, int col1, int row2, int col2) {
        cards[row1][col1] = HIDDEN;
        cards[row2][col2] = HIDDEN;
        notifyObservers();
    }

    /**
     * Method to check to see if client input is valid
     * Two things are checked.
     * 1. Coordinates input is within bounds of gameboard dimensions
     * 2. Chosen "card" is hidden.
     *
     * @param row the row of the revealed card
     * @param col the col of the revealed card
     */
    public boolean isValid(int row, int col) {
        if (row < 0 || row >= dim || col < 0 || col >= dim) return false;
        if (cards[row][col] == HIDDEN) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method to check to see if game is finished
     * Basically checks if there are any hidden cards in the board
     * if there is then returns false. game continues
     */
    public boolean shouldEnd() {
        return matches * 2 == dim * dim;
    }
}
