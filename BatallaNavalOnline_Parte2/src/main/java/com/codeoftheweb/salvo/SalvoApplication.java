package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.boot.CommandLineRunner;
import  org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

    @Autowired
    PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository){
		return (args) -> {


/*
			Player player1=new Player("j.bauer@ctu.gov", passwordEncoder.encode("24"));
			Player player2=new Player("c.obrian@ctu.gov", passwordEncoder.encode("42"));
			Player player3=new Player("kim_bauer@gmail.com", passwordEncoder.encode("kb"));
			Player player4=new Player("t.almeida@ctu.gov", passwordEncoder.encode("mole"));
			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);

			Game game1=new Game();
			Game game2=new Game(1);
			Game game3=new Game(2);
			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);

			GamePlayer gamePlayer1 = new GamePlayer(player1,game1);
            GamePlayer gamePlayer2 = new GamePlayer(player2,game1);
            GamePlayer gamePlayer3 = new GamePlayer(player1,game2);
            GamePlayer gamePlayer4 = new GamePlayer(player2,game2);
            GamePlayer gamePlayer5 = new GamePlayer(player2,game3);
            GamePlayer gamePlayer6 = new GamePlayer(player4,game3);

            gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);
			gamePlayerRepository.save(gamePlayer6);


			Ship ship1 =new Ship("Destroyer",gamePlayer1, new ArrayList<>(Arrays.asList("H2","H3","H4")),"h");
		    Ship ship2 =new Ship("Submarine",gamePlayer1, new ArrayList<>(Arrays.asList("E1","F1","G1")),"v");
            Ship ship3 =new Ship("PatrolBoat",gamePlayer1, new ArrayList<>(Arrays.asList("B4","B5")),"h");
            Ship ship4 =new Ship("Destroyer",gamePlayer2, new ArrayList<>(Arrays.asList("B5","C5","D5")),"v");
            Ship ship5 =new Ship("PatrolBoat",gamePlayer2, new ArrayList<>(Arrays.asList("F1","F2")),"h");

            Ship ship6 =new Ship("Destroyer",gamePlayer3, new ArrayList<>(Arrays.asList("B5","C5","D5")),"v");
            Ship ship7 =new Ship("PatrolBoat",gamePlayer3, new ArrayList<>(Arrays.asList("C6","C7")),"h");
            Ship ship8 =new Ship("Submarine",gamePlayer4, new ArrayList<>(Arrays.asList("A2","A3","A4")),"h");
            Ship ship9 =new Ship("PatrolBoat",gamePlayer4, new ArrayList<>(Arrays.asList("G6","H6")),"h");

            Ship ship10 =new Ship("Destroyer",gamePlayer5, new ArrayList<>(Arrays.asList("B5","C5","D5")),"h");
            Ship ship11 =new Ship("PatrolBoat",gamePlayer5, new ArrayList<>(Arrays.asList("C6","C7")),"h");
            Ship ship12 =new Ship("Submarine",gamePlayer6, new ArrayList<>(Arrays.asList("A2","A3","A4")),"h");
            Ship ship13 =new Ship("PatrolBoat",gamePlayer6, new ArrayList<>(Arrays.asList("G6","H6")),"v");

            gamePlayer1.addShip(ship1);
            gamePlayer1.addShip(ship2);
			gamePlayer1.addShip(ship3);
            gamePlayerRepository.save(gamePlayer1);

            gamePlayer2.addShip(ship4);
            gamePlayer2.addShip(ship5);
            gamePlayerRepository.save(gamePlayer2);

            gamePlayer3.addShip(ship6);
            gamePlayer3.addShip(ship7);
            gamePlayerRepository.save(gamePlayer3);

            gamePlayer4.addShip(ship8);
            gamePlayer4.addShip(ship9);
            gamePlayerRepository.save(gamePlayer4);

            gamePlayer5.addShip(ship10);
            gamePlayer5.addShip(ship11);
            gamePlayerRepository.save(gamePlayer5);

            gamePlayer6.addShip(ship12);
            gamePlayer6.addShip(ship13);
            gamePlayerRepository.save(gamePlayer6);

            Salvo salvo1= new Salvo(gamePlayer1,1,new ArrayList<>(Arrays.asList("B5","C5","F1")));
            Salvo salvo2= new Salvo(gamePlayer2,1,new ArrayList<>(Arrays.asList("B4","B5","B6")));
            Salvo salvo3= new Salvo(gamePlayer1,2,new ArrayList<>(Arrays.asList("F2","D5")));
            Salvo salvo4= new Salvo(gamePlayer2,2,new ArrayList<>(Arrays.asList("E1","H3","A2")));

            Salvo salvo5= new Salvo(gamePlayer3,1,new ArrayList<>(Arrays.asList("A2","A4","G6")));
            Salvo salvo6= new Salvo(gamePlayer4,1,new ArrayList<>(Arrays.asList("B5","D5","C7")));
            Salvo salvo7= new Salvo(gamePlayer3,2,new ArrayList<>(Arrays.asList("A3","H6")));
            Salvo salvo8= new Salvo(gamePlayer4,2,new ArrayList<>(Arrays.asList("C5","C6")));

            Salvo salvo9= new Salvo(gamePlayer5,1,new ArrayList<>(Arrays.asList("G6","H6","A4")));
            Salvo salvo10= new Salvo(gamePlayer6,1,new ArrayList<>(Arrays.asList("H1","H2","H3")));
            Salvo salvo11= new Salvo(gamePlayer5,2,new ArrayList<>(Arrays.asList("A2","A3","D8")));
            Salvo salvo12= new Salvo(gamePlayer6,2,new ArrayList<>(Arrays.asList("E1","F2","G3")));

            salvoRepository.save(salvo1);
            salvoRepository.save(salvo2);
            salvoRepository.save(salvo3);
            salvoRepository.save(salvo4);
            salvoRepository.save(salvo5);
            salvoRepository.save(salvo6);
            salvoRepository.save(salvo7);
            salvoRepository.save(salvo8);
            salvoRepository.save(salvo9);
            salvoRepository.save(salvo10);
            salvoRepository.save(salvo11);
            salvoRepository.save(salvo12);

            Score score1 = new Score(game1,player1,1);
            Score score2 = new Score(game1,player2,0);

            Score score3 = new Score(game2,player1,0.5);
            Score score4 = new Score(game2,player2,0.5);

            Score score5 = new Score(game3,player2,1);
            Score score6 = new Score(game3,player4,0);

            scoreRepository.save(score1);
            scoreRepository.save(score2);
            scoreRepository.save(score3);
            scoreRepository.save(score4);
            scoreRepository.save(score5);
            scoreRepository.save(score6);

            Player player1=new Player("mar.ben@ctu.gov", passwordEncoder.encode("27"));
            Player player2=new Player("ign.riv@ctu.gov", passwordEncoder.encode("72"));
            playerRepository.save(player1);
            playerRepository.save(player2);
            Game game1=new Game();
            GamePlayer gamePlayer1 = new GamePlayer(player1,game1);
            GamePlayer gamePlayer2 = new GamePlayer(player2,game1);
            gamePlayerRepository.save(gamePlayer1);
            gamePlayerRepository.save(gamePlayer2);

            Ship ship1 =new Ship("AircraftCarrier",gamePlayer1, new ArrayList<>(Arrays.asList("H2","H3","H4")));
            Ship ship2 =new Ship("Battleship",gamePlayer1, new ArrayList<>(Arrays.asList("E1","F1","G1")));
            Ship ship3 =new Ship("PatrolBoat",gamePlayer1, new ArrayList<>(Arrays.asList("B4","B5")));
            Ship ship4 =new Ship("Destroyer",gamePlayer2, new ArrayList<>(Arrays.asList("B5","C5","D5")));
            Ship ship5 =new Ship("PatrolBoat",gamePlayer2, new ArrayList<>(Arrays.asList("F1","F2")));



            salvoRepository.save(salvo1);
            salvoRepository.save(salvo2);
            salvoRepository.save(salvo3);
            salvoRepository.save(salvo4);
            salvoRepository.save(salvo5);
            salvoRepository.save(salvo6);
            salvoRepository.save(salvo7);
            salvoRepository.save(salvo8);
            salvoRepository.save(salvo9);
            salvoRepository.save(salvo10);
            salvoRepository.save(salvo11);
            salvoRepository.save(salvo12);
*/

        };
	}
}
