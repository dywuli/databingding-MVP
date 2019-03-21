package voice.example.com.memoryapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private static final String URL = "ws://192.168.2.1:55555";
    private WebSocketClient mWebSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebSocket = new WebSocketClient(URI.create(URL)) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.d("MainActivity", ">>>>>>>>onOpen ");
            }

            @Override
            public void onMessage(String message) {
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                super.onMessage(bytes);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onError(Exception ex) {
                Log.d("MainActivity", ">>>>>>>>onError " + ex);
            }
        };
        mWebSocket.connect();
    }
}
