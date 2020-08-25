package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String type;
    private String position;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_Id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name="cell")
    private List<String> locations = new ArrayList<>();

    public Ship() {
    }

    public Ship(String type, GamePlayer gamePlayer, List<String> locations ) {
        this.type = type;
        this.gamePlayer = gamePlayer;
        this.locations = locations;
    }
    public Ship(String type, GamePlayer gamePlayer, List<String> locations, String position ) {
        this.type = type;
        this.gamePlayer = gamePlayer;
        this.locations = locations;
        this.position = position;
    }
    public long getId() {
        return id;
    }

     public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public String getPosition() { return position; }

    public void setPosition(String position) { this.position = position;}
}