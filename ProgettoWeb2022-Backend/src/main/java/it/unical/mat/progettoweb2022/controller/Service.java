package it.unical.mat.progettoweb2022.controller;

import it.unical.mat.progettoweb2022.model.*;
import it.unical.mat.progettoweb2022.persistenza.DAO.*;
import it.unical.mat.progettoweb2022.persistenza.DBManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin("http://localhost:4200/")
public class Service {

    @GetMapping("/ads")
    public List<Ad> getAds(){
        AdDAO dao = DBManager.getInstance().getAdDao();
        return dao.findAll();
    }

    @GetMapping("/ad")
    public Ad getAd(@RequestParam String adId){
        AdDAO dao = DBManager.getInstance().getAdDao();
        Ad ad = dao.findByPrimaryKey(Integer.parseInt(adId));
        System.out.println("ASTA: " + ad.getAuction());
        return ad;
    }
    @GetMapping("/searchAds")
    public List<Ad> getAdsWithFilter(@RequestParam String searchType, @RequestParam String parameter){
        List<Ad> adList = new ArrayList<Ad>();
        AdDAO dao = DBManager.getInstance().getAdDao();
        List<Ad> tempList = dao.findAll();
        if(searchType.equals("Città")) {
            for(Ad ad : tempList){
                if(ad.getCity().contains(parameter) || ad.getCity().equals(parameter))
                    adList.add(ad);
            }
        }else if(searchType.equals("Titolo")){
            for(Ad ad : tempList){
                if(ad.getTitle().contains(parameter))
                    adList.add(ad);
            }
        }else if(searchType.equals("Contenuto")){
            for(Ad ad : tempList){
                if(ad.getTitle().contains(parameter) || ad.getDescription().contains(parameter)){
                    adList.add(ad);
                }
            }
        }else if(searchType.equals("Tipo")){
            PropertyDAO pDao = DBManager.getInstance().getPropertyDao();
            for(Ad ad : tempList){
                Property p = pDao.findByPrimaryKey(ad.getProperty());
                if(p.getType().equals(parameter)) {
                    adList.add(ad);
                }
            }
        }
        return adList;
    }

    @GetMapping("/user")
    public User getNickname(HttpServletRequest req, @RequestParam String parameter, @RequestParam Boolean bySession) {
        User user = null;
        try {
            if (bySession) {
                HttpSession session = (HttpSession) req.getServletContext().getAttribute(parameter);
                user = (User) session.getAttribute("user");
            }else{
            user = DBManager.getInstance().getUserDao().findByPrimaryKey(parameter);
            }
        }catch(Exception e){
            return user;
        }
        return user;
    }

