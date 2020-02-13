package com.example.android.panoimageuploader.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImageDetailsDAO {

    @Query("SELECT * FROM imagedetails ORDER BY id desc")
    LiveData<List<ImageDetails>> loadAllDetails();

    @Insert
    void insertTask(ImageDetails details);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTask(ImageDetails details);

    @Delete
    void deleteTask(ImageDetails details);

    @Query("SELECT * FROM imagedetails WHERE id = :id")
    LiveData<ImageDetails> loadTaskById(int id);
}
