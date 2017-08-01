package com.csjbot.snowbot.pupwindow;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.core.util.StrUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.views.BasePopupWindow;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jl
 * @Date: 2016/12/20
 * @Desc:
 */

public class AddHomePup extends BasePopupWindow  implements View.OnClickListener{
    private View view;
    private Context context;
    private EditText editText;
    private Button cancleBtn;
    private Button sureBtn;
    private List<String> mData = new ArrayList<>();

    private clickLIstener clickLIstener = null;


    public AddHomePup(View anchor, View contentView, Context context, int aniType,clickLIstener clickLIstener) {
        super(anchor, contentView, context, aniType);
        this.clickLIstener = clickLIstener;
        this.context = context;
        this.view = contentView;
        initView();
    }


    @Override
    public void initView() {
        editText = (EditText) view.findViewById(R.id.room_name_ed);
        cancleBtn = (Button) view.findViewById(R.id.see_qr_code_btn);
        sureBtn = (Button) view.findViewById(R.id.yes_btn);
        cancleBtn.setOnClickListener((View.OnClickListener) this);
        sureBtn.setOnClickListener((View.OnClickListener) this);

    }

    @Override
    public void onClick(View v) {
            switch(v.getId()){
                case R.id.see_qr_code_btn:
                    dismiss();
                break;
                case R.id.yes_btn:
                    String str  = editText.getText().toString();
                    if (StrUtil.isBlank(str)){
                        CSJToast.showToast(context,context.getResources().getString(R.string.roome_name_empty));
                        return;
                    }
                    clickLIstener.sure(str);
                    dismiss();
                break;
            }
    }

    public interface  clickLIstener{
        void cancle();
        void sure(String str);
    }

}
