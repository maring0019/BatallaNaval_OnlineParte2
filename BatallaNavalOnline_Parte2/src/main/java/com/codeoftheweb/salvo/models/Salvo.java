package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_Id")
    private GamePlayer gamePlayer;

    private int turn;

    @ElementCollection
    @Column(name="cell")
    private List<String> locations = new ArrayList<>();
    //Hibernate necesita este constructor vacío.
    public Salvo() {
    }

    public Salvo(GamePlayer gamePlayer, int turn, List<String> locations) {
        this.gamePlayer = gamePlayer;
        this.turn = turn;
        this.locations = locations;
    }

    public long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public Map<String, Object> makeSalvoHitsDTO() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("turn", turn);
        dto.put("hitsShips", getShipHits());
        return  dto;
    }

    private  List<Map<String,Object>> getShipHits(){
        List<Map<String,Object>> lista = new ArrayList<>();
       gamePlayer.getOponent().getShips().stream().forEach(ship -> {
           List<String> hitsLocations = ship.getLocations().stream().filter(location-> locations.contains(location)).collect(toList());
           if (hitsLocations.size()>0)
               lista.add(makeHitsDTO(ship.getType(),hitsLocations));
       });
       return  lista;
    }

    private Map<String,Object> makeHitsDTO(String type, List<String> locations){
        Map<String, Object> dto = new HashMap<>();
        dto.put("type",type);
        dto.put("locations",locations);
        return dto;
    }


}


