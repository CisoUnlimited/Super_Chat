/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dam.super_chat;

/**
 *
 * @author Propietario
 */
public class User {
    
    private final String userName;
    private final String password;

    public User(String userName) {
        this.userName = userName;
        this.password = "";
    }

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User{" + "userName=" + userName + ", password=" + password + '}';
    }
    
}
