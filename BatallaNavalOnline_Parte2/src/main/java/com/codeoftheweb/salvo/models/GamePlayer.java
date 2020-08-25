package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private LocalDateTime joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_Id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_Id")
    private Player player;

    @OneToMany(mappedBy="gamePlayer", cascade=CascadeType.ALL) 
    private Set<Ship> ships = new HashSet<Ship>();

    @OneToMany(mappedBy="gamePlayer", cascade=CascadeType.ALL) 
    private Set<Salvo> salvoes = new HashSet<Salvo>();

    public GamePlayer() {}
    public GamePlayer(Player player, Game game) {
        this.player=player;
        this.game=game;
        this.joinDate = LocalDateTime.now();
    }

    public void addShip(Ship ship) {
        ship.setGamePlayer(this);
        ships.add(ship);
    }

	public void addSalvo(Salvo salvo) {
        salvo.setGamePlayer(this);
        salvoes.add(salvo);
    }
	
    public long getId() {
        return id;
    }

    public void setGame(Game game) {
        this.game=game;
    }

    public Game getGame(){
        return game;
    }
    public void setPlayer(Player player) {
        this.player=player;
    }
    public  Player getPlayer(){
        return  player;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public Set<Salvo> getSalvoes() {
        return salvoes;
    }

    public void setSalvoes(Set<Salvo> salvoes) {
        this.salvoes = salvoes;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public Score getScore() {return this.player.getScore(this.game);}

    public List<Map<String,Object>> getHits(){

        if (getOponent()!=null)
            return getOponent().getSalvoes().stream().map(salvo -> salvo.makeSalvoHitsDTO()).collect(toList());
        else
            return new ArrayList<>();
    }

    public List<String> getSunkInMyShips(){
        GamePlayer opponent = getOponent();
        if (opponent !=null){
            List<String> locationsSalvo = opponent.getSalvoes().stream().flatMap( salvo -> salvo.getLocations().stream()).collect(toList());
            return ships.stream().filter(ship -> locationsSalvo.containsAll(ship.getLocations())).map(ship -> ship.getType()).collect(toList());
        }
        else{
            List<String> locationsSalvo = new ArrayList<>();
            return locationsSalvo;
        }
    }

    public GamePlayer getOponent(){
       //Me retorna un gamePlayer. Estoy en Game Player. No me retorna un Player.
       //Solo obtengo el primero(para este caso sirve). Y como findFirst puede devolver nulo, le pongo orElse()
       return this.game.getGamePlayers().stream().filter(gp-> gp.getId()!= this.id).findFirst().orElse(null);
    }

    public State getState(){
        State state = State.SHOOT;
        GamePlayer opponent = getOponent();
        int cantSunkShipsOpponent = getSunkInMyShips().size();
        int cantShipsViewer = ships.size();
        int turnViewer = getSalvoes().size();
        //Si están en el mismo turno
        if (cantShipsViewer == 0) {
            state = State.PLACE_SHIPS;
        }else{
            //Pregunto si oponente puso ships-> recién verifica si ganó, perdió el viewer sino puede ganar porque array = 0 y coincide
               if(opponent!=null) {
				   int cantSunkShipsViewer = opponent.getSunkInMyShips().size();
				   int cantShipsOpponent = opponent.getShips().size();
				   int turnOpponent = opponent.getSalvoes().size();
                   if (cantShipsOpponent == 5) {
                       //Mismo turno
                       if (turnViewer - turnOpponent == 0)  {
                           if (cantSunkShipsViewer == cantShipsOpponent && cantSunkShipsOpponent < cantShipsViewer) {
                               state = State.WIN;
                           } else if (cantSunkShipsViewer < cantShipsOpponent && cantSunkShipsOpponent == cantShipsViewer) {
                               state = State.LOST;
                           } else if (cantSunkShipsViewer == cantShipsViewer && cantSunkShipsOpponent == cantShipsOpponent) {
                               state = State.TIED;
                           }
                       }else {
                           if ((turnViewer - turnOpponent > 1 && cantSunkShipsOpponent!=cantShipsViewer) || cantSunkShipsViewer==cantShipsOpponent) {
                               state = State.WAIT_SHOOT_OPPONENT;
                           }
                       }
                   }
                   else{
                       state=State.WAIT_SHIPS_OPPONENT;
                   }
              }
               else 
                       state=State.WAIT_OPPONENT;
        }
        return state;
    }
}