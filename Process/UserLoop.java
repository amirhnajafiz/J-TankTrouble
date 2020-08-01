package game.Process;

import game.Server.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

/**
 * This class is for the user game loop
 * where we send the current user state
 * and we will get the other users
 * and will show the output.
 */
public class UserLoop extends Thread{

    private User thisPlayerUser;
    private GameFrame canvas;
    private Vector<User> users;
    private Socket clientSocket;

    /**
     * The main constructor of the UserLoop class.
     * @param user the current user
     */
    public UserLoop(User user) {
        thisPlayerUser = user;
        try {
            user.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientSocket = user.getClientSocket();
        // The output frame
        canvas = new GameFrame("Jtank");
        canvas.setGameMap(user.getGameMap());
        canvas.setVisible(true);
        canvas.initBufferStrategy();
        user.getState().setLimits(canvas.getGameMap().getNumberOfRows(), canvas.getGameMap().getNumberOfColumns());
        user.getState().width = canvas.getImage().getWidth() / 8; // Setting the width and the height
        user.getState().height = canvas.getImage().getHeight() / 8;
    }

    @Override
    public void run() {
        // The game loop
        while (!thisPlayerUser.getState().gameOver) {
            write(thisPlayerUser.getState());
            canvas.setBullets((ArrayList<Bullet>) read());
            users = (Vector<User>) read();
            for (User u : users)
                if (u.getUserName().equals(thisPlayerUser.getUserName()))
                    thisPlayerUser.setState(u.getState());
            canvas.addKeyListener(thisPlayerUser.getState().getKeyListener());
            canvas.addMouseListener(thisPlayerUser.getState().getMouseListener());
            canvas.addMouseMotionListener(thisPlayerUser.getState().getMouseMotionListener());
            canvas.render(users);
        }
    }

    public Object read() {
        try {
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            return in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void write(Object object) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}