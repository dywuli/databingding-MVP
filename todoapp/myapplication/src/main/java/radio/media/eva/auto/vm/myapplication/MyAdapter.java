package radio.media.eva.auto.vm.myapplication;

import android.databinding.ViewDataBinding;

import java.util.Collections;
import java.util.List;

import radio.media.eva.auto.vm.myapplication.model.TemperatureData;
import radio.media.eva.auto.vm.myapplication.common.MyBaseAdapter;

/**
 * Created by liwu on 18-11-2.
 */

public class MyAdapter extends MyBaseAdapter<MyAdapter.MyViewHolder> {
    private List<TemperatureData> data;

    @Override
    public void onItemDelete(int positon) {
        data.remove(positon);
        notifyItemRemoved(positon);
    }

    @Override
    public void onMove(int fromPosition, int toPosition) {
        Collections.swap(data,fromPosition,toPosition);//交换数据
        notifyItemMoved(fromPosition,toPosition);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends MyBaseAdapter.MyViewHolder {
        // each data item is just a string in this case
        public MyViewHolder(ViewDataBinding binding) {
            super(binding);
        }
        public void bind(Object obj) {
            binding.setVariable(BR.obj,obj);
            binding.executePendingBindings();
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<TemperatureData> myDataset) {
        data.addAll(myDataset);
    }
    @Override
    public Object getDataAtPosition(int position) {
        return data.get(position);
    }

    @Override
    public int getLayoutIdForType(int viewType) {
        return R.layout.rowlayout;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


}
