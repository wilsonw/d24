package d24;

import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * @author Wilson Wu
 */
public class WilBotV2 extends Robot {

    private static final double DISTANCE_TO_WALL = 0.5;
    private boolean peek = false;

    // 0 - north, 1 - east, 2 - south, 3 - west
    private enum Direction {
        NORTH, EAST, SOUTH, WEST
    }

    private Direction direction = Direction.NORTH;
    private double lastGunHeading = 0.0;

    @Override
    public void run() {
        setBodyColor(Color.LIGHT_GRAY);
        setGunColor(Color.DARK_GRAY);
        setRadarColor(Color.GREEN);
        setScanColor(Color.BLUE);

        moveToWall();

        while (true) {
            //move from one side to other side
            ahead(calcMoveAlongWallAmount());
            //re-align the gun
            turnGunLeft(getGunHeading() - getHeading() + 0.5);
            peek = true;
            turnGunRight(181);
            peek = false;
            ahead(calcMoveAlongWallAmount());
            peek = true;
            turnGunLeft(181);
            peek = false;
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        if (getGunHeading() == lastGunHeading) {
            // stationing target, hit harder
            fire(3);
        } else {
            fire(1);
        }
        lastGunHeading = getGunHeading();
        if (peek) {
            scan();
        }
    }

    /*
    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        // If he's in front of us, set back up a bit.
        if (event.getBearing() > -90 && event.getBearing() < 90) {
            back(100);
        } // else he's in back of us, so set ahead a bit.
        else {
            ahead(100);
        }
    }
    */

    @Override
    public void onHitRobot(HitRobotEvent event) {
        double currentGunHeading = getGunHeading();
        peek = true;
        turnGunLeft(event.getBearing() - currentGunHeading);
        peek = false;
        moveToWall();
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        System.out.println("Ouch...");
    }

    private void moveToWall() {
        //move to wall
        turnLeft(getHeading() % 90);
        ahead(calcMoveToWallAmount());
        turnRight(90);
    }

    private double calcMoveToWallAmount() {
        // if something wrong, just anyhow move.
        double moveAmount = 100.0;

        double currentHeading = getHeading();
        System.out.println("Current heading: " + currentHeading);

        if (Math.abs(currentHeading) < 1) {
            System.out.println("0 degree");
            moveAmount = getBattleFieldHeight() - getY() - getHeight() / 2 - DISTANCE_TO_WALL;
            direction = Direction.NORTH;
        } else if (Math.abs(currentHeading - 90) < 1) {
            System.out.println("90 degrees");
            moveAmount = getBattleFieldWidth() - getX() - getHeight() / 2 - DISTANCE_TO_WALL;
            direction = Direction.EAST;
        } else if (Math.abs(currentHeading - 180) < 1) {
            System.out.println("180 degrees");
            moveAmount = getY() - getHeight() / 2 - DISTANCE_TO_WALL;
            direction = Direction.SOUTH;
        } else if (Math.abs(currentHeading - 270) < 1) {
            System.out.println("270 degrees");
            moveAmount = getX() - getHeight() / 2 - DISTANCE_TO_WALL;
            direction = Direction.WEST;
        } else {
            System.out.println("Errr....");
        }

        return moveAmount;
    }

    private double calcMoveAlongWallAmount() {
        switch (direction) {
            case NORTH:
                if (getX() < 120) {
                    return 100;
                } else if (getBattleFieldWidth() - getX() < 120) {
                    return -100;
                } else {
                    return (Math.random() - 0.5) * 200.0;
                }
            case EAST:
                if (getY() < 120) {
                    return -100;
                } else if (getBattleFieldHeight() - getY() < 120) {
                    return 100;
                } else {
                    return (Math.random() - 0.5) * 200.0;
                }
            case SOUTH:
                if (getX() < 120) {
                    return -100;
                } else if (getBattleFieldWidth() - getX() < 120) {
                    return 100;
                } else {
                    return (Math.random() - 0.5) * 200.0;
                }
            case WEST:
                if (getY() < 120) {
                    return 100;
                } else if (getBattleFieldHeight() - getY() < 120) {
                    return -100;
                } else {
                    return (Math.random() - 0.5) * 200.0;
                }
            default:
                System.out.println("WTF!");
                return 0.0;
        }
    }
}
