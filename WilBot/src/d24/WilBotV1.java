package d24;

import robocode.HitByBulletEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * @author Wilson Wu
 */
public class WilBotV1 extends Robot {
    @Override
    public void run() {
        setBodyColor(Color.LIGHT_GRAY);
        setGunColor(Color.DARK_GRAY);
        setRadarColor(Color.GREEN);
        setScanColor(Color.BLUE);

        while (true) {
            ahead(100);
            turnGunRight(360);
            back(100);
            turnGunLeft(360);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        fire(1);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        turnLeft(90 - event.getBearing());
    }
}
