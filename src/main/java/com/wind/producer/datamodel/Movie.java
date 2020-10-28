package com.wind.producer.datamodel;

import com.netflix.hollow.core.write.objectmapper.HollowHashKey;
import com.netflix.hollow.core.write.objectmapper.HollowPrimaryKey;

import java.util.Set;

@HollowPrimaryKey(fields = "id")
public class Movie {

    public int id;
    public String title;
    @HollowHashKey(fields = "actorName")
    public Set<Actor> actors;

    public Movie() {
    }

    public Movie(int id, String title, Set<Actor> actors) {
        this.id = id;
        this.title = title;
        this.actors = actors;
    }

}