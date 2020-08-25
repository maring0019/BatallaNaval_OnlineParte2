var app = new Vue({
	el: '#app',
	data: {
		games: [],
		players: [],
		userMail: '',
		userPassword: '',
		player: ''
	},

	filters: {
		formatDate: function (value) {
			if (!value) return '';
			return moment(value).format('MMMM Do YYYY, h:mm:ss a');
		}
	},
	methods: {
		loginEmail: function () {
			$.post("/api/login", {
						name: app.userMail,
						pwd: app.userPassword
					},
					function () {
						console.log("logged in!");
						window.location = "/web/games.html";
					}).done(function () {
					console.log("Listo");
				})
				.fail(function (data) {
					alert("Invalid Mail or Password");
				});

		},
		logout: function () {
			$.post("/api/logout",
					function () {
						console.log("logged out!");
						window.location = "/web/games.html";
					}).done(function () {
					console.log("Listo");
				})
				.fail(function (data) {
					alert("No se puede logout");
				});

		},
		signup: function () {
			$.post("/api/players", {
				name: app.userMail,
				pwd: app.userPassword
			}, function () {
				console.log("signing up");
				app.loginEmail()
			}).done(function () {
				console.log("Listo");
			}).fail(function (data) {
				alert("Email ya existente, Password o Email Inválido");
			});
		},
		createGame: function () {
			$.post("/api/games",
				function (data) {
					console.log("Creating game");
					window.location = "/web/game.html?Gp=" + data.id;
				}).done(function () {
				console.log("Listo");
			}).fail(function (data) {
				alert("Error, No se pudo crear el game");
			});
		},
		joinGame: function (idGame) {
			$.post("/api/game/"+idGame+"/players",
				function (data) {
					console.log("Joining game");
					window.location = "/web/game.html?Gp=" + data.id;
				}).done(function () {
				console.log("Listo");
			}).fail(function (data) {
				alert("Error, No se puede unir al game");
			});
		},

	}
})

$.get("/api/games")
	.done(function (data) {
		app.games = data.games;
		app.player = data.player;
		console.log(app.player);
		getScoresList()
	})
	.fail(function (data) {
		alert("error");
	});

function getScoresList() {
	var indice = -1;
	var id = -1;
	var games = app.games;
	var players = app.players;

	for (var i = 0; i < games.length; ++i) {
		for (var j = 0; j < games[i].gamePlayers.length; ++j) {
			id = games[i].gamePlayers[j].player.id;
			indice = players.findIndex(idPlayer => id == idPlayer.id);
			if (indice == -1) {
				players.push({
					id: games[i].gamePlayers[j].player.id,
					email: games[i].gamePlayers[j].player.email,
					total: 0,
					cantWin: 0,
					cantLoss: 0,
					cantTie: 0,

				});
				indice = players.length - 1;
				console.log("Indice nuevo:" + indice);
				addScore(indice, app.players[indice], app.games[i].gamePlayers[j].score.score)

			} else {
				addScore(indice, app.players[indice], app.games[i].gamePlayers[j].score.score)
			}
			indice = -1;
		}
	}
}

function addScore(indice, player, score) {
	score == 1.0 ? (player.total += score, ++player.cantWin) :
		(score == 0.0 ? (player.total += score, ++player.cantLoss) :
			(score == 0.5 ?(player.total += score, ++player.cantTie):player.total=player.total));
}
