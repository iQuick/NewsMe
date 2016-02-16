package me.imli.newme.rx;

import java.io.File;
import java.io.FileInputStream;

import rx.Observable;

/**
 * Created by Em on 2015/12/2.
 */
public class RxFileInputStream {

    public static Observable<FileInputStream> create(File file) {
        return Observable.defer(() -> {
            try {
                return Observable.just(new FileInputStream(file));
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

}
