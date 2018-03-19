package strategies;

import java.util.ArrayList;

import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

/***
 * MailPool class which manages the movement of mail throughout the system.
 */
public class MyMailPool implements IMailPool{
    // My first job with Robotic Mailing Solutions Inc.!

    // Instead of 2 structures, we'll have 2 arraylists which will be separated depending on priority
    /*** An arraylist of mail items that are non priority*/
    private ArrayList<MailItem> nonPriorityPool;
    /*** An arraylist of mail items that are priority*/
    private ArrayList<PriorityMailItem> priorityPool;

    // Declare constants
    /*** Maximum number of items that can be held in the Robot's storage tube */
    private static final int MAX_TAKE = 4;
    /*** Maximum weight for the "weak" robots */
    private static final int THRESHOLD = 2000;
    /*** To be used when removing items from the head of an ArrayList */
    private static final int HEAD = 0;

    public MyMailPool(){
        // Assign new ArrayLists
        nonPriorityPool = new ArrayList<>();
        priorityPool = new ArrayList<>();
    }

    /***
     * This function takes a mail item and assigns it a score. This score is derived from the equation given in the spec
     * Specifically, it favours a higher delivery time as this allows items to be delivered faster.
     * @param mailItem - mail item to score
     * @return score - an integer of the score
     */
    private int scoreMailItem(MailItem mailItem) {
        // Check if the mail is a priority. If so, then take the priority level, otherwise the priority is zero.
        double priority =
                (mailItem instanceof PriorityMailItem) ? ((PriorityMailItem) mailItem).getPriorityLevel() : 0.0;

        // Estimate the delivery time by the current time + the distance away from the start
        double estDeliveryTime = Clock.Time() + mailItem.getDestFloor();

        // score the mail using a scoring equation that favours priority and then estimate delivery time
        double score = (estDeliveryTime + priority);

        // return an int to be used by Collections.sort()
        return (int) Math.floor(score);
    }

    /**
     * This method adds mail to the pools
     * @param mailItem the mail item being added.
     */
    public void addToPool(MailItem mailItem) {

        // Check if the item is a priority item
        if(mailItem instanceof PriorityMailItem){
            // Add to priority items
            // if the item is heavy, add the item to the respective arraylist
            priorityPool.add((PriorityMailItem) mailItem);

            // Sort the priority pool
            priorityPool.sort((m1, m2) -> scoreMailItem(m1) - scoreMailItem(m2));
        }
        else {
            // Add to nonpriority items
            // if the item is heavy, add to the respective arraylist
            nonPriorityPool.add(mailItem);

            // Sort the non priority pool
            nonPriorityPool.sort((m1, m2) -> scoreMailItem(m1) - scoreMailItem(m2));
        }

    }

    /**
     * This method gets the number of available non priority mail with the given weight
     * @param weightLimit - the maximum weight the robot can carry
     * @return int - size of the available pool of mail
     */
    private int getNonPriorityPoolSize(int weightLimit) {
        // Use streaming to filter out any items that are too heavy, and then get the number of remaining mail items
        return (int) nonPriorityPool.stream().filter(m -> m.getWeight() < weightLimit).count();
    }

    /**
     * This method gets the number of available priority mail with the given weight
     * @param weightLimit - the maximum weight the robot can carry
     * @return int - size of the available pool of mail
     */
    private int getPriorityPoolSize(int weightLimit) {
        // Use streaming to filter out any items that are too heavy, and then get the number of remaining mail items
        return (int) priorityPool.stream().filter(m -> m.getWeight() < weightLimit).count();
    }

    /**
     * This gets the highest ranked "important" non-priority mail
     * @param weightLimit - the maximum weight the robot can carry
     * @return MailItem - the most important non-priority mail
     */
    private MailItem getNonPriorityMail(int weightLimit){
        // If there is mail available to be picked up
        if(getNonPriorityPoolSize(weightLimit) > 0){
            int index = 0;
            // then find the most important mail that is under the weight limit and return it
            for (MailItem mail : nonPriorityPool) {
                if (mail.getWeight() < weightLimit) {
                    break;
                }
                index++;
            }
            // remove the mail from the pool
            return nonPriorityPool.remove(index);
        }

        // in the event that there is no mail available, return null
        return null;
    }

    /**
     * This gets the highest ranked "important" priority mail
     * @param weightLimit - the maximum weight the robot can carry
     * @return MailItem - the most important priority mail
     */
    private MailItem getHighestPriorityMail(int weightLimit){
        // If there is mail available to be picked up
        if(getPriorityPoolSize(weightLimit) > 0){
            int index = 0;
            // then find the most important mail that is under the weight limit and return it
            for (MailItem mail : priorityPool) {
                if (mail.getWeight() < weightLimit) {
                    break;
                }
                index++;
            }
            // remove the mail from the pool
            return priorityPool.remove(index);
        }

        // if there is no mail available, return null
        return null;
    }

    @Override
    public void fillStorageTube(StorageTube tube, boolean strong) {
        int max = strong ? Integer.MAX_VALUE : THRESHOLD; // max weight
        try{
            // Given our Robot Behaviour does not return even if a new priority item appears,
            // we do not need to account for our tube containing any mail.

            // Add mail items to a temporary staging "tube" which will be sorted
            // in reverse order (as items on top should be delivered first)

            ArrayList<MailItem> temp = new ArrayList<>();

            // If there are any available priority mail, add these to the staging area first
            if (getPriorityPoolSize(max) > 0) {
                // Get as many priority items as available
                while(temp.size() < MAX_TAKE && getPriorityPoolSize(max) > 0) {
                    temp.add(getHighestPriorityMail(max));
                }
            }
            else{
                // Get as many non priority items as available
                while(temp.size() < MAX_TAKE && getNonPriorityPoolSize(max) > 0) {
                    temp.add(getNonPriorityMail(max));
                }
            }
            // Reverse sort these by destination, so the closest are delivered first
            temp.sort((m1, m2) -> m2.getDestFloor() - m1.getDestFloor());

            // Put all staged mail items into the actual tube, and send them off!
            while (!temp.isEmpty()) {
                tube.addItem(temp.remove(HEAD));
            }

        }
        catch(TubeFullException e){
            e.printStackTrace();
        }
    }

}
