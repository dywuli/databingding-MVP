package radio.media.eva.auto.vm.myapplication.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import java.util.Arrays;
import java.util.List;

import radio.media.eva.auto.vm.myapplication.MyAdapter;
import radio.media.eva.auto.vm.myapplication.R;
import radio.media.eva.auto.vm.myapplication.common.RecycleItemTouchHelper;
import radio.media.eva.auto.vm.myapplication.model.TemperatureData;

/**
 * Created by liwu on 18-11-2.
 */

public class SecondActivity1 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<TemperatureData> items =
                Arrays.asList(new TemperatureData("Hamburg", "5"), new TemperatureData("Berlin", "6"));

        // define an adapter
        mAdapter = new MyAdapter(items);
        recyclerView.setAdapter(mAdapter);
        ItemTouchHelper.Callback callback = new RecycleItemTouchHelper(this, (MyAdapter)mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
