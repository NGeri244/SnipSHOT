package com.snipshot.controller;

import com.snipshot.model.MainModel;
import com.snipshot.view.MainView;

public class MainController {
    private MainModel model;
    private MainView view;

    public MainController() {
        model = new MainModel();
        view = new MainView();
    }

    public void showMainView() {
        view.show();
    }
} 