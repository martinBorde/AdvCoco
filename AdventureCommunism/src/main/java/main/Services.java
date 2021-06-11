package main;

import generated.World;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import javax.xml.bind.*;



public class Services {
    World readWorldFromXml(){
        InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
        World world = new World();
        try {
            JAXBContext jc = JAXBContext.newInstance(World.class);
            Unmarshaller u = jc.createUnmarshaller();
            world = (World) u.unmarshal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return world;
    }
        void saveWordlToXml(World world){
            try {
                OutputStream output = new FileOutputStream("world.xml");
                JAXBContext jc = JAXBContext.newInstance(World.class);
                Marshaller u = jc.createMarshaller();
                u.marshal(world, output);
            } catch (Exception e) {
                e.printStackTrace();
            }

    }
    World getWorld() {
        World world  = new World();
        saveWordlToXml(world);
        return world;
    }

}
