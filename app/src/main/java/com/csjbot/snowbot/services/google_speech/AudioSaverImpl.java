package com.csjbot.snowbot.services.google_speech;

import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/08/17 0017-09:10.
 * Email: puyz@csjbot.com
 */

public class AudioSaverImpl implements IVoiceSaver {
    private File savedAuioFile;
    private Sink sink = null;
    private BufferedSink bufferedSink = null;
    private String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath();

    public AudioSaverImpl() {
    }

    public AudioSaverImpl(String dir) {
        saveDir = dir;
    }

    @Override
    public void startSaveFile(String fileName) {
        if (savedAuioFile != null) {
            closeFile();
        }

        savedAuioFile = new File(saveDir + "/" + fileName);

        try {
            sink = Okio.sink(savedAuioFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeData(byte[] data, int size) {
        try {
            bufferedSink.write(Arrays.copyOf(data, size));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeFile() {
        closeQuietly(bufferedSink);
        savedAuioFile = null;
    }


    public void closeQuietly(Closeable closeable) {

        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}
