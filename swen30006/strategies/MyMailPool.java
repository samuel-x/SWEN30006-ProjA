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
    // Instead of 2 structures, we'll have 4 priority queues which will be separated depending on priority and weight
    // This will allow for better management of mail between the two different kinds of robots
    /*** A priority queue of mail items that have weight less than the threshold and are non priority*/
    private ArrayList<MailItem> weakNonPriorityPool;
    /*** A priority queue of mail items that have weight more than the threshold and are non priority*/
    private ArrayList<MailItem> strongNonPriorityPool;
    /*** A priority queue of mail items that have weight less than the threshold and are priority*/
    private ArrayList<PriorityMailItem> weakPriorityPool;
    /*** A priority queue of mail items that have weight more than the threshold and are priority*/
    private ArrayList<PriorityMailItem> strongPriorityPool;

    // Declare constants
    /*** Maximum number of items that can be held in the Robot's storage tube */
    private static final int MAX_TAKE = 4;
    /*** Maximum weight for the "weak" robots */
    private static final int THRESHOLD = 2000;

    private static final Comparator<MailItem> nonPriorityComparator = Comparator
                .comparing((MailItem mail) -> mail.getDestFloor())
                .thenComparing((MailItem mail) -> mail.getArrivalTime())
                .thenComparing((MailItem mail) -> mail.getWeight());

    private static final Comparator<PriorityMailItem> priorityComparator = Comparator
                .comparing((PriorityMailItem mail) -> mail.getPriorityLevel())
                .thenComparing((PriorityMailItem mail) -> mail.getArrivalTime())
                .thenComparing((PriorityMailItem mail) -> mail.getDestFloor());

    public MyMailPool(){
        // First create a comparator which will determine how items are sorted in the priority queue.
        // All non priority items should be sorted in order of Destination, then Arrival Time, then Weight
//        Comparator<MailItem> nonPriorityComparator = Comparator
//                .comparing((MailItem mail) -> mail.getDestFloor())
//                .thenComparing((MailItem mail) -> mail.getArrivalTime());
//                .thenComparing((MailItem mail) -> mail.getWeight());
//        // Priority items should be sorted in order of priority level, then arrival time (since time is more important)
//        // destination and weight.
//        Comparator<PriorityMailItem> PriorityComparator = Comparator
//                .comparing((PriorityMailItem mail) -> mail.getPriorityLevel())
//                .thenComparing((PriorityMailItem mail) -> mail.getArrivalTime())
//                .thenComparing((PriorityMailItem mail) -> mail.getDestFloor());
//                //.thenComparing((PriorityMailItem mail) -> mail.getWeight());

        Comparator<MailItem> comparator = Comparator.comparing((MailItem mail) -> scoreMailItem(mail));
        // Assign new priority queues with the specified comparators above
        weakNonPriorityPool = new ArrayList<>();
        weakPriorityPool = new ArrayList<>();
        strongNonPriorityPool = new ArrayList<>();
        strongPriorityPool = new ArrayList<>();
    }

    public int scoreMailItem(MailItem mailItem) {
        double priority =
                (mailItem instanceof PriorityMailItem) ? ((PriorityMailItem) mailItem).getPriorityLevel() : 0.0;
        double estDeliveryTime = Clock.Time() + mailItem.getDestFloor();
        double ArrivalTime = mailItem.getArrivalTime();
        double score = Math.pow((estDeliveryTime), (1+priority));
        System.out.print(estDeliveryTime + " " + score + " " + (mailItem instanceof PriorityMailItem) + " ");
        System.out.println(mailItem);
        return (int)Math.floor(score);

    }

    public void addToPool(MailItem mailItem) {
        // Check whether it has a priority or not
        boolean heavyItem = (mailItem.getWeight() > THRESHOLD);
        if(mailItem instanceof PriorityMailItem){
            // Add to priority items
            // Kinda feel like I should be sorting or something
            ArrayList<PriorityMailItem> pool = heavyItem ? strongPriorityPool : weakPriorityPool;
            System.out.println(pool);
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
//        System.out.println("Non Priority Pool " + this.nonPriorityPool);
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
