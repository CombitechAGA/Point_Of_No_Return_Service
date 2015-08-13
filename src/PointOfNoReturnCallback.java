import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;

/**
 * Created by Fredrik on 2015-07-06.
 */
public class PointOfNoReturnCallback implements MqttCallback{
    private MqttClient client;
    private HashMap<String, PointOfNoReturnInfo> clientToInfo;


    public PointOfNoReturnCallback(MqttClient client){
        this.client = client;
        clientToInfo = new HashMap<>();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("PointOfNoReturnService lost connection!");
    }


    //nu timestampar vi här, vi borde skicka med timestampet, men då kanske vi måste ändra i databasen, vi kan göra det imorgon
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = mqttMessage.toString();
        System.out.println(message);


       if(topic.equals("telemetry/snapshot")){
           String carID = message.split(";")[0].split("carID:")[1];
           System.out.println(carID);
           if(clientToInfo.containsKey(carID)){
               double longitude = Double.parseDouble(message.split("longitude:")[1].split(";")[0]);
               System.out.println(longitude);
               double latitude = Double.parseDouble(message.split("latitude:")[1]);
               System.out.println(latitude);
               PointOfNoReturnInfo pointOfNoReturnInfo = clientToInfo.get(carID);
               pointOfNoReturnInfo.setCurrentLong(longitude);
               pointOfNoReturnInfo.setCurrentLat(latitude);
               boolean error =pointOfNoReturnInfo.checkForPointOfNoReturn();
               if (error){
                   new PublishThread(client,carID+"/message","You are close to the point of no return, please return to your charging station.").start();
               }
           }
           else{
               System.out.println("Nu ska jag subscribe:a");
               new SubscribeThread(client,carID+"/config").start();
               new PublishThread(client,"request/config",carID).start();
           }
       }
       else if(topic.contains("set/config")) {
           if(message.contains("home")){
               String carID = message.substring(message.indexOf(":")+1,message.indexOf(";"));
               if(clientToInfo.containsKey(carID)){
                   PointOfNoReturnInfo pointOfNoReturnInfo = clientToInfo.get(carID);
                   double latitude = Double.parseDouble(message.split("home:")[1].split(",")[0]);
                   double longitude = Double.parseDouble(message.split(",")[1]);
                   System.out.println("uppdaterar min pointOFNoReturn");
                   System.out.println(latitude);
                   System.out.println(longitude);
                   pointOfNoReturnInfo.setHomeLong(longitude);
                   pointOfNoReturnInfo.setHomeLat(latitude);
               }
           }
       }

       //som för set/config för ändringar i margin också

       else if (topic.contains("/config")){
           System.out.println("Nu kom det en requestad config");
           String carID = topic.split("/config")[0];
           System.out.println("carID: "+carID);
           double latitude = Double.parseDouble(message.split("home#")[1].split(",")[0]);
           System.out.println(latitude);
           double longitude = Double.parseDouble(message.split("home#")[1].split(",")[1].split("\n")[0]);
           System.out.println(longitude);
           int margin = Integer.parseInt(message.split("margin#")[1].split("\n")[0]);
           System.out.println("margin: "+margin);
           PointOfNoReturnInfo pointOfNoReturnInfo = new PointOfNoReturnInfo(margin,latitude,longitude);
           clientToInfo.put(carID,pointOfNoReturnInfo);
           new SubscribeThread(client,carID+"/distancePrediction").start();

       }

       else if(topic.contains("distancePrediction")) {
           double fuelLevel = Double.parseDouble(message);
           double distanceLeft = 50 * (fuelLevel/100);
           String carID = topic.split("/distancePrediction")[0];
           clientToInfo.get(carID).setDistanceLeft(distanceLeft);
           boolean error = clientToInfo.get(carID).checkForPointOfNoReturn();
           if (error){
               new PublishThread(client,carID+"/message","You are close to the point of no return, please return to your charging station.").start();
           }

       }
       else{
           System.out.println("Unknown topic:\""+topic+"\"");
       }




//        System.out.println(mqttMessage.toString());
//        String message = mqttMessage.toString();
//        String[] messageParts =message.split(";");
//        String carID = messageParts[0];
//        Long currentTime = System.currentTimeMillis();
//        if (topic.equals("telemetry/fuel")||topic.equals("telemetry/speed")) {
//            if (topic.equals("telemetry/fuel")) {
//                float fuelLevel = Float.parseFloat(messageParts[1]);
//                if (!clientToInfo.containsKey(carID)) {
//                    clientToInfo.put(carID, new PointOfNoReturnInfo());
//                }
//                clientToInfo.get(carID).setFuel(fuelLevel, currentTime);
//
//            } else if (topic.equals("telemetry/speed")) {
//                float speed = Float.parseFloat(messageParts[1]);
//                if (!clientToInfo.containsKey(carID)) {
//                    clientToInfo.put(carID, new PointOfNoReturnInfo());
//                }
//                clientToInfo.get(carID).setSpeed(speed, currentTime);
//                //boolean error = clientToInfo.get(carID).isThereNewError();
//            }
//            boolean error = clientToInfo.get(carID).isThereNewError();
//            System.out.println("error: "+error);
//            if (error){
//                new PublishThread(client,carID+"/message").start();
//            }
//        }
//
//        else {
//            System.out.println("unkown topic \""+topic+"\"");
//        }
    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
