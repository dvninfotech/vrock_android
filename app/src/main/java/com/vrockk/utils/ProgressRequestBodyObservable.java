package com.vrockk.utils;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import okio.BufferedSink;

public class ProgressRequestBodyObservable extends RequestBody {

    File file;
    int ignoreFirstNumberOfWriteToCalls;
    int numWriteToCalls;

    public ProgressRequestBodyObservable(File file) {
        this.file = file;

        ignoreFirstNumberOfWriteToCalls =0;
    }

    public ProgressRequestBodyObservable(File file, int ignoreFirstNumberOfWriteToCalls) {
        this.file = file;
        this.ignoreFirstNumberOfWriteToCalls = ignoreFirstNumberOfWriteToCalls;
    }


    PublishSubject<Float> floatPublishSubject = PublishSubject.create();

    public Observable<Float> getProgressSubject(){
        return floatPublishSubject;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("image/*");
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }



    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        numWriteToCalls++;


        float fileLength = file.length();
        byte[] buffer = new byte[2048];
        FileInputStream in = new  FileInputStream(file);
        float uploaded = 0;

        try {
            int read;
            read = in.read(buffer);
            float lastProgressPercentUpdate = 0;
            while (read != -1) {

                uploaded += read;
                sink.write(buffer, 0, read);
                read = in.read(buffer);

                // when using HttpLoggingInterceptor it calls writeTo and passes data into a local buffer just for logging purposes.
                // the second call to write to is the progress we actually want to track
                if (numWriteToCalls > ignoreFirstNumberOfWriteToCalls ) {
                    float progress = (uploaded / fileLength) * 100;
                    //prevent publishing too many updates, which slows upload, by checking if the upload has progressed by at least 1 percent
                    if (progress - lastProgressPercentUpdate > 1 || progress == 100f) {
                        // publish progress
                        floatPublishSubject.onNext(progress);
                        lastProgressPercentUpdate = progress;
                    }
                }
            }
        } finally {
            in.close();
        }

    }
}
