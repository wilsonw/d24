package d24;

import robocode.ScannedRobotEvent;

public class EnemyBot implements Comparable<EnemyBot>{
		
	protected String name;
	protected double bearing;
	protected double distance;
	protected double energy;
	protected double heading;
	protected double velocity;
	protected double weight;
	
	public EnemyBot() {
		reset();
	}
	
	public void update(ScannedRobotEvent evt) {
		name = evt.getName(); 
		bearing = evt.getBearing();
		distance = evt.getDistance();
		energy = evt.getEnergy();
		heading = evt.getHeading();
		velocity = evt.getVelocity();
		weight = BotUtil.getWeight(this); 
	}
	
	public void reset() {
		name = ""; 
		bearing = 0.0;
		distance = 0.0;
		energy = 0.0;
		heading = 0.0;
		velocity = 0.0;
		weight = 0.0;
	}
	
	public boolean none() {
		if("".equals(name)) {
			return true;
		} else {
			return false;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}
	
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public int compareTo(EnemyBot o) {
		return (this.weight < o.weight ) ? -1: (this.weight > o.weight ) ? 1:0 ;
	}
	
	
}
