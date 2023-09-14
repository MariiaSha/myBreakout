package com.shpp.p2p.cs.mshatalova.assignment4;

import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Breakout extends WindowProgram {
    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /* The amount of time to pause between frames (60fps). */
    private static final double PAUSE_TIME = 1000.0 / 60;

    /**
     * Dimensions of the paddle
     */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /**
     * Offset of the paddle up from the bottom
     */
    private static final int PADDLE_Y_OFFSET = 30;

    /**
     * Number of bricks per row
     */
    private static final int NBRICKS_PER_ROW = 10;

    /**
     * Number of rows of bricks
     */
    private static final int NBRICK_ROWS = 10;

    /**
     * Separation between bricks
     */
    private static final int BRICK_SEP = 4;

    /**
     * Height of a brick
     */
    private static final int BRICK_HEIGHT = 8;

    /**
     * Radius of the ball in pixels
     */
    private static final int BALL_RADIUS = 10;

    /**
     * Offset of the top brick row from the top
     */
    private static final int BRICK_Y_OFFSET = 70;

    /**
     * Amount of colors for bricks rows
     */
    private static final int COLORS_OF_ROWS = 5;

    /**
     * Number of turns
     */
    private static final int NTURNS = 3;

    /**
     * Colors of the bricks rows
     */
    private static final Color[] colors = {
            Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN
    };

    /**
     * Start speed
     */
    private final static double START_SPEED = 5;

    /**
     * Speed dispersion
     */
    private final static double START_SPEED_DISPERSION = 1;

    /**
     * Tells if collision was on side of circle
     */
    private boolean isSide = false;
    /**
     * The object for dragging.
     */
    private GRect paddle;

    /**
     * The object which moves and search objects for collision.
     */
    private GOval circle;

    /**
     * The object which have information about amount of player's lives.
     */
    private GLabel liveMessage;

    public void run() {
        addObjects();
        addMouseListeners();
        startGame();
    }

    /**
     * Create and add objects as paddle, circle and bricks in the frame
     */
    private void addObjects() {
        paddle = createPaddle();
        add(paddle);
        circle = createCircle();
        add(circle);
        createBricks();
    }

    /**
     * Decrease amount of lives, set circle location in the centre of frame,
     * remove message about amount of lives and if lives is then start new cycle of game,
     * else add message about defeat
     */
    private void startGame() {
        int nBricksInMatrix = NBRICKS_PER_ROW * NBRICK_ROWS;
        int lives = NTURNS; // Total number of lives at the game start
        while (lives > 0 && nBricksInMatrix > 0) {
            newGamePrepare(lives);
            nBricksInMatrix = moveCircle(nBricksInMatrix);
            lives--;
            remove(liveMessage);
        }
        if (lives <= 0) {
            finishMessage(false);
        }
    }

    /**
     * Add message about amount of lives, wait for click and start game cycle
     */
    private void newGamePrepare(int lives) {
        circle.setLocation((double) getWidth() / 2 - BALL_RADIUS,
                (double) getHeight() / 2 - BALL_RADIUS);
        liveMessage = gameMessage(lives);
        add(liveMessage);
        waitForClick();
    }

    /**
     * Create matrix of rectangles with given parameters. Centre them and change color
     */
    private void createBricks() {
        int brickWidth = (getWidth() - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;
        double matrixWidth = NBRICKS_PER_ROW * brickWidth + (NBRICKS_PER_ROW - 1) * BRICK_SEP;
        int colorNumber = 0;

        for (int i = 1; i <= NBRICK_ROWS; i++) {
            double x = (double) getWidth() / 2 - matrixWidth / 2;
            for (int j = 0; j < NBRICKS_PER_ROW; j++) {
                addBrick(x, i, brickWidth, colorNumber);
                x = x + brickWidth + BRICK_SEP;
            }

            if ((i % 2 == 0))
                colorNumber++;
            if (colorNumber >= COLORS_OF_ROWS)
                colorNumber = 0;
        }
    }

    /**
     * Add rectangle with given parameters. Centre it and set color
     *
     * @param x           This parameter represents the x offset of the brick
     * @param i           This parameter represents the number of brick in the row
     * @param brickWidth  This parameter represents the width of brick
     * @param colorNumber This parameter represents the number of color
     */
    private void addBrick(double x, int i, int brickWidth, int colorNumber) {
        GRect rectangle = new GRect(x,
                BRICK_Y_OFFSET + i * (BRICK_HEIGHT + BRICK_SEP),
                brickWidth, BRICK_HEIGHT);
        rectangle.setFilled(true);
        rectangle.setColor(colors[colorNumber]);
        add(rectangle);
    }


    /**
     * Set coordinates for moving, start cycle of moving the circle:
     * checking collisions on each step with removing them if it is the brick
     * and changing the trajectory of circle;s moving,
     * while bricks exist
     *
     * @param nBricksInMatrix This parameter represents the amount of bricks
     * @return the amount of bricks
     */
    private int moveCircle(int nBricksInMatrix) {
        /* The downward velocity of the circle */
        double vy = START_SPEED;
        /* The horizontal velocity of the circle */
        double vx = generateX();
        // while bricks exist
        while (nBricksInMatrix > 0) {
            circle.move(vx, vy);
            pause(PAUSE_TIME);
            // if circle fell down of frame
            if ((circle.getY() + BALL_RADIUS * 2) > getHeight())
                break;

            // get collisions and check is it paddle or brick
            GObject collider = getCollidingObject();
            if (collider == paddle) {
                vy = -vy;
            } else if (collider == liveMessage) {
                continue;
            } else if (collider != null) {
                remove(collider);
                if (isSide) {
                    vx = -vx;
                    isSide = false;
                }
                vy = -vy;
                nBricksInMatrix--;
            }

            // check collision with walls and top
            vx = checkCollisionWithWalls(vx);
            vy = checkCollisionWithTop(vy);
        }
        // if bricks does not exist call method to print victory
        isWin(nBricksInMatrix);
        return nBricksInMatrix;
    }

    /**
     * Check if amount of bricks is 0 or less and call method for finish
     *
     * @param nBricksInMatrix This parameter represents the amount of bricks
     */
    private void isWin(int nBricksInMatrix) {
        if (nBricksInMatrix <= 0) {
            finishMessage(true);
        }
    }

    /**
     * Generate the horizontal velocity of the circle
     *
     * @return the horizontal velocity of the circle
     */
    private double generateX() {
        RandomGenerator rgen = RandomGenerator.getInstance();
        double vx = rgen.nextDouble(START_SPEED - START_SPEED_DISPERSION, START_SPEED + START_SPEED_DISPERSION);
        if (rgen.nextBoolean(0.5))
            vx = -vx;
        return vx;
    }

    /**
     * Check collisions of circle with top of frame
     *
     * @param vy This parameter represents the downward velocity of the circle
     * @return the downward velocity of the circle
     */
    private double checkCollisionWithTop(double vy) {
        // check top of the circle in frame
        if ((circle.getY()) <= 0) {
            return -vy;
        }
        return vy;
    }

    /**
     * Check collisions of circle with walls
     *
     * @param vx This parameter represents the horizontal velocity of the circle
     * @return the horizontal velocity of the circle
     */
    private double checkCollisionWithWalls(double vx) {
        // check right side of the circle in frame
        if ((circle.getX() + BALL_RADIUS * 2) > getWidth()) {
            return -vx;
        }
        // check left side of the circle in frame
        if ((circle.getX()) <= 0) {
            return -vx;
        }
        return vx;
    }

    /**
     * Create message about amount of lives in game
     *
     * @param lives This parameter represents the amount of lives in game
     * @return GLabel element with amount of lives in game
     */
    private GLabel gameMessage(int lives) {
        GLabel nLives;
        nLives = new GLabel("You have " + lives + " lives left!");
        nLives.setFont("Roboto-24");
        nLives.setLocation((double) getWidth() / 2 - nLives.getWidth() / 2, nLives.getHeight() + nLives.getAscent());
        return nLives;
    }

    /**
     * Add message about status of game after finish
     *
     * @param isVictory This parameter represents the status of game after finish
     */
    private void finishMessage(boolean isVictory) {
        GLabel result;
        if (isVictory) {
            result = new GLabel("VICTORY!");
        } else {
            result = new GLabel("DEFEAT!");
        }

        result.setFont("Roboto-36");
        result.setLocation((double) getWidth() / 2 - result.getWidth() / 2,
                (double) getHeight() / 2 - result.getHeight() / 2);
        add(result);
    }

    /**
     * Check collisions at left, right, top and bottom sides of circle and four corners
     *
     * @return collided GObject element
     */
    private GObject getCollidingObject() {
        if (checkSidesCollide() != null) {
            return checkSidesCollide();
        } else if (getElementAt(circle.getX(), circle.getY()) != null) {
            return getElementAt(circle.getX(), circle.getY());
        } else if (getElementAt(circle.getX() + BALL_RADIUS * 2, circle.getY()) != null) {
            return getElementAt(circle.getX() + BALL_RADIUS * 2, circle.getY());
        } else if (checkMidCollide() != null) {
            return checkMidCollide();
        } else if (getElementAt(circle.getX(), circle.getY() + BALL_RADIUS * 2) != null) {
            return getElementAt(circle.getX(), circle.getY() + BALL_RADIUS * 2);
        } else if (getElementAt(circle.getX() + BALL_RADIUS * 2, circle.getY() + BALL_RADIUS * 2) != null) {
            return getElementAt(circle.getX() + BALL_RADIUS * 2, circle.getY() + BALL_RADIUS * 2);
        }
        return null;
    }

    /**
     * Check collisions at left and right sides of circle
     *
     * @return collided GObject element
     */
    private GObject checkSidesCollide() {
        if (getElementAt(circle.getX() - 2, circle.getY() + BALL_RADIUS) != null) {
            isSide = true; // left
            return getElementAt(circle.getX() - 2, circle.getY() + BALL_RADIUS);
        } else if (getElementAt(circle.getX() + 2 * BALL_RADIUS + 2, circle.getY() + BALL_RADIUS) != null) {
            isSide = true; // right
            return getElementAt(circle.getX() + 2 * BALL_RADIUS + 2, circle.getY() + BALL_RADIUS);
        }
        return null;
    }

    /**
     * Check collisions at top and bottom of circle
     *
     * @return collided GObject element
     */
    private GObject checkMidCollide() {
        // top
        if (getElementAt(circle.getX() + BALL_RADIUS, circle.getY() - 2) != null) {
            return getElementAt(circle.getX() + BALL_RADIUS, circle.getY() - 2);
        } // bottom
        else if (getElementAt(circle.getX() + BALL_RADIUS, circle.getY() + 2 * BALL_RADIUS + 2) != null) {
            return getElementAt(circle.getX() + BALL_RADIUS, circle.getY() + 2 * BALL_RADIUS + 2);
        }
        return null;
    }

    /**
     * Repositions the dragged object to the mouse's location when the mouse
     * is moved.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        /* If there is something to drag at all, go move it. */
        if (paddle != null) {
            double newX = e.getX() - paddle.getWidth() / 2.0;
            if (newX > 0 && newX < getWidth() - PADDLE_WIDTH) {
                paddle.setLocation(newX, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT);
            }
        }
    }

    /**
     * Create new oval with given parameters
     *
     * @return GOval element
     */
    private GOval createCircle() {
        GOval object = new GOval((double) getWidth() / 2 - BALL_RADIUS,
                (double) getHeight() / 2 - BALL_RADIUS,
                BALL_RADIUS * 2, BALL_RADIUS * 2);
        object.setFilled(true);
        object.setColor(Color.BLACK);
        return object;
    }

    /**
     * Create new rectangle with given parameters
     *
     * @return GObject element
     */
    private GRect createPaddle() {
        GRect rectangle = new GRect(0, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);
        rectangle.setFilled(true);
        rectangle.setColor(Color.BLACK);
        return rectangle;
    }
}
