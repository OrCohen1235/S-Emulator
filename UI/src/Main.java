import menu.Menu;

public class Main {
    public static void main(String[] args) {
        Menu mainMenu = new Menu();
        try {
            mainMenu.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}