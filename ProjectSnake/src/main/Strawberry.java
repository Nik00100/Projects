package main;

import main.engine.Color;
import main.engine.Game;

public class Strawberry extends GameObject {

    private static final String APPLE_SIGN = "\uD83C\uDF53";

    public boolean isAlive = true;

    public Strawberry(int x, int y) {
        super(x, y);
    }

    public void draw(Game game) {
        game.setCellValueEx(x, y, Color.NONE, APPLE_SIGN, Color.GREEN, 75);
    }
}
