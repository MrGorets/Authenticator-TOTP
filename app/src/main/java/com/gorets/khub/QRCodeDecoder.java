package com.gorets.khub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class QRCodeDecoder implements ImageAnalysis.Analyzer {

    BarcodeScannerOptions options;
    BarcodeScanner scanner;
    Context context;


    public QRCodeDecoder(Context context){
        this.context = context;
        options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        scanner = BarcodeScanning.getClient(options);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {

            @SuppressLint("UnsafeOptInUsageError")
            Image mediaImage = image.getImage();

            if (mediaImage != null){
                int rotationDeg = image.getImageInfo().getRotationDegrees();

                InputImage inputImage = InputImage.fromMediaImage(mediaImage, rotationDeg);

                Task<List<Barcode>> result = scanner.process(inputImage);
                result.addOnSuccessListener(barcode -> {
                    if (barcode.size()>0){
                        Barcode.UrlBookmark urlBookmark = barcode.get(0).getUrl();
                        String url = null;
                        try{
                            url = urlBookmark.getUrl();
                        } catch (Exception ex){
                            url = barcode.get(0).getDisplayValue();
                        }

                        if (!((ScanCode) context).isProcess && url != null){
                            ((ScanCode) context).isProcess = true;
                            ((ScanCode) context).qRCodeHandler(url);
                        }
                    }
                    image.close();
                });
                result.addOnFailureListener(e-> image.close());
            }
    }
}
