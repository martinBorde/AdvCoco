package main;

import generated.PallierType;
import java.lang.Math;
import generated.ProductsType;
import generated.TyperatioType;
import generated.ProductType;
import generated.World;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.xml.bind.*;



public class Services {
	
    World readWorldFromXml(String username){
        World world = new World();
        InputStream input = null;
        System.out.println("!!!Init!!!");
        try {
            input = new FileInputStream(username+"-world.xml");
            System.out.println("!!!Recup username world.xml!!!");
        } catch (Exception e) {
        }
        if (input==null) {
            input = getClass().getClassLoader().getResourceAsStream("world.xml");
            System.out.println("!!!Recup world.xml!!!");
        }
        try {
            JAXBContext jc = JAXBContext.newInstance(World.class);
            Unmarshaller u = jc.createUnmarshaller();
            world = (World) u.unmarshal(input);
            System.out.println("!!!Unmarshall!!!");
        } catch (Exception e) {
            e.printStackTrace();
            
        }
        return world;
    }

    void saveWordlToXml(World world, String username){
        OutputStream output = null;
        try {
            output = new FileOutputStream(username+"world.xml");
            JAXBContext jc = JAXBContext.newInstance(World.class);
            Marshaller u = jc.createMarshaller();
            u.marshal(world, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    World getWorld(String username) {
        World world = readWorldFromXml(username);
        updateScore(world);
        saveWordlToXml(world, username);
        return world;
    }
    
    public void updateScore(World world) {
    	long lastUpdate = world.getLastupdate();
    	world.setLastupdate(System.currentTimeMillis());
    	long currentUpdate = world.getLastupdate();
    	long lapsTime = currentUpdate - lastUpdate;
    	ProductsType products = world.getProducts();
    	int angelbonus = world.getAngelbonus();
    	double activeangels = world.getActiveangels();
    	for (ProductType product : products.getProduct()) {
    		int producted = 0;
    		if (product.isManagerUnlocked()) {
    			// soustraire timeleft à lapstime si lapstime est supérieur ou égal à timeleft
    			//ou si timeleft est nul
    			if (product.getTimeleft()<=lapsTime || product.getTimeleft() == 0) {
    				lapsTime -= product.getTimeleft();
		    		long vitesse = product.getVitesse();
		    		producted += lapsTime/vitesse;
		    		long inproduction = lapsTime%vitesse;
		    		product.setTimeleft(inproduction);
    			} else {
    				//le temps écoulé n'a pas suffit à fair un cycle de production
    				//on met juste à jour le timeleft
    				product.setTimeleft(product.getTimeleft()-lapsTime);
    			}
	    		
    		} else {
    			if (product.getTimeleft()!=0 && product.getTimeleft()<lapsTime) {
    				producted = 1;
    			} else {
    				product.setTimeleft(product.getTimeleft()-lapsTime);
    			}
    		}
    		double newscore = producted*product.getRevenu()*product.getQuantite()*(1+activeangels+angelbonus/100);
    		world.setScore(world.getScore()+newscore);
    		world.setMoney(world.getMoney()+newscore);
    	}	
    }
    
    
    public ProductType findProductById(World world, int id) {
        ProductType product = null;
        try {
            product = world.getProducts().getProduct().get(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return product;
    }

    public PallierType findManagerByName(World world, String name) {
        PallierType manager = null;
        int index = world.getManagers().getPallier().indexOf(name);
        if (index != -1) {
            manager = world.getManagers().getPallier().get(index);
        }
        return manager;
    }

    public boolean updateProduct(String username, ProductType newproduct) {
        World world = getWorld(username);
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null)
            return false;
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
        	double money = world.getMoney();
        	if (money-product.getCout() >= 0) {
        		world.setMoney(money-product.getCout());
                product.setQuantite(product.getQuantite()-qtchange);
        	} else {
        		return false;
        	}
        } else {
        	product.setTimeleft(product.getVitesse());
        }
        for (PallierType pallier : product.getPalliers().getPallier()) {
        	applyAllunlock(pallier, world);
        	if (newproduct.getQuantite() >= pallier.getSeuil() && ! pallier.isUnlocked()) {
        		applyPallier(pallier, product, world);
        	}
        }
        saveWordlToXml(world, username);
        return true;
    }

    public boolean updateManager(String username, PallierType newmanager) {
        World world = getWorld(username);
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) 
            return false;
        if (world.getMoney()-manager.getSeuil() < 0)
        	return false;
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null)
            return false;
        manager.setUnlocked(true);
        product.setManagerUnlocked(true);
        double money = world.getMoney();
        world.setMoney(money-manager.getSeuil());
        saveWordlToXml(world, username);
        return true;
    }
    
    public void applyPallier(PallierType pallier, ProductType product, World world) {
    	pallier.setUnlocked(true);
		TyperatioType type = pallier.getTyperatio();
		double ratio = pallier.getRatio();
		if (type.value() == "gain") {
			product.setRevenu(product.getRevenu()*ratio);
		}
		if (type.value() == "vitesse") {
			product.setVitesse(product.getVitesse()*(int)ratio);
		}
		if (type.value() == "ange") {
			world.setAngelbonus(world.getAngelbonus()*(int)ratio);
		}
    }
    
    public boolean applyUpgrades(String username, PallierType upgrade) {
    	World world = getWorld(username);
    	if (world.getMoney()-upgrade.getSeuil() < 0)
    		return false;
	    if (upgrade.getIdcible() == 0) {
	    	for (ProductType product : world.getProducts().getProduct()) {
	    		applyPallier(upgrade, product, world);
	    	}
	    } else {
	    	ProductType product = findProductById(world, upgrade.getIdcible());
	    	applyPallier(upgrade, product, world);
	    }
	    world.setMoney(world.getMoney()-upgrade.getSeuil());
	    saveWordlToXml(world, username);
    	return true;
    }
    
    public boolean applyReset(String username) {
    	World world = getWorld(username);
    	double totalangels = world.getTotalangels();
    	double activeangels = world.getActiveangels();
    	double score = world.getScore();
    	double newangels = 150*Math.sqrt(score/Math.pow(10, 15)-totalangels);
    	World newworld = readWorldFromXml(""); 
    	newworld.setTotalangels(totalangels + newangels);
    	newworld.setActiveangels(activeangels + newangels);
    	newworld.setScore(score);
    	saveWordlToXml(newworld, username);
    	return true;
    }
    
    // possibilité de factoriser avec applyUpgrades
    public boolean applyAngelUpgrade(String username, PallierType angelupgrade) {
    	World world = getWorld(username);
    	if (world.getActiveangels() - angelupgrade.getSeuil() < 0)
    		return false;
    	if (angelupgrade.getIdcible() == 0) {
	    	for (ProductType product : world.getProducts().getProduct()) {
	    		applyPallier(angelupgrade, product, world);
	    	}
    	} else {
    		ProductType product = findProductById(world, angelupgrade.getIdcible());
	    	applyPallier(angelupgrade, product, world);
    	}
    	world.setActiveangels(world.getActiveangels()-angelupgrade.getSeuil());
    	saveWordlToXml(world, username);
    	return true;
    }
    
    public boolean applyAllunlock(PallierType allunlock, World world) {
    	for (ProductType product : world.getProducts().getProduct()) {
    		if (product.getQuantite() < allunlock.getSeuil()) {
    			return false;
    		}
    	}
    	allunlock.setUnlocked(true);
    	return true;
    }
    

}

 