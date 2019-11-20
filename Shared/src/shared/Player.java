package shared;

public class Player {
    public String id;
    public String name;
    boolean hasBall;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        if (hasBall)
            return name + "*";
        else return name;
    }
}