    @PostMapping("/addAd")
    @CrossOrigin("http://localhost:4200/addAd")
    public boolean addAd(HttpServletRequest req, @RequestParam String sessionId, @RequestParam String title,
                         @RequestParam String description, @RequestParam String type, @RequestParam String mq,
                         @RequestParam String latitude, @RequestParam String longitude, @RequestParam String price,
                         @RequestParam String status, @RequestParam String city, @RequestParam String isAuction,
                         @RequestBody byte[] media){

        HttpSession session = (HttpSession) req.getServletContext().getAttribute(sessionId);
        User user = (User) session.getAttribute("user");

        try {
            Property property = new Property();
            property.setType(type);
            property.setMq(Double.parseDouble(mq));
            property.setLatitude(latitude);
            property.setLongitude(longitude);
            property.setUser(user.getNickname());

            DBManager.getInstance().getPropertyDao().saveOrUpdate(property);

            Ad ad = new Ad();
            ad.setTitle(title);
            ad.setDescription(description);
            ad.setUser(user);
            ad.setProperty(property.getId());
            ad.setPrice(Double.parseDouble(price));
            ad.setMq(Double.parseDouble(mq));
            ad.setAuction(isAuction);
            ad.setCity(city);
            if (status.equals("Affitto"))
                ad.setStatus("affittasi");
            else {
                ad.setStatus("vendesi");
            }

            DBManager.getInstance().getAdDao().saveOrUpdate(ad);

            ImageDAO imageDAO = DBManager.getInstance().getImageDao();
            Image img = new Image();
            img.setData(media);
            img.setAd(ad.getId());
            imageDAO.saveOrUpdate(img);

            property.setAd(ad.getId());
            DBManager.getInstance().getPropertyDao().saveOrUpdate(property);

            if(isAuction.equals("true")){
                AuctionDAO aDao = DBManager.getInstance().getAuctionDao();
                Auction auction = new Auction();
                auction.setAd_id(ad.getId());
                auction.setCurrentPrice(Integer.parseInt(price));
                auction.setNumOfferte(0);
                Integer days = 7;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(calendar.getTime());
                calendar.add(Calendar.DAY_OF_YEAR, days);
                Date date = calendar.getTime();
                Timestamp ts = new Timestamp(date.getTime());
                System.out.println(ts);
                auction.setEndTime(ts.toString());
                aDao.saveOrUpdate(auction);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @GetMapping("/getImage")
    public List<byte[]> getImage(HttpServletRequest request, @RequestParam String adId){
        List<Image> imageList = DBManager.getInstance().getImageDao().findByAdId(Integer.parseInt(adId));
        List<byte[]> imgList = new ArrayList<>();
        for (Image image : imageList) {
            imgList.add(image.getData());
        }
        return imgList;
    }

    @GetMapping("/newReview")
    @CrossOrigin("http://localhost:4200")
    public boolean addReview(HttpServletRequest req, @RequestParam String title, @RequestParam String description,
                              @RequestParam Integer rating, @RequestParam String user, @RequestParam Integer ad){

        System.out.println(user);
        Review review = new Review();
        review.setTitle(title);
        review.setDescription(description);
        review.setVote(rating);
        try{
            review.setUser(DBManager.getInstance().getUserDao().findByPrimaryKey(user));
            review.setAd(DBManager.getInstance().getAdDao().findByPrimaryKey(ad));
            ReviewDAO dao = DBManager.getInstance().getReviewDao();
            dao.saveOrUpdate(review);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    @GetMapping("/sendEmail")
    public Boolean sendEmail(@RequestParam String title, @RequestParam Integer adId,
                             @RequestParam String text, @RequestParam String emailAnnuncio,
                             @RequestParam String emailMittente){
        String oggetto="# "+adId+": "+title;
        String to = emailAnnuncio;
        String from = emailMittente;
        String host = "smtp.gmail.com";
        String messaggio=text+"\n"+"Inviato da: "+from;


        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("assistenzaprojectjava@gmail.com", "rjgwpjzbmsuqribl");
            }});

        // Used to debug SMTP issues
        session.setDebug(true);
        try {
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            // Set Subject: header field
            message.setSubject(oggetto);
            // Now set the actual message
            message.setText(messaggio);

            System.out.println("sending...");
            Transport.send(message);
            System.out.println("Sent message successfully....");
            return true;
        } catch (MessagingException me) {
            me.printStackTrace();
        }
        System.out.println("ESCO CON false");
        return false;

    }

    @GetMapping("/getReviews")
    public List<Review> getReviews(@RequestParam Integer adId){
        ReviewDAO dao = DBManager.getInstance().getReviewDao();
        return dao.findByAdId(adId);
    }

    @GetMapping("/property")
    public Property getProperty(@RequestParam Integer id){
        PropertyDAO dao = DBManager.getInstance().getPropertyDao();
        return dao.findByPrimaryKey(id);
    }

    @GetMapping("/removeReview")
    public boolean removeReview(@RequestParam Integer reviewId){
        ReviewDAO dao = DBManager.getInstance().getReviewDao();
        return dao.delete(reviewId);
    }

    @GetMapping("/banUser")
    public boolean banUser(@RequestParam String nickname){
        UserDAO dao = DBManager.getInstance().getUserDao();
        User user = dao.findByPrimaryKey(nickname);
        user.setBanned(true);
        try {
            dao.saveOrUpdate(user);
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    @GetMapping("/updateUser")
    public boolean updateUser(@RequestParam String nickname, @RequestParam String name,
        @RequestParam String lastname,@RequestParam String telephone, @RequestParam String email,
        @RequestParam String state, @RequestParam String country, @RequestParam String address,
        @RequestParam String postalCode, @RequestParam String password){

        try {
            UserDAO dao = DBManager.getInstance().getUserDao();
            User user = dao.findByPrimaryKey(nickname);
            user.setName(name);
            user.setLastname(lastname);
            user.setTelephone(telephone);
            user.setEmail(email);
            user.setState(state);
            user.setCountry(country);
            user.setAddress(address);
            user.setPostalCode(postalCode);
            if(password.length() > 8){
                user.setPassword(password);
            }

            dao.saveOrUpdate(user);
        }catch(Exception e){
            return false;
        }
        return true;
    }

    @GetMapping("/getAuction")
    public Auction getAuction(@RequestParam Integer adId){
        AuctionDAO dao = DBManager.getInstance().getAuctionDao();
        Auction auction = dao.findByAd(adId);
        return auction;
    }

    @GetMapping("/putOffer")
    public Boolean prendiOfferta(Integer id, String offerta) {
        AuctionDAO aDao = DBManager.getInstance().getAuctionDao();
        try{
            Auction auction = aDao.findByPrimaryKey(id);
            auction.setCurrentPrice(Integer.parseInt(offerta));
            aDao.saveOrUpdate(auction);
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @GetMapping("/deleteAd")
    public Boolean deleteAd(@RequestParam Integer adId){
        PropertyDAO pDao = DBManager.getInstance().getPropertyDao();
        pDao.deleteByAd(adId);
        AdDAO dao = DBManager.getInstance().getAdDao();
        Ad ad = dao.findByPrimaryKey(adId);
        return dao.delete(ad);
    }

    @GetMapping("/editAd")
    public Boolean editAd(@RequestParam Integer adId, @RequestParam String title, @RequestParam String description,
                          @RequestParam String price){
        AdDAO dao = DBManager.getInstance().getAdDao();
        Ad ad = dao.findByPrimaryKey(adId);

        ad.setTitle(title);
        ad.setDescription(description);
        ad.setPrice(Double.parseDouble(price));

        return dao.saveOrUpdate(ad);
    }

    @GetMapping("/makeAdmin")
    public Boolean makeAdmin(@RequestParam String nickname ){
        UserDAO dao = DBManager.getInstance().getUserDao();

        try {
            User user = dao.findByPrimaryKey(nickname);
            user.setRole("admin");
            dao.saveOrUpdate(user);
        }
        catch (Exception e ) {
            return false;
        }
        return true;
    }

    @GetMapping("/sendSupport")
    public Boolean sendSupport(@RequestParam String name, @RequestParam String email, @RequestParam String message,
                               @RequestParam String subject){

        String oggetto= subject;
        String to = "matteofusaro1@gmail.com";
        String from = email;
        String host = "smtp.gmail.com";
        String messaggio= name + " scrive: \n" + message+"\n\n\n"+" Contatto: " + from;

        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("assistenzaprojectjava@gmail.com", "rjgwpjzbmsuqribl");
            }});

        // Used to debug SMTP issues
        session.setDebug(false);
        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            // Set From: header field of the header.
            mimeMessage.setFrom(new InternetAddress(from));
            // Set To: header field of the header.
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            // Set Subject: header field
            mimeMessage.setSubject(oggetto);
            // Now set the actual message
            mimeMessage.setText(messaggio);

            Transport.send(mimeMessage);
            return true;
        } catch (MessagingException me) {
            return false;
        }
    }
}
