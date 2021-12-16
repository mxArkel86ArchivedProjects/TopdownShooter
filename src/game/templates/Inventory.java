package game.templates;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    public int stacks_max;
    public Stack stacks[];

    Inventory(int stacks_max) {
        this.stacks_max = stacks_max;
        stacks = new Stack[stacks_max];
    }

    Item getItemAt(int i) {
        return stacks[i].getItem();
    }

    // 0 - no error
    // 1 - full inventory
    public int addItemsToInv(int item, int count) {
        for (Stack s : stacks) {
            if(s==null)
                continue;
            if (s.itemid == item) {
                count = s.addCount(count);
                System.out.println(String.format("count=%d", count));
            }
        }
        if (!hasSpace())
            return 1;
        if (count > 0) {
            int ret = addItemsNewStacks(item, count);
            if (ret == 1)
                return 1;
            else
                return 0;

        }
        return 0;
    }

    // 0 - all items have been stored
    // 1 - some items still remain on hold
    int addItemsNewStacks(int item, int count) {
        final int maxstack = ItemManager.IMGR.getItem(item).max_stack;
        for (int i = 0; i < stacks_max; i++) {
            if (stacks[i] == null) {
                if (count > maxstack) {
                    stacks[i]= new Stack(item, maxstack);
                    count -= maxstack;
                } else {
                    stacks[i]= new Stack(item, count);
                    return 0;
                }
            }
        }
        if (count > 0)
            return 1;
        return 0;
    }

    int count(){
        int count = 0;
        for(int i =0;i<stacks_max;i++){
            if(stacks[i]!=null)
            count++;
        }
        return count;
    }
    boolean hasSpace() {
        return count() < stacks_max;
    }
}
