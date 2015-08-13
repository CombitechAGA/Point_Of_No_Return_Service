/**
 * Created by Fredrik on 2015-08-06.
 */
public class PointOfNoReturnInfo {


    private double distanceLeft = Double.MAX_VALUE;
    private int margin;
    private double currentLat = Double.MAX_VALUE;
    private double currentLong = Double.MAX_VALUE;
    private double homeLat;
    private double homeLong;
    private boolean currentlyError = false;


    public PointOfNoReturnInfo(int margin,double homeLat,double homeLong ){
        this.homeLat=homeLat;
        this.homeLong=homeLong;
        this.margin=margin;
    }

    public void setCurrentLong(double currentLong) {
        this.currentLong = currentLong;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public void setHomeLong(double homeLong) {
        this.homeLong = homeLong;
    }

    public void setHomeLat(double homeLat) {
        this.homeLat = homeLat;
    }

    public void setDistanceLeft(double distanceLeft) {
        this.distanceLeft = distanceLeft;
    }


    public double distanceFromHome(double homeLat, double homeLong,double currentLat,double currentLong) {
        int R = 6371;
        double a =
                0.5 - Math.cos((currentLat - homeLat) * Math.PI / 180)/2 +
                        Math.cos(homeLat * Math.PI / 180) * Math.cos(currentLat * Math.PI / 180) *
                                (1 - Math.cos((currentLong - homeLong) * Math.PI / 180))/2;

        return R * 2 * Math.asin(Math.sqrt(a));
    }

    public boolean checkForPointOfNoReturn() {
        if(currentLat !=Double.MAX_VALUE && currentLong!=Double.MAX_VALUE && distanceLeft != Double.MAX_VALUE){
            double distanceFromHome = distanceFromHome(homeLat,homeLong,currentLat,currentLong);
            if(distanceFromHome+margin>distanceLeft){
                if(!currentlyError){
                    currentlyError = true;
                    return true;
                }
            }
            else if(distanceLeft>distanceFromHome+margin+5){
                currentlyError=false;
            }
        }
        return false;

    }


//    public void setFuel(float lastFuel,long lastFuelMessageTimestamp){
//        this.lastFuel=lastFuel;
//    }
//    public void setSpeed(float lastSpeed,long lastSpeedMessageTimestamp){
//        this.lastSpeed=lastSpeed;
//        this.lastSpeedMessageTimestamp=lastSpeedMessageTimestamp;
//    }
//    public boolean isThereNewError(){
//        if ((lastFuel<20 && lastSpeed>40  )  &&  Math.abs(lastFuelMessageTimestamp-lastSpeedMessageTimestamp)<1000){
//            System.out.println("isThereNewError " );
//            if(!currentlyError){
//                return false;
//            }
//            else {
//                currentlyError = false;
//                return true;
//            }
////            currentlyError = true;
////            return currentlyError;
//
//        }
//        else{
//            currentlyError = true;
//            return false;
//        }
//    }
}
