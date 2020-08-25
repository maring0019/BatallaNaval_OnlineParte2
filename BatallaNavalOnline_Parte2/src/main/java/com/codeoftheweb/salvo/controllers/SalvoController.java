package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.Location;
import java.util.*;
import static java.util.stream.Collectors.toList;
//Controller escucha peticiones
//Rest: Retorna respuesta como JSON -> accede a ruta /api...
@RestController
@RequestMapping("/api")
public class SalvoController {
    //Para cifrar las contraseñas
    //Autowired: inyecta. Permite que valor no sea nulo. Con "Autowired" saco cosas. Con "Bean" meto cosas en el Application Context
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @PostMapping(path = "/players")
    public ResponseEntity<Map<String, Object>> createUser(@RequestParam String name, @RequestParam String pwd) {

        if (name.isEmpty() || pwd.isEmpty()) {
            return new ResponseEntity<>(makeMap("error","No name or password given"), HttpStatus.FORBIDDEN);
        }

        Player player = playerRepository.findByUserName(name);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error","Player already used"), HttpStatus.CONFLICT);
        }
        //El password va codificado
        Player newPlayer = playerRepository.save(new Player(name,passwordEncoder.encode(pwd)));

        return new ResponseEntity<>(makeMap("name",makePlayerDTO(newPlayer)),HttpStatus.CREATED);
    }

    @GetMapping("/games")
    public Map<String, Object> getAllGame(Authentication authentication) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        if (!isGuest(authentication)){
            Player  player = playerRepository.findByUserName(authentication.getName());
            dto.put("player", makePlayerDTO(player));
        }else{
            dto.put("player", "invitado");
        }
        dto.put("games", gameRepository.findAll().stream().map(game -> makeGameDTO(game)).collect(toList()));
        return dto;
    }

    //Por defecto el requestMapping es un get. y por eso este no lleva el parámetro method: Get como en el Post
    @GetMapping("/game_view/{id}")
    private ResponseEntity<Map<String, Object>> getGamePlayer(@PathVariable Long id, Authentication authentication){

        if (isGuest(authentication))
            return new ResponseEntity<>(makeMap("Error","Acceso no autorizado"),HttpStatus.UNAUTHORIZED);

        Player  player = playerRepository.findByUserName(authentication.getName());

        // Retorna tipo Optional (cuando valor puede ser nulo al hacer findById(). A partir de versión 8 de java). Obtengo valor dentro de Optional con get()

        Optional<GamePlayer> gamePlayerOptional =  gamePlayerRepository.findById(id);

        if(!gamePlayerOptional.isPresent())
            return new ResponseEntity<>(makeMap("Error","No existe el Game Player con ese Id "),HttpStatus.UNAUTHORIZED);

        Map<String, Object> dto = makeGameViewDTO(gamePlayerOptional.get());
        //Usamos get() porque es de tipo Optional

        if (gamePlayerOptional.get().getPlayer().getId() != player.getId())
            return new ResponseEntity<>(makeMap("Error","Acceso no autorizado"),HttpStatus.UNAUTHORIZED);

        return  new ResponseEntity<>(dto,HttpStatus.OK);
    }

    @PostMapping(path = "/games")
    private ResponseEntity<Map<String, Object>> createGame(Authentication authentication){
        //Error si es invitado
        if (isGuest(authentication))
            return new ResponseEntity<>(makeMap("Error","Acceso no autorizado"),HttpStatus.UNAUTHORIZED);
        //Usuario actual
        Player  player = playerRepository.findByUserName(authentication.getName());
        //Crea y guarda un nuevo game
        Game game = new Game();
        gameRepository.save(game);
        GamePlayer gamePlayer=new GamePlayer(player, game);
        gamePlayerRepository.save(gamePlayer);

        return new ResponseEntity<>(makeMap("id", gamePlayer.getId()), HttpStatus.CREATED);
    }
    //@RequestMapping(path = "/game/{gamePlayerId}/players", method = RequestMethod.POST) lo cambio a @PostMapping.
    @PostMapping(path = "/game/{gamePlayerId}/players")
    private ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gamePlayerId, Authentication authentication){
        //Error si es invitado
        if (isGuest(authentication))
            return new ResponseEntity<>(makeMap("Error","Acceso no autorizado"),HttpStatus.UNAUTHORIZED);
        //Usuario actual
        Player  player = playerRepository.findByUserName(authentication.getName());
        //Obtiene game con id=gamePlayerId
        Optional<Game> gameOptional =  gameRepository.findById(gamePlayerId);
        //AGREGADO, esto faltaba. Estaba contolado en el front-end pero no el back-end
        //findFirst() devuelve un Optional.
        if(gameOptional.get().getPlayers().stream().findFirst().get().getId() ==  player.getId())
            return new ResponseEntity<>(makeMap("Error","Ud. ya esta en este juego. No puede volverse a unir"),HttpStatus.UNAUTHORIZED);
        //Si no hay ninguno, error
        if (!gameOptional.isPresent())
            return new ResponseEntity<>(makeMap("Error", "No such game"), HttpStatus.FORBIDDEN);

        if (gameOptional.get().getPlayers().size()>1)
            return new ResponseEntity<>(makeMap("Error", "Game is full"), HttpStatus.FORBIDDEN);
        //Crea y guarda un nuevo game player para este game y el usuario actual
        GamePlayer gamePlayer=new GamePlayer(player, gameOptional.get());
        gamePlayerRepository.save(gamePlayer);

        return new ResponseEntity<>(makeMap("id", gamePlayer.getId()), HttpStatus.CREATED);
    }

    @PostMapping(path = "/games/players/{gamePlayerId}/ships")
    private ResponseEntity<Map<String, Object>> placeShips(@PathVariable Long gamePlayerId,Authentication authentication,@RequestBody List<Ship> ships){

        //No ir a la base de datos si no esta autenticado
        if (isGuest(authentication))
            return new ResponseEntity<>(makeMap("Error","No hay un usuario logueado"),HttpStatus.UNAUTHORIZED);

        Optional<GamePlayer> gamePlayerOptional =  gamePlayerRepository.findById(gamePlayerId);

        if(!gamePlayerOptional.isPresent())
            return new ResponseEntity<>(makeMap("Error","No existe el Game Player con ese Id "),HttpStatus.UNAUTHORIZED);
        //Usuario actual
        Player  player = playerRepository.findByUserName(authentication.getName());

        //No hay jugador con el id dado. Ese player no esta en ese juego. Quiere poner barcos en un juego donde no está el usuario autenticado
        if(gamePlayerOptional.get().getPlayer().getId()!=player.getId())
            return new ResponseEntity<>(makeMap("Error","Ud. no es el jugador en este juego"),HttpStatus.UNAUTHORIZED);

        if(gamePlayerOptional.get().getState()!=State.PLACE_SHIPS){
            return new ResponseEntity<>(makeMap("Error", "Ud. no puede ubicar ships"), HttpStatus.FORBIDDEN);
        }

        if (gamePlayerOptional.get().getShips().size()>1)
            return new ResponseEntity<>(makeMap("Error", "Ud. already has ships placed."), HttpStatus.FORBIDDEN);

        //AGREGADO: Verifica que haya 5 barcos para recién guardarlos
        if (ships.size()<5)
            return new ResponseEntity<>(makeMap("Error", "Ud. no ha colocado todos los barcos"), HttpStatus.FORBIDDEN);

        //Guardar en repositorio los ship. Pero tengo un listado de ship y ship repositorio guarda de a uno
        //Recorrer el listaso de ship y asignar cada ship, add y save
        ships.stream().forEach(ship->gamePlayerOptional.get().addShip(ship));
        //Repository en gamePlayer no en ship. Por eso no hace repository en Ship. Cuando ya hice el add de cada ship, guardo una sola vez en gamePlayer
        gamePlayerRepository.save(gamePlayerOptional.get());
        //Respuesta de éxito
        return new ResponseEntity<>(makeMap("Exito", "Ships added to the game player and saved"), HttpStatus.CREATED);
    }

    @PostMapping(path = "/games/players/{gamePlayerId}/salvoes")
    private ResponseEntity<Map<String, Object>> storeSalvos(@PathVariable Long gamePlayerId,Authentication authentication,@RequestBody Salvo salvo){
        //Error si es invitado
        //No ir a la base de datos si no está autenticado
        if (isGuest(authentication))
            return new ResponseEntity<>(makeMap("Error","No hay un usuario logueado"),HttpStatus.UNAUTHORIZED);

        Optional<GamePlayer> gamePlayerOptional =  gamePlayerRepository.findById(gamePlayerId);

        if(!gamePlayerOptional.isPresent())
            return new ResponseEntity<>(makeMap("Error","No existe el Game Player con ese Id "),HttpStatus.UNAUTHORIZED);
        //Usuario actual
        Player  player = playerRepository.findByUserName(authentication.getName());

        //No hay jugador con el id dado. Ese player no esta en ese juego. Quiere poner barcos en un juego donde no está el usuario autenticado
        if(gamePlayerOptional.get().getPlayer().getId()!=player.getId())
            return new ResponseEntity<>(makeMap("Error","Ud. no es el jugador en este juego"),HttpStatus.UNAUTHORIZED);

        if(gamePlayerOptional.get().getState()!=State.SHOOT){
            return new ResponseEntity<>(makeMap("Error", "Ud. no puede disparar"), HttpStatus.FORBIDDEN);
        }

        //Verificar que envíe al menos un salvo
        if (salvo.getLocations().size()==0)
            return new ResponseEntity<>(makeMap("Error", "Ud. no ha seleccionado ningun salvo"), HttpStatus.FORBIDDEN);

        //Verifico que no exceda el límite de salvos permitidos
        if (salvo.getLocations().size()>5)
            return new ResponseEntity<>(makeMap("Error", "Ud. ha excedido el maximo permitido de salvoes por turno (5)"), HttpStatus.FORBIDDEN);

        GamePlayer oponent = gamePlayerOptional.get().getOponent();

        if(gamePlayerOptional.get().getShips().size()==0){
            return new ResponseEntity<>(makeMap("Error", "Ud. no puede disparar porque aun no ha ubicado sus ships."), HttpStatus.FORBIDDEN);
        }

        if(oponent!=null) {
            if(gamePlayerOptional.get().getState()!=State.SHOOT && (gamePlayerOptional.get().getState()==State.WAIT_SHOOT_OPPONENT || gamePlayerOptional.get().getState()==State.WAIT_OPPONENT || gamePlayerOptional.get().getState()==State.WAIT_SHIPS_OPPONENT) ){
                return new ResponseEntity<>(makeMap("Error", "Ud. esta adelantado respecto al turno de su oponente."), HttpStatus.FORBIDDEN);
            }
        }

        salvo.setTurn(gamePlayerOptional.get().getSalvoes().size()+1);
        gamePlayerOptional.get().addSalvo(salvo);
        gamePlayerRepository.save(gamePlayerOptional.get());

        double puntoViewer = -1.0;
        double puntoOpponent = -1.0;
        if (gamePlayerOptional.get().getState() == State.WIN) {puntoViewer = 1.0; puntoOpponent=0.0;
            Score scoreViewer = new Score(gamePlayerOptional.get().getGame(),gamePlayerOptional.get().getPlayer(),puntoViewer);
            scoreRepository.save(scoreViewer);
            Score scoreOpponent = new Score(oponent.getGame(),oponent.getPlayer(),puntoOpponent);
            scoreRepository.save(scoreOpponent);
        }
        else if (gamePlayerOptional.get().getState() == State.LOST)  {puntoViewer = 0.0; puntoOpponent=1.0;
            Score scoreViewer = new Score(gamePlayerOptional.get().getGame(),gamePlayerOptional.get().getPlayer(),puntoViewer);
            scoreRepository.save(scoreViewer);
            Score scoreOpponent = new Score(oponent.getGame(),oponent.getPlayer(),puntoOpponent);
            scoreRepository.save(scoreOpponent);}
        else if(gamePlayerOptional.get().getState() == State.TIED) {puntoViewer = 0.5; puntoOpponent=0.5;
            Score scoreViewer = new Score(gamePlayerOptional.get().getGame(),gamePlayerOptional.get().getPlayer(),puntoViewer);
            scoreRepository.save(scoreViewer);
            Score scoreOpponent = new Score(oponent.getGame(),oponent.getPlayer(),puntoOpponent);
            scoreRepository.save(scoreOpponent);
        }

        return new ResponseEntity<>(makeMap("Exito", "Salvo added to the game player and saved"), HttpStatus.CREATED);
    }



    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
    private Map<String, Object> makeGameDTO(Game game){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer -> makeGamePlayerDTO(gamePlayer)).collect(toList()));
        return dto;
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player",makePlayerDTO(gamePlayer.getPlayer()));
        dto.put("score", makeScoreDTO(gamePlayer.getScore()));
        return  dto;
    }

    private Map<String, Object> makePlayerDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getUserName());
        return  dto;
    }

    private Map<String, Object> makeScoreDTO(Score score){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        //Si el score devuelve nulo para que no de error o lo cuente como punto perdido
        if (score!= null)
            dto.put("score",score.getScore());
        else
            dto.put("score", null);
        return  dto;
    }

    private Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getJoinDate());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(gamePlayer2 -> makeGamePlayerDTO(gamePlayer2)).collect(toList()));
        dto.put("ships", gamePlayer.getShips().stream().map(ship -> makeShipDTO(ship)).collect(toList()));
        //Con flatMap obtengo un solo elemento
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream()
                //Poniendo map solo, obtengo un array de salvos dentro del array. Y con flatMap obtengo un solo objeto y no un array.
                .flatMap(gamePlayer3 -> gamePlayer3.getSalvoes().stream()
                        //Debo hacer nuevo DTO para salvo, porque obtengo recurrencia.
                        .map(salvo -> makeSalvoDTO(salvo)))
                .collect(toList()));

        dto.put("hitsOnViewer", gamePlayer.getHits());
        dto.put("sunkOnViewer", gamePlayer.getSunkInMyShips());

        if (gamePlayer.getOponent()!=null){
            dto.put("shipsOpponent", gamePlayer.getOponent().getShips().size());
            dto.put("hitsOnOpponent", gamePlayer.getOponent().getHits());
            dto.put("sunkOnOpponent", gamePlayer.getOponent().getSunkInMyShips());
        }
        else{
            dto.put("hitsOnOpponent",new ArrayList<>());
            dto.put("sunkOnOpponent", new ArrayList<>());
        }

        dto.put("state",gamePlayer.getState());
        return dto;
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn",salvo.getTurn());
        dto.put("player",salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations",salvo.getLocations());
        return  dto;
    }

    private Map<String, Object> makeShipDTO(Ship ship){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations",ship.getLocations());
        dto.put("position",ship.getPosition());
        return  dto;
    }
}
