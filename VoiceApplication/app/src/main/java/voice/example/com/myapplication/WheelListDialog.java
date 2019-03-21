package voice.example.com.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;

/**
 * Created by liwu on 19-1-21.
 */

public class WheelListDialog extends AlertDialog {
    private static final String TAG = "WheelListDialog";
    private ListView mViewList;
    private int firstPosition;
    private View line1;
    private View line2;
    private ConstraintLayout constraintLayout;
    private View view;
    private Button btOk, btCancel;
    private Context mContext;
    private List<String> data = new ArrayList<>();
    private List<String> showData = new ArrayList<>();
    private WheelListAdapter adapter;
    private int showNum = 3;//中间子项上方显示子项数目
    //
    private int textColor[] = {BLACK, GRAY, GRAY, GRAY};//默认黑色
    private int textSize[] = {15, 13, 12, 10};//字体大小，从中间向外侧排列,这里是默认值
    private int color = -1;
    private int size = -1;
    private float textAlpha[] = {1.0f, 0.8f, 0.7f, 0.5f};
    //字体透明度，从中间向外侧排列,这里是默认值
    private int itemHeight = dp2px(40);
    // 每个子项高度为40dp
    private View.OnClickListener listener;

    //初始化，设置显示数据
    public WheelListDialog(@NonNull Context context, List<String> data) {
        super(context, R.style.style_dialog);
        mContext = context;
        this.data = data;
        view = LayoutInflater.from(context).inflate(R.layout.wheel_list, null);
    } //设置一个值，让显示字体颜色一致

    public void setTextColor(int textColor) {
        this.textColor = new int[showNum + 1];
        for (int i = 0; i < showNum + 1; i++) {
            this.textColor[i] = textColor;
        }
    } //设置一个值，让显示字体大小一致

    public void setTextSize(int textSize) {
        this.textSize = new int[showNum + 1];
        for (int i = 0; i < showNum + 1; i++) {
            this.textSize[i] = textSize;
        }
    } //获取点击位置

    public String getPositionData() {
        return showData.get(firstPosition);
    } //点击按钮监听

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    } //设置显示在中间之上的子项的数目

    public void setShowNum(int num) {
        showNum = num;
    } //设置显示字体大小，由中间向两侧排列

    public void setTextSize(int[] textSize) {
        this.textSize = textSize;
    } //设置显示字体透明度，由中间向两侧排列

    public void setTextAlpha(float[] textAlpha) {
        this.textAlpha = textAlpha;
    } //设置字体颜色，由中间向两侧排列

    public void setTextColor(int[] textColor) {
        this.textColor = textColor;
    }

    private void initView() {
        line1 = view.findViewById(R.id.line1);
        line2 = view.findViewById(R.id.line2);
        constraintLayout = (ConstraintLayout) view.findViewById(R.id.constraintLayout);
        mViewList = (ListView) view.findViewById(R.id.list_view);
        //设置大小
        ConstraintLayout.LayoutParams p = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        p.height = itemHeight * (showNum * 2 + 1) - 2;
        ConstraintLayout.LayoutParams p1 = (ConstraintLayout.LayoutParams) line1.getLayoutParams();
        p1.topMargin = itemHeight * showNum;
        ConstraintLayout.LayoutParams p2 = (ConstraintLayout.LayoutParams) line2.getLayoutParams();
        p2.topMargin = itemHeight * (showNum + 1);
//        btCancel = (Button) view.findViewById(R.id.btCancel);
        btOk = (Button) view.findViewById(R.id.btOk);
        adapter = new WheelListAdapter(mContext, showData);
        mViewList.setAdapter(adapter);
        mViewList.setOverScrollMode(android.view.View.OVER_SCROLL_NEVER);
        mViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position - showNum >= 0 && position < showData.size() - showNum) {
                    if (position + 1 == firstPosition + showNum) {
                        mViewList.smoothScrollBy(-dp2px(40), 300);
                        mViewList.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mViewList.requestFocusFromTouch();
                                mViewList.setSelection(position - showNum);
                            }
                        }, 300);
                    } else if (position < firstPosition + showNum) {
                        mViewList.smoothScrollByOffset(position - showNum - firstPosition + 1);
                        firstPosition = position - showNum + 1;
                    } else {
                        mViewList.smoothScrollByOffset(position - showNum - firstPosition);
                        firstPosition = position - showNum;
                    }
                    adapter.change(position);
                }
            }
        });
        mViewList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
                    mViewList.smoothScrollToPosition(firstPosition);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.d(TAG, "onItemClick: " + firstVisibleItem);
                if (firstVisibleItem < firstPosition) {
                    firstPosition = firstVisibleItem;
                    adapter.change(firstVisibleItem + showNum);
                } else if (firstVisibleItem > firstPosition) {
                    firstPosition = firstVisibleItem;
                    adapter.change(firstVisibleItem + showNum);
                }
            }
        });
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "第" + (firstPosition+1) + "项", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(data.get(firstPosition));
                WheelListDialog.this.dismiss();
            }
        });
