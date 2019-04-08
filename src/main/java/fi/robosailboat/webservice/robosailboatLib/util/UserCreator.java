package fi.robosailboat.webservice.robosailboatLib.util;

import fi.robosailboat.webservice.web.dto.User;
import fi.robosailboat.webservice.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


public class UserCreator {

    private static String USERNAME = "test2";
    private static String PASSWORD = "test2";
    private static BCryptPasswordEncoder encoder =  new BCryptPasswordEncoder();
    @Autowired
    static
    UserRepository userRepository;





    public static void createUser(){
        User user = new User();
        user.setUserName(USERNAME);
        user.setPassword(encoder.encode(PASSWORD));
        System.out.println("New User Initialized | UserName: " + user.getUserName() + " | password: "+ user.getPassword());

        userRepository.insert(user);
    }

}
