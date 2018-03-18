package strategies;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailPool implements IMailPool{
    // My first job with Robotic Mailing Solutions Inc.!
    // 2 kinds of items so two structures
    // Remember stacks from 1st year - easy to use, not sure if good choice
    private PriorityQueue<MailItem> weakNonPriorityPool;
    private PriorityQueue<PriorityMailItem> weakPriorityPool;
    private PriorityQueue<MailItem> strongNonPriorityPool;
    private PriorityQueue<PriorityMailItem> strongPriorityPool;
    private static final int MAX_TAKE = 4;
    private static final int THRESHOLD = 2000;
    private static final int HIGH_PRIORITY = 100;
    private static final int LOW_PRIORITY = 10;

    public MyMailPool(){
        // Start empty
        Comparator<MailItem> nonPriorityComparator = Comparator
                .comparing((MailItem mail) -> mail.getArrivalTime())
                .thenComparing((MailItem mail) -> mail.getDestFloor())
                .thenComparing((MailItem mail) -> mail.getWeight());

        Comparator<PriorityMailItem> PriorityComparator = Comparator
                .comparing((PriorityMailItem mail) -> mail.getPriorityLevel())
                .thenComparing((PriorityMailItem mail) -> mail.getArrivalTime())
                .thenComparing((PriorityMailItem mail) -> mail.getDestFloor())
                .thenComparing((PriorityMailItem mail) -> mail.getWeight());

        weakNonPriorityPool = new PriorityQueue<>(nonPriorityComparator);
        weakPriorityPool = new PriorityQueue<>(PriorityComparator);
        strongNonPriorityPool = new PriorityQueue<>(nonPriorityComparator);
        strongPriorityPool = new PriorityQueue<>(PriorityComparator);
    }

    public void addToPool(MailItem mailItem) {
        // Check whether it has a priority or not
        boolean heavyItem = (mailItem.getWeight() > THRESHOLD);
        if(mailItem instanceof PriorityMailItem){
            // Add to priority items
            // Kinda feel like I should be sorting or something
            addToPriorityPool(mailItem, heavyItem);
        }
        else{
            // Add to nonpriority items
            // Maybe I need to sort here as well? Bit confused now
            addToNonPriorityPool(mailItem, heavyItem);
        }
    }

    private void addToPriorityPool(MailItem mailItem, boolean heavyItem) {
        PriorityQueue<PriorityMailItem> pool = heavyItem ? strongPriorityPool : weakPriorityPool;
        pool.add((PriorityMailItem) mailItem);
    }

    private void addToNonPriorityPool(MailItem mailItem, boolean heavyItem) {
        PriorityQueue<MailItem> pool = heavyItem ? strongNonPriorityPool : weakNonPriorityPool;
        pool.add(mailItem);
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
            // Should I be getting the earliest one?
            // Surely the risk of the weak robot getting a heavy item is small!
            if (strongNonPriorityPool.isEmpty()) {
                return weakNonPriorityPool.poll();
            }
            return strongNonPriorityPool.poll();
        }
        else {
            return null;
        }
    }

    private MailItem getHighestPriorityMail(int weightLimit){
        if(getPriorityPoolSize(weightLimit) > 0){
            // Should I be getting the earliest one?
            // Surely the risk of the weak robot getting a heavy item is small!
            if (strongPriorityPool.isEmpty()) {
                return weakPriorityPool.poll();
            }
            return strongPriorityPool.poll();
        }
        else {
            return null;
        }

    }

    // Never really wanted to be a programmer any way ...

    @Override
    public void fillStorageTube(StorageTube tube, boolean strong) {
        int max = strong ? Integer.MAX_VALUE : 2000; // max weight
        // Priority items are important;
        // if there are some, grab one and go, otherwise take as many items as we can and go
        try{
            // Start afresh by emptying undelivered items back in the pool
            while(!tube.isEmpty()) {
                addToPool(tube.pop());
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
