package model;

import controller.EventListener;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Model {
    public static final int FIELD_CELL_SIZE = 20;

    private GameObjects gameObjects;
    private int currentLevel = 1;
    private EventListener eventListener;
    LevelLoader levelLoader;

    public Model() {
        try {
            Path levels = Paths.get(getClass().getResource("../res/levels.txt").toURI());
            levelLoader = new LevelLoader(levels);
        } catch (URISyntaxException e) {e.printStackTrace();}
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void move(Direction direction) {
        Player player = gameObjects.getPlayer();

        if (checkWallCollision(player,direction)) {
            return;
        }
        if (checkBoxCollisionAndMoveIfAvailable(direction)) {
            return;
        }

        int dx = direction==Direction.LEFT ? -FIELD_CELL_SIZE : (direction==Direction.RIGHT ? FIELD_CELL_SIZE : 0);
        int dy = direction==Direction.UP ? -FIELD_CELL_SIZE : (direction==Direction.DOWN ? FIELD_CELL_SIZE : 0);
        player.move(dx,dy);

        checkCompletion();
    }

    public boolean checkWallCollision(CollisionObject gameObject, Direction direction) {
        Set<Wall> walls = gameObjects.getWalls();
        for(Wall wall : walls) {
            if (gameObject.isCollision(wall,direction)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkBoxCollisionAndMoveIfAvailable(Direction direction) {
        Player player = gameObjects.getPlayer();
        Set<Box> boxes = gameObjects.getBoxes();

        for (Box box : boxes) {
            if (player.isCollision(box,direction)) {
                for (Box item : boxes) {
                    if (!box.equals(item)) {
                        if (box.isCollision(item,direction)) {
                            return true;
                        }
                    }
                    if (checkWallCollision(box, direction)) {
                        return true;
                    }
                }
                int dx = direction==Direction.LEFT ? -FIELD_CELL_SIZE : (direction==Direction.RIGHT ? FIELD_CELL_SIZE : 0);
                int dy = direction==Direction.UP ? -FIELD_CELL_SIZE : (direction==Direction.DOWN ? FIELD_CELL_SIZE : 0);
                box.move(dx,dy);
            }
        }
        return false;
    }

    public void checkCompletion() {
        int numberOfHomes = gameObjects.getHomes().size();
        int numberOfHomesWithBoxes = 0;

        Set<Box> boxes = gameObjects.getBoxes();
        Set<Home> homes = gameObjects.getHomes();

        for(Box box : boxes) {
            for (Home home : homes){
                if (box.getX()==home.getX() && box.getY()==home.getY())
                    numberOfHomesWithBoxes++;
            }
        }

        if (numberOfHomes==numberOfHomesWithBoxes) {
            eventListener.levelCompleted(currentLevel);
        }
    }


    public void restart() {
        restartLevel(currentLevel);
    }

    public void startNextLevel() {
        currentLevel++;
        restartLevel(currentLevel);
    }

    public void restartLevel(int level) {
        gameObjects = levelLoader.getLevel(level);
    }

    public GameObjects getGameObjects() {
        return gameObjects;
    }
}
