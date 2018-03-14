package strategies;
import automail.Clock;
import automail.PriorityMailItem;
import automail.StorageTube;

public class MyRobotBehaviour implements IRobotBehaviour {

    private int maxWeight;
    final static int THRESHOLD = 2000;
    private boolean newPriority; // Used if we are notified that a priority item has arrived.

    public MyRobotBehaviour(boolean strong) {
        newPriority = false;
        if (strong) {
            maxWeight = THRESHOLD;
        }
        else {
            maxWeight = 5000;
        }
    }

    public void startDelivery() {
        newPriority = false;
    }

    @Override
    public void priorityArrival(int priority, int weight) {
        // Oh! A new priority item has arrived.
        // (Why's it telling me the weight?)
        if (weight < maxWeight) {
            newPriority = true;
        }
    }

    @Override
    public boolean returnToMailRoom(StorageTube tube) {
        if (tube.isEmpty()) {
            return false; // Empty tube means we are returning anyway
        } else {
            // Return if we don't have a priority item and a new one came in
            Boolean priority = (tube.peek() instanceof PriorityMailItem);
            return !priority && newPriority;
        }
    }

}
