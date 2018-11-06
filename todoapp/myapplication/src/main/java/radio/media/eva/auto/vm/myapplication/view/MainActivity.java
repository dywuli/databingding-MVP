package radio.media.eva.auto.vm.myapplication.view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import radio.media.eva.auto.vm.myapplication.EventListener;
import radio.media.eva.auto.vm.myapplication.R;
import radio.media.eva.auto.vm.myapplication.model.TemperatureData;
import radio.media.eva.auto.vm.myapplication.model.User;
import radio.media.eva.auto.vm.myapplication.common.MainActivityContract;
import radio.media.eva.auto.vm.myapplication.databinding.ActivityMainBinding;
import radio.media.eva.auto.vm.myapplication.presenter.MainActivityPresenter;

public class MainActivity extends AppCompatActivity implements MainActivityContract.View{

    ActivityMainBinding dataBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBinding.setContent("Hello");
        dataBinding.setTextContent("first");
        final User user = new User();
        user.setBtnName("userButton");
        user.setTextName("userText");
        dataBinding.setUser(user);
        //dataBinding.setVariable(BR.user,user);
        dataBinding.setEvent(new EventListener() {
            @Override
            public void click1(View v) {
                dataBinding.setTextContent("click1");
                user.setBtnName("Btn111");
                user.setTextName("Text111");
            }

            @Override
            public void click2(View v) {
                dataBinding.setTextContent("click2");
                user.setBtnName("Btn222");
                user.setTextName("Text222");
            }

            @Override
            public void cilck3(String s) {
                dataBinding.setTextContent("cilck3" + s);
                user.setBtnName("Btn333");
                user.setTextName("Text333");
            }
        });
        dataBinding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataBinding.setContent("ID");
                dataBinding.textView.setText("NO needing findID");
            }
        });
        dataBinding.btnObservable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setBtnName("Observable");
                user.setTextName("Observable");
            }
        });
        MainActivityPresenter mainActivityPresenter = new MainActivityPresenter(this, getApplicationContext());
        TemperatureData temperatureData = new TemperatureData("Hamburg", "10");
        dataBinding.setTemp(temperatureData);
        dataBinding.setPresenter(mainActivityPresenter);


    }

    @Override
    public void showData(TemperatureData temperatureData) {
        String celsius = temperatureData.getCelsius();
        Toast.makeText(this, celsius, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setView(MainActivityContract.Presenter presenter) {
        getLifecycle().addObserver(presenter);
    }
}
