package d24;

import robocode.*;
import robocode.Robot;

import java.awt.*;

/**
 * @author Wilson Wu
 */
public class WilBotV1 extends Robot {

    private static final double DISTANCE_TO_WALL = 0.5;
    private boolean peek = false;

    @Override
    public void run() {
        setBodyColor(Color.LIGHT_GRAY);
        setGunColor(Color.DARK_GRAY);
        setRadarColor(Color.GREEN);
        setScanColor(Color.BLUE);

        turnLeft(getHeading() % 90);

        peek = true;
        turnGunRight(90);
        turnRight(90);

        while (true) {
            peek = true;
            ahead(calcMoveAmount());
            peek = false;
            turnRight(90);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        fire(1);
        if (peek) {
            scan();
        }
    }

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

    @Override
    public void onHitRobot(HitRobotEvent event) {
        double currentGunHeading = getGunHeading();
        peek = true;
        turnGunLeft(event.getBearing() - currentGunHeading);
        peek = false;
        turnGunRight(event.getBearing() - currentGunHeading);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        System.out.println("Ouch...");
    }

    private double calcMoveAmount() {
        // if something wrong, just anyhow move.
        double moveAmount = 100.0;

        double currentHeading = getHeading();
        System.out.println("Current heading: " + currentHeading);

        if (Math.abs(currentHeading) < 1) {
            System.out.println("0 degree");
            moveAmount = getBattleFieldHeight() - getY() - getHeight() / 2 - DISTANCE_TO_WALL;
        } else if (Math.abs(currentHeading - 90) < 1) {
            System.out.println("90 degrees");
            moveAmount = getBattleFieldWidth() - getX() - getHeight() / 2 - DISTANCE_TO_WALL;
        } else if (Math.abs(currentHeading - 180) < 1) {
            System.out.println("180 degrees");
            moveAmount = getY() - getHeight() / 2 - DISTANCE_TO_WALL;
        } else if (Math.abs(currentHeading - 270) < 1) {
            System.out.println("270 degrees");
            moveAmount = getX() - getHeight() / 2 - DISTANCE_TO_WALL;
        } else {
            System.out.println("Errr....");
        }

        return moveAmount;
    }
}
