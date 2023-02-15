package com.example.budgetapp.services;

import java.io.File;

public interface FilesService {
    boolean saveToFile(String json);

    String readToFile();

    boolean cleanDataFile();

    File getDataFile();
}
