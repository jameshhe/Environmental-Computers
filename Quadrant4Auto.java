import rxtxrobot.*;

public class Quadrant4Auto {
    static RXTXRobot r = new ArduinoNano();
    
    public static void main(String[] args) {
                r = new ArduinoUno();
		r.setPort("/dev/tty.usbmodem14301"); 
		r.connect();
                
                //first task: remain stationary in slope
                r.sleep(5000); //not suren for how long but five seconds maybe?
                
                //second task: drive down the slope
                r.runTwoPCAMotor(14, 235, 15, -200, 2500); //i think this would be long enough to get down but need to calibrate
                
                //second task: naviagte to hotspot and deploy temperature probe
                
                //rotate to face beacon
                FindBeacon('x');
                
                //drive to hot spot
                //could use bump sensore loop
                r.runTwoPCAMotor(14, 235, 15, -200, 4500); //calibrate for distance to hot spot
                
                //deploy temperature probe
                //unsure of how much servo needs to rotate
                r.runPCAServo(0, 90);
                
                //third task: get temperature reading 
                        int thermistorReading = getThermistorReading();
                        System.out.println("The probe read the value: " + thermistorReading);
                        System.out.println("In volts: " + (thermistorReading * (5.0/1023.0)));
                        double celsius = (thermistorReading - 677.7186471)/-6.93063423; 
                        System.out.printf("In Degrees Celsius: %.2f\n", celsius);
                //withdraw probe
                r.runPCAServo(0, 90);
                
                //fifth task: navigate to bridge
                FindBeacon('x');
                
                //drive to beacon
                r.runTwoPCAMotor(14, 235, 15, -200, 4500); //calibrate for distance to beacon
                
                //sixth task: cross bridge and return home
                //based on map should rotate about 135º
                RotateRobotPositive(1988);
                r.runTwoPCAMotor(14, 235, 15, -200, 4500); //calibrate for distance to home
                
                System.exit(0);
    }
    
    public static void RotateRobotPositive(int time) {
	    r.runTwoPCAMotor(14, 180, 15, 180, time); //rotates the robot 90 degrees when battery is at 13.0 V //1325 for 90
	    //motor strength 180, channels 0 and 1, time: .8 seconds
	}
	    
    public static void RotateRobotNegative(int time) {
	    r.runTwoPCAMotor(14, -150, 15, -150, time); //rotates the robot approximately -90 degrees when battery is at 13.0 V //850 for 90
	    //motor strength 150, channels 0 and 1, time: .8 seconds
	}
    public static int getThermistorReading() {
        int sum = 0;
        int readingCount = 10;
        
        for (int i = 0; i < readingCount; i++) {
            r.refreshAnalogPins();
            int reading = r.getAnalogPin(0).getValue();
            sum += reading;
        }
        return sum / readingCount;
    }
    public static void FindBeacon (char beaconChar) {
        int[] angles = {0, 0, 0}; //array of angles detected
                char beaconInput = '0';
                char beaconOutput = beaconChar; //character sent out by beacon CHANGE THIS!!!
                int avgAngle = 0;
                int rotationAngle = 0;
                int timeAngle = 0; //time based on rotaitonAngle
                
                for(int i = 30; i <= 180; i+=10) {
                    r.runPCAServo(0, i);
                    for (int j = 0; j < 3; j ++) { //try to get angle five times to avoid random ir signals
                            beaconInput = r.getIRChar(); //the code received from the beacon
			if(beaconOutput == beaconInput) {
                            angles[j] = i;
			}
                }
                            if (angles[0] == angles[1] && angles[1] == angles[2] && beaconInput != '0') { //if all the angles are the same it updates the avgAngle
                            avgAngle = i;
                                if (angles[0] == 0) {
                                    break;
                                }
                            }
                            
                }
                System.out.println(avgAngle);
                rotationAngle = 90 - avgAngle; //change rotation based on the way the robot is facing 
                r.runPCAServo(0, 90); //make the servo point forwards
                r.sleep(400);
                if (rotationAngle == 0) { //if the beacon is right in front of the robot it will just go forwards
                    r.runPCAServo(0, 90);
                    r.sleep(300);
                }
                if (rotationAngle > 0) {
                    timeAngle = ((2000)/(90/rotationAngle)); //change the time function adjusted on the angle
                    r.runPCAServo(0, rotationAngle); //point servo towards beacon
                    r.sleep(2000);
                    RotateRobotPositive(timeAngle);
                    r.sleep(2000);
                }
                if (rotationAngle < 0) {
                    rotationAngle = rotationAngle * -1; //this value will be negative so multiplied by -1
                    timeAngle = ((850)/(120/rotationAngle)); //dividing by 120 because of results needing more rotation
                    System.out.println(rotationAngle);
                    System.out.print(timeAngle);
                    r.sleep(2000);
                    RotateRobotNegative(timeAngle);
                    r.sleep(2000);
                }
                r.runPCAServo(0, 90);
    }
}