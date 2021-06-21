package main;

import generated.PallierType;
import generated.ProductsType;
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
    	for (ProductType product : products.getProduct()) {
    		long producted = 0;
    		long inproduction = 0;
    		if (product.isManagerUnlocked()) {
	    		long vitesse = product.getVitesse();
	    		producted = lapsTime/vitesse;
	    		inproduction = lapsTime%vitesse;
	    		if (inproduction!=0) {
	    			long timeleft = lapsTime - vitesse*producted;
	    			product.setTimeleft(timeleft);
	    		}
    		} else {
    			if (product.getTimeleft()!=0 && product.getTimeleft()<lapsTime) {
    				producted = 1;
    			} else {
    				product.setTimeleft(product.getTimeleft()-lapsTime);
    			}
    		}
    		double newscore = producted*product.getRevenu();
    		world.setScore(world.getScore()+newscore);
    	}	
    }
    
    public void applyBonus(PallierType pallier) {
    	
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
            world.setMoney(money-product.getCout());
            product.setQuantite(product.getQuantite()-qtchange);
        } else {
        	product.setTimeleft(product.getVitesse());
        }
        saveWordlToXml(world, username);
        return true;
    }

    public boolean updateManager(String username, PallierType newmanager) {
        World world = getWorld(username);
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) 
            return false;
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null)
            return false;
        manager.setUnlocked(true);
        double money = world.getMoney();
        world.setMoney(money-manager.getSeuil());
        saveWordlToXml(world, username);
        return true;
    }

}

