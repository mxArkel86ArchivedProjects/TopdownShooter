package game.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import game.managers.ItemManager;

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
        Stack stack_[] = new Stack[stacks.length];
        
        for (int i = 0;i<stacks.length&&count>0;i++) {
            if(stacks[i]==null)
                continue;
            try {
                stack_[i] = stacks[i].clone();
            } catch (CloneNotSupportedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (Stack s : stack_) {
            if(s==null)
                continue;
            if (s.itemid == item) {
                count = s.addCount(count);
            }
        }
        if (!hasSpace())
            return 1;
        if (count > 0) {
            int ret = addItemsNewStacks(stack_, item, count);
            if (ret == 1)
                return 1;
            else{
                this.stacks = stack_;
                return 0;
            }

        }
        this.stacks = stack_;
        return 0;
    }

    public int removeItemsFromInv(int item, int count){
        Stack stack_[] = new Stack[stacks.length];

        for (int i = 0;i<stacks.length&&count>0;i++) {
            if(stacks[i]==null)
                continue;
            try {
                stack_[i] = stacks[i].clone();
            } catch (CloneNotSupportedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (int i =stack_.length-1;i>=0;i--) {
            if(stack_[i]==null)
                continue;
            if (stack_[i].itemid == item) {
                int ret = stack_[i].removeCount(count);
                if(ret<0){
                    count=-ret;
                    stack_[i] = null;
                }else if(ret>0){
                    count = 0;
                    this.stacks = stack_;
                    return 0;
                }else if(ret==0){
                    stack_[i]= null;
                    count = 0;
                    this.stacks = stack_;
                    return 0;
                }
                //System.out.println(String.format("ret=%d",ret));
            }
        }
        return 1;
    }

    public void sortItems(){
        for(int r = stacks.length-1;r>=0;r--){
            if(stacks[r]==null)
                continue;
            int l =0;
            for(l = 0;l<r;l++){
                if(stacks[l]!=null)
                    continue;
                stacks[l] = stacks[r];
                stacks[r] = null;
            }
            if(l==r)
                break;
        }
    }
    // 0 - all items have been stored
    // 1 - some items still remain on hold
    int addItemsNewStacks(Stack stacks_[], int item, int count) {
        final int maxstack = ItemManager.IMGR.getItem(item).max_stack;
        for (int i = 0; i < stacks_max; i++) {
            if (stacks_[i] == null) {
                if (count > maxstack) {
                    stacks_[i]= new Stack(item, maxstack);
                    count -= maxstack;
                } else {
                    stacks_[i]= new Stack(item, count);
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
    boolean isEmpty(){
        return count()==0;
    }
}
