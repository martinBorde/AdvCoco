package main;

import generated.PallierType;
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
        try {
            input = new FileInputStream(username+"-world.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (input==null) {
            input = getClass().getClassLoader().getResourceAsStream("world.xml");
        }
        try {
            JAXBContext jc = JAXBContext.newInstance(World.class);
            Unmarshaller u = jc.createUnmarshaller();
            world = (World) u.unmarshal(input);
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
        World world = new World();
        saveWordlToXml(world, username);
        return world;
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

