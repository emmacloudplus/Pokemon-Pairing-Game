package controller;
/**
 * Client Thread Class
 */
public class ConcentrationControllerThread extends Thread {
    private ConcentrationController controller;
    private int row;
    private int col;

    /**
     * Constructor
     * sets coordinates and controller
     *
     * @param row coordinate of card
     * @param col coordinate of card
     * @param controller
     */
    public ConcentrationControllerThread(ConcentrationController controller, int row, int col) {
        this.controller = controller;
        this.row = row;
        this.col = col;
    }

    @Override
    public void run() {
        controller.clientThreadRun(row, col);
    }
}
