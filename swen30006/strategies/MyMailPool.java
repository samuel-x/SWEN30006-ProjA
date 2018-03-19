package strategies;

import java.util.Comparator;
import java.util.ArrayList;

import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;
import javafx.scene.layout.Priority;

/***
 * MailPool class which manages the movement of mail throughout the system.
 */
public class MyMailPool implements IMailPool{
    // My first job with Robotic Mailing Solutions Inc.!
    // Instead of 2 structures, we'll have 4 arraylists which will be separated depending on priority and weight
    // This will allow for better management of mail between the two different kinds of robots
    /*** An arraylist of mail items that have weight less than the threshold and are non priority*/
    private ArrayList<MailItem> weakNonPriorityPool;
    /*** An arraylist of mail items that have weight more than the threshold and are non priority*/
    private ArrayList<MailItem> strongNonPriorityPool;
    /*** An arraylist of mail items that have weight less than the threshold and are priority*/
    private ArrayList<PriorityMailItem> weakPriorityPool;
    /*** An arraylist of mail items that have weight more than the threshold and are priority*/
    private ArrayList<PriorityMailItem> strongPriorityPool;

    // Declare constants
    /*** Maximum number of items that can be held in the Robot's storage tube */
    private static final int MAX_TAKE = 4;
    /*** Maximum weight for the "weak" robots */
    private static final int THRESHOLD = 2000;

    public MyMailPool(){
        // Assign new ArrayLists
        weakNonPriorityPool = new ArrayList<>();
        weakPriorityPool = new ArrayList<>();
        strongNonPriorityPool = new ArrayList<>();
        strongPriorityPool = new ArrayList<>();
    }

    /***
     * This function takes a mail item and assigns it a score. This score is derived from the equation given in the spec
     * Particularly, it favours a higher delivery time as this keeps the score lower.
     * @param mailItem - mail item to score
     * @return score - an integer of the score
     */
    private int scoreMailItem(MailItem mailItem) {
        // Check if the mail is a priority. If so, then take the priority level, otherwise the priority is zero.
        double priority =
                (mailItem instanceof PriorityMailItem) ? ((PriorityMailItem) mailItem).getPriorityLevel() : 0.0;

        // Estimate the delivery time by the current time + the distance away from the start
        double estDeliveryTime = Clock.Time() + mailItem.getDestFloor();

        // score the mail
        double score = Math.pow((estDeliveryTime), (1.1 * (1+priority)));

        // return an int to be used by Collections.sort()
        return (int) Math.floor(score);
    }

    /**
     * This method adds mail to the pools
     * @param mailItem the mail item being added.
     */
    public void addToPool(MailItem mailItem) {
        // Check whether it has a priority or not
        boolean heavyItem = (mailItem.getWeight() > THRESHOLD);
        if(mailItem instanceof PriorityMailItem){
            // Add to priority items
            // Kinda feel like I should be sorting or something
            ArrayList<PriorityMailItem> pool = heavyItem ? strongPriorityPool : weakPriorityPool;
            pool.add((PriorityMailItem) mailItem);
        }
        else{
            // Add to nonpriority items
            // Maybe I need to sort here as well? Bit confused now
            ArrayList<MailItem> pool = heavyItem ? strongNonPriorityPool : weakNonPriorityPool;
            pool.add(mailItem);
        }
    }

    private int getNonPriorityPoolSize(int weightLimit) {
        // This was easy until we got the weak robot
        // Oh well, there's not that many heavy mail items -- this should be close enough
        if (weightLimit > THRESHOLD) {
            if (strongNonPriorityPool.isEmpty()) {
                return weakNonPriorityPool.size();
            }
            else {
                return strongNonPriorityPool.size();
            }
        }
        else {
            return weakNonPriorityPool.size();
        }
    }

    private int getPriorityPoolSize(int weightLimit){
        // Same as above, but even less heavy priority items -- hope this wordks too
        if (weightLimit > THRESHOLD) {
            if (strongPriorityPool.isEmpty()) {
                return weakPriorityPool.size();
            }
            else {
                return strongPriorityPool.size();
            }
        }
        else {
            return weakPriorityPool.size();
        }
    }

    private MailItem getNonPriorityMail(int weightLimit){
        if(getNonPriorityPoolSize(weightLimit) > 0){
            ArrayList<MailItem> pool = ((weightLimit > THRESHOLD) && (!strongNonPriorityPool.isEmpty())) ? strongNonPriorityPool : weakNonPriorityPool;

            pool.sort((m1, m2) -> scoreMailItem(m1) - scoreMailItem(m2));
            return pool.remove(0);

//            if (weightLimit > THRESHOLD) {
//                // Should I be getting the earliest one?
//                // Surely the risk of the weak robot getting a heavy item is small!
//                if (strongNonPriorityPool.isEmpty()) {
//                    return weakNonPriorityPool.sort(;
//                }
//                return strongNonPriorityPool.remove(0);
//            }
//            else {
//                return weakNonPriorityPool.remove(0);
//            }
        }
        else {
            return null;
        }
    }

    private MailItem getHighestPriorityMail(int weightLimit){
        if(getPriorityPoolSize(weightLimit) > 0){
            ArrayList<PriorityMailItem> pool = ((weightLimit > THRESHOLD) && (!strongPriorityPool.isEmpty())) ? strongPriorityPool : weakPriorityPool;

            pool.sort((m1, m2) -> scoreMailItem(m1) - scoreMailItem(m2));
            return pool.remove(0);
//
//            if (weightLimit > THRESHOLD) {
//                if (strongPriorityPool.isEmpty()) {
//                    return weakPriorityPool.remove(0);
//                }
//                return strongPriorityPool.remove(0);
//            }
//            else {
//                return weakPriorityPool.remove(0);
//            }
        }
        else {
            return null;
        }

    }

    // Never really wanted to be a programmer any way ...

    @Override
    public void fillStorageTube(StorageTube tube, boolean strong) {
        int max = strong ? Integer.MAX_VALUE : THRESHOLD; // max weight
        // Priority items are important;
        // if there are some, grab one and go, otherwise take as many items as we can and go
        try{
            // Start afresh by emptying undelivered items back in the pool
            while(!tube.isEmpty()) {
                if (!(tube.peek() instanceof PriorityMailItem)) {
                    addToPool(tube.pop());
                }
            }
            // Check for a top priority item
            // Add priority mail item
            if (getPriorityPoolSize(max) > 0) {
                while(tube.getSize() < MAX_TAKE && getPriorityPoolSize(max) > 0) {
                    tube.addItem(getHighestPriorityMail(max));

                }
            }
            else{
                // Get as many nonpriority items as available or as fit
                while(tube.getSize() < MAX_TAKE && getNonPriorityPoolSize(max) > 0) {
                    tube.addItem(getNonPriorityMail(max));
                }
            }
        }
        catch(TubeFullException e){
            e.printStackTrace();
        }
    }

}
