package com.csjbot.snowbot.Fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.utils.QRCodeUtil;
import com.google.zxing.WriterException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/06/08 0008-19:56.
 * Email: puyz@csjbot.com
 */

public class SNQRFregment extends BaseFrg {
    @BindView(R.id.sn_qr_textView)
    TextView snQrTextView;
    @BindView(R.id.sn_qr_imageView)
    ImageView snQrImageView;
    @BindView(R.id.sn_qr_textViewCode)
    TextView snQrTextViewCode;

    private String name, number;
    private  Unbinder unbinder;

    public void setNameAndNumber(String name, String number) {
        this.name = name;
        this.number = number;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, super.onCreateView(inflater, container, savedInstanceState));
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public int getContentViewId() {
        return R.layout.sn_qr_fregment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bitmap qrCode;
        try {
            qrCode = QRCodeUtil.getQrCodeImage(350, 350, number);
            snQrImageView.setImageBitmap(qrCode);
        } catch (WriterException e) {
            Csjlogger.error(e);
        }

        snQrTextView.setText(name);
        snQrTextViewCode.setText(number);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