//        btCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                WheelListDialog.this.dismiss();
//            }
//        });
    }

    private int dp2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private boolean checkDataIsWrong() {
        if (textAlpha != null && textSize != null &&
                textSize.length == textAlpha.length &&
                textSize.length == showNum + 1 &&
                textColor.length == showNum + 1)
            return true;
        try {
            throw new Exception("The mQueryTxtFileNameList is wrong!Please check the  variable:textSize,textAlpha,and showNum");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void show() {
        super.show(); //
        showData.clear();
        if (!checkDataIsWrong())
            return; //让前面和后面都空出来
        for (int i = 0; i < showNum; i++) {
            showData.add("");
        }
        showData.addAll(data);
        for (int i = 0; i < showNum; i++) {
            showData.add("");
        }
        initView();
        setContentView(view);
        Window window = getWindow();
        WindowManager.LayoutParams p = window.getAttributes();
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.width = WindowManager.LayoutParams.WRAP_CONTENT;
        p.dimAmount = 0.0f;
        window.setGravity(Gravity.CENTER);
        window.setAttributes(p);
    }

    class TextBean {
        int size = 15;
        float alpha = 1.0f;
        String text;
        int color;

        public TextBean(String string) {
            text = string;
        }

        public void setAlphaAndSize(Float alpha, int size) {
            this.alpha = alpha;
            this.size = size;
        }

        public int getSize() {
            return size;
        }

        public float getAlpha() {
            return alpha;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

    class WheelListAdapter extends BaseAdapter {
        List<String> adapterData;
        Context mContext;
        List<TextBean> beanList = new ArrayList<>();

        public WheelListAdapter(Context mContext, List<String> data) {
            this.adapterData = data;
            this.mContext = mContext;
            for (String str : adapterData) {
                beanList.add(new TextBean(str));
            } //初始化最先显示的数据
            for (int i = 0; i < showNum + 1; i++) {
                beanList.get(i).setAlphaAndSize(textAlpha[showNum - i], textSize[showNum - i]);
                beanList.get(i).setColor(textColor[showNum - i]);
            }
            for (int i = showNum + 1; i < showNum * 2 + 1; i++) {
                beanList.get(i).setAlphaAndSize(textAlpha[i - showNum], textSize[i - showNum]);
                beanList.get(i).setColor(textColor[i - showNum]);
            }
//            beanList.get(showNum * 2 + 1).setAlphaAndSize(textAlpha[textAlpha.length - 1], textSize[textSize.length - 1]);
//            beanList.get(showNum * 2 + 1).setColor(textColor[textColor.length - 1]);
        }

        @Override
        public int getCount() {
            return adapterData.size();
        }

        @Override
        public String getItem(int position) {
            return adapterData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.wheel_list_item, parent, false);
            } else {
                view = convertView;
            }
            TextView textView = (TextView) view.findViewById(R.id.text);
            TextBean text = beanList.get(position);
            textView.setText(text.getText());
            textView.setAlpha(text.getAlpha());
            textView.setTextSize(text.getSize());
            textView.setTextColor(text.getColor());
            return view;
        }

        private void change(int position) {
            for (int i = 0; i < showNum + 1; i++) {
                if (position + i < beanList.size()) {
                    beanList.get(position + i).setAlphaAndSize(textAlpha[i], textSize[i]);
                    beanList.get(position + i).setColor(textColor[i]);
                }
                if (position - i >= 0) {
                    beanList.get(position - i).setAlphaAndSize(textAlpha[i], textSize[i]);
                    beanList.get(position - i).setColor(textColor[i]);
                }
            }
            if (position + showNum + 1 < beanList.size()) {
                beanList.get(position + showNum + 1).setAlphaAndSize(textAlpha[showNum], textSize[showNum]);
                beanList.get(position + showNum + 1).setColor(textColor[showNum]);
            }
            if (position - showNum - 1 >= 0) {
                beanList.get(position - showNum - 1).setAlphaAndSize(textAlpha[showNum], textSize[showNum]);
                beanList.get(position - showNum - 1).setColor(textColor[showNum]);
            }
            notifyDataSetChanged();
        }
    }

}
