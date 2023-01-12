package it.unical.mat.progettoweb2022;

import it.unical.mat.progettoweb2022.model.Ad;
import it.unical.mat.progettoweb2022.model.Image;
import it.unical.mat.progettoweb2022.model.Property;
import it.unical.mat.progettoweb2022.model.User;
import it.unical.mat.progettoweb2022.persistenza.DBManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.*;
import java.io.*;
import java.util.List;

@SpringBootTest
class ProgettoWeb2022ApplicationTests extends JFrame{

    @Test
    void userDAOworks() {
        //FINDALL
        List<User> lista = DBManager.getInstance().getUserDao().findAll();
        for(User u : lista){
            System.out.println(u.toString());
        }

        //FINDBYPRIMARYKEY
        User user = DBManager.getInstance().getUserDao().findByPrimaryKey("calzino22");
        System.out.println(user.toString());

        //UPDATEorSAVE
        User user2 = new User();
        user2.setName("Matteo");
        user2.setLastname("Fusaro");
        user2.setAge(23);
        user2.setNickname("matt320");
        user2.setPassword("password");
        user2.setEmail("email");
        user2.setRole("admin");
        user2.setBanned(false);

        DBManager.getInstance().getUserDao().saveOrUpdate(user2);
        System.out.println(user2.toString());
    }

    @Test
    void adDAOworks() {
        //FINDALL
        List<Ad> adList = DBManager.getInstance().getAdDao().findAll();
        for(Ad a : adList){
            System.out.println(a.getId().toString());
            System.out.println(a.getTitle().toString());
        }
    }

    @Test
    void imageDAOworks() throws IOException {
              /*
        byte[] bytes = null;
        JFileChooser filechooser = new JFileChooser();
        int result = filechooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION){
            File file = filechooser.getSelectedFile();
            try {
                bytes = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        for(byte b : bytes){
            System.out.println(b);
        }
        */
        /*
        Image image = new Image();
        image.setAd(1);
        image.setData(bytes);
        */
        Image image = DBManager.getInstance().getImageDao().findByPrimaryKey(1);
        File imageFile = new File("IMAGGINEFICA.jpg");
        try {
            FileOutputStream op = new FileOutputStream(imageFile);
            op.write(image.getData());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void propertyDaoWorks() {
        List<Property> pList = DBManager.getInstance().getPropertyDao().findAll();
        for(Property p : pList) {
            System.out.println(p.getId().toString());
            System.out.println(p.getType().toString());
        }
    }
}
