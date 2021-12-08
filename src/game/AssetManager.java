package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.image.BufferedImage;

public class AssetManager {
    Map<String, Integer> assetIdentifier = new HashMap<String, Integer>();
    List<Asset> assets = new ArrayList<Asset>();
    public int addAsset(String name, BufferedImage src){
        int index = assets.size();
        Asset a = new Asset();
        a.name = name;
        a.src = src;
        if(assetID(name)!=-1){
            return 1;
        }else{
        assets.add(a);
        assetIdentifier.put(name, index);
        }
        return 0;
	}

    public List<Asset> getAssets(){
        return assets;
    }

    public Asset getAsset(int i){
        return assets.get(i);
    }
    public Asset getAsset(String name){
        int i = assetIdentifier.get(name);
        return assets.get(i);
    }

    public int assetID(String name){
        if(assetIdentifier.get(name)!=null)
            return assetIdentifier.get(name);
        return -1;
    }
}

class Asset{
	String name;
    BufferedImage src;
}