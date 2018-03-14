package strategies;

import java.util.ArrayList;
import java.util.Stack;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailPool implements IMailPool{
    // My first job with Robotic Mailing Solutions Inc.!
    // 2 kinds of items so two structures
    // Remember stacks from 1st year - easy to use, not sure if good choice
    private ArrayList<MailItem> nonPriorityPool;
    private ArrayList<PriorityMailItem> priorityPool;
    private static final int MAX_TAKE = 4;
    private static final int HIGH_PRIORITY = 100;
    private static final int LOW_PRIORITY = 10;

    public MyMailPool(){
        // Start empty
        nonPriorityPool = new ArrayList<MailItem>();
        priorityPool = new ArrayList<PriorityMailItem>();
    }

    public void addToPool(MailItem mailItem) {
        // Check whether it has a priority or not
        if(mailItem instanceof PriorityMailItem){
            // Add to priority items
            // Kinda feel like I should be sorting or something
            addToPriorityPool(mailItem);
        }
        else{
            // Add to nonpriority items
            // Maybe I need to sort here as well? Bit confused now
            nonPriorityPool.add(mailItem);
        }
    }

    private void addToPriorityPool(MailItem mailItem) {
        if (priorityPool.isEmpty()) {
            priorityPool.add((PriorityMailItem) mailItem);
        }
        else {
            int index = 0;
            for (PriorityMailItem mail : priorityPool) {
                if (compareMail((PriorityMailItem) mailItem, mail, true)) {
                    break;
                }
                index++;
            }
            priorityPool.add(index, ((PriorityMailItem) mailItem));
        }
    }

    private void addToNonPriorityPool(MailItem mailItem) {
        if (nonPriorityPool.isEmpty()) {
            nonPriorityPool.add(mailItem);
        }
        else {
            int index = 0;
            for (MailItem mail : nonPriorityPool) {
                if (compareMail(mailItem, mail, false)) {
                    break;
                }
                index++;
            }
            nonPriorityPool.add(index, (mailItem));
        }
    }

    private boolean compareMail(MailItem mailA, MailItem mailB, boolean isPriority) {
        // this function compares two priority mail items for score and returns true of A is "more important" than B
        if (isPriority) {
            if (((PriorityMailItem) mailA).getPriorityLevel() > ((PriorityMailItem) mailB).getPriorityLevel()) {
                return true;
            }
             if (((PriorityMailItem) mailA).getPriorityLevel() == ((PriorityMailItem) mailB).getPriorityLevel() && mailA.getArrivalTime() < mailB.getArrivalTime()){
                 if (mailA.getArrivalTime() == mailB.getArrivalTime() && mailA.getDestFloor() > mailB.getDestFloor()) {
                        return true;
                }
            }
        }
        else if (mailA.getArrivalTime() < mailB.getArrivalTime()){
            return true;
        }
        else if (mailA.getArrivalTime() == mailB.getArrivalTime() && mailA.getDestFloor() > mailB.getDestFloor()) {
            return true;
        }
        return false;
    }

    private int getNonPriorityPoolSize(int weightLimit) {
        // This was easy until we got the weak robot
        // Oh well, there's not that many heavy mail items -- this should be close enough
        int count = 0;
        for (MailItem mail : nonPriorityPool) {
            if (mail.getWeight() < weightLimit) {
                count++;
            }
        }
        return count;
    }

    private int getPriorityPoolSize(int weightLimit){
        // Same as above, but even less heavy priority items -- hope this works too
        int count = 0;
        for (MailItem mail : priorityPool) {
            if (mail.getWeight() < weightLimit) {
                count++;
            }
        }
        return count;
    }

    private MailItem getNonPriorityMail(int weightLimit){
        if(getNonPriorityPoolSize(weightLimit) > 0){
            // Should I be getting the earliest one?
            // Surely the risk of the weak robot getting a heavy item is small!
            int index = 0;
            for (MailItem mail : nonPriorityPool) {
                if (mail.getWeight() < weightLimit) {
                    break;
                }
                index++;
            }
            return nonPriorityPool.remove(index);
        }
        else{
            return null;
        }
    }

    private MailItem getHighestPriorityMail(int weightLimit){
        if(getPriorityPoolSize(weightLimit) > 0){
            // How am I supposed to know if this is the highest/earliest?
            int index = 0;
            for (MailItem mail : priorityPool) {
                if (mail.getWeight() < weightLimit) {
                    break;
                }
                index++;
            }
            return priorityPool.remove(index);
        }
        else{
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
            if (getPriorityPoolSize(max) > 0) {
                // Add priority mail item
                tube.addItem(getHighestPriorityMail(max));
                // Won't add any more - want it delivered ASAP
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
