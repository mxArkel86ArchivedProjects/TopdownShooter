package game.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemManager {
    public static ItemManager IMGR = new ItemManager();

    Map<String, Integer> assetIdentifier = new HashMap<String, Integer>();
    List<Item> assets = new ArrayList<Item>();
    public int addAsset(String name, int asset, int max_stack){
        int index = assets.size();
        Item item = new Item();
        item.name = name;
        item.asset = asset;
        item.max_stack = max_stack;
        if(itemID(name)!=-1){
            return 1;
        }else{
        assets.add(item);
        assetIdentifier.put(name, index);
        }
        return 0;
	}

    public List<Item> getAssets(){
        return assets;
    }

    public Item getItem(int i){
        return assets.get(i);
    }
    public Item getItem(String name){
        int i = assetIdentifier.get(name);
        return assets.get(i);
    }

    public int itemID(String name){
        if(assetIdentifier.get(name)!=null)
            return assetIdentifier.get(name);
        return -1;
    }
}