package strategies;
import automail.StorageTube;

public class MyRobotBehaviour implements IRobotBehaviour {

    private int maxWeight;
    private boolean newPriority; // Used if we are notified that a priority item has arrived.

    /*** Constant which determines the threshold between a "weak" or "strong" robot */
    final static int THRESHOLD = 2000;
    /*** Determines a high priority item*/
    final static int HIGH_PRIORITY = 100;

    /**
     * Data constructor for robot behaviour. This takes a boolean of whether the robot should be considered "strong".
     * @param strong
     */
    public MyRobotBehaviour(boolean strong) {
        // If this robot is strong, then the maximum weight should be infinite, otherwise it should be the threshold
        this.maxWeight = strong ? Integer.MAX_VALUE : THRESHOLD;
        this.newPriority = false;
    }

    /**
     * This function starts a delivery
     */
    public void startDelivery() {
        // Our strategy considers it more efficient to just deliver all items in the storage tube before returning
        // as this can cause long delays which can increase the score dramatically
        // Therefore, do nothing! Just keep delivering until the tube is empty.
    }

    @Override
    public void priorityArrival(int priority, int weight) {
        // Our strategy considers it more efficient to just deliver all items in the storage tube before returning
        // as this can cause long delays which can increase the score dramatically
        // Therefore, do nothing! Just keep delivering until the tube is empty.
    }

    @Override
    public boolean returnToMailRoom(StorageTube tube) {
        // As described above, our strategy considers it more efficient to deliver all items in the storage tube
        // before returning.
        // Because of this, if the robot is returning, then the storage tube is empty by default and we should
        // return anyway.
        // So this always evaluates to false; the robot should never stop returning to the mail room.
        return false;
    }

}
