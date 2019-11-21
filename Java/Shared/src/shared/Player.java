package shared;

public class Player {
    public int id;
    public String name;
    boolean hasBall;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;

        return id == ((Player) obj).id;
    }
}
