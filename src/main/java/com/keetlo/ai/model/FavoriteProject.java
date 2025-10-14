package com.keetlo.ai.model;

import java.util.UUID;

public class FavoriteProject {
    
    public FavoriteProject(){};

      //Helper method
    public String createFavoriteProjectId() {
        return UUID.randomUUID().toString();
    }
}
