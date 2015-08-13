import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class SubscribeThread extends Thread {
    private MqttClient client;
    private String topic;

    public SubscribeThread(MqttClient client, String topic){
        this.client=client;
        this.topic=topic;
    }

    @Override
    public void run(){
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}