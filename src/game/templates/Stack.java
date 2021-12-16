package game.templates;

import game.managers.ItemManager;

public class Stack implements Cloneable{
    public int count;
    public int itemid;

    public Item getItem(){
        return ItemManager.IMGR.getItem(itemid);
    }
    Stack(int itemid, int count){
        this.itemid = itemid;
        this.count = count;
    }
    Stack(){

    }

    //returns the amount of items that were not able to fit in the stack, 0 if item count valid
    int addCount(int x){
        int maxstack = getItem().max_stack;
        this.count+=x;
        if(this.count>maxstack){
            int diff = count - maxstack;
            this.count = maxstack;
            return diff;
        }
        return 0;
    }
    int removeCount(int x){
        int diff = count-x;
        if(diff<0)
            count = 0;
        else
            count = diff;
        return diff;
    }
    @Override
    protected Stack clone() throws CloneNotSupportedException {
        Stack st = new Stack();
        st.count = this.count;
        st.itemid = this.itemid;
        return st;
    }
}
