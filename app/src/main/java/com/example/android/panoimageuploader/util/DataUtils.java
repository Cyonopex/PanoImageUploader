package com.example.android.panoimageuploader.util;

import com.example.android.panoimageuploader.database.ImageDetails;

import java.util.ArrayList;
import java.util.List;

public class DataUtils {

    public static List<ImageDetails> getDummyData() {

        ArrayList<ImageDetails> list = new ArrayList<>();

        list.add(new ImageDetails("Incomplete.jpg", ImageDetails.PROCESSING));

        for (int i=0; i < 20; i++) {
            String imageName = "Test "+ i + ".jpg";
            ImageDetails a = new ImageDetails(imageName, ImageDetails.COMPLETED);
            list.add(a);
        }

        return list;
    }
}
