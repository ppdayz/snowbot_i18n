package com.csjbot.snowbot.services.google_speech;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/08/17 0017-09:07.
 * Email: puyz@csjbot.com
 */

public interface IVoiceSaver {
    void startSaveFile(String fileName);

    void writeData(byte[] data, int size);

    void closeFile();
}
