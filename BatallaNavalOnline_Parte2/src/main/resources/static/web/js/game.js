var app = new Vue({
	el: '#app',
	data: {
		//Datos del encabezado de la tabla(nombres columnas)
		columnHeaders: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
		//Datos de la primera columna de la tabla (nombres de filas)
		rowHeaders: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
		//Necesario numérico para pasarlo para array
		valor: -1,
		url: '',
		game: {},
		viewer: {},
		otherPlayer: {},
		shipLocations: [],
		salvoLocations: [],
		shipSalvoLocations: [],
		//Guarda el tipo de ship seleccionado
		shipType: "",
		shipLength: -1,
		position: "",
		//Guarda posiciones todas, repetidas
		locations: [],
		locationsSalvo: [],
		//Guarda si no hay posiciones ya repetidas
		newsLocations: [],
		//Posición del ship actual
		newLocation: [],
		shipsType: [],
		showCreateShip: false,
		showCreateSalvo: false,
		turnViewer: 0,
		turnOponent: 0,
		//Datos de los hits on viewer del json
		hitsOnViewer: [],
		//Datos de los hits on oponent del json
		hitsOnOpponent: [],
		//Array de objetos hitsOnViewer para mostrar en el html
		hitsOnViewerHtml: [],
		//Array de objetos hitsOnViewer para mostrar en el html
		hitsOnOpponentHtml: [],
		//Todos los ships disponible
		shipsData: [{

				"type": "AircraftCarrier",
				"longitud": 5,
				"locations": [],
				"position": "",
				//Almacena la parte de barco que va mostrando
				"part": 0,
				"link": 'images/aircraftCarrier.png',
				"sunk": 'images/aircraftCarrierSunk.png',
		}, {
				"type": "Battleship",
				"longitud": 4,
				"locations": [],
				"position": "",
				//Almacena la parte de barco que va mostrando
				"part": 0,
				"link": 'images/battleship.png',
				"sunk": 'images/battleshipSunk.png',
		}, {
				"type": "Submarine",
				"longitud": 3,
				"locations": [],
				"position": "",
				//Almacena la parte de barco que va mostrando
				"part": 0,
				"link": 'images/submarine.png',
				"sunk": 'images/submarineSunk.png',
		},

			{
				"type": "Destroyer",
				"longitud": 3,
				"locations": [],
				"position": "",
				//Almacena la parte de barco que va mostrando
				"part": 0,
				"link": 'images/destroyer.png',
				"sunk": 'images/destroyerSunk.png',
		},

			{
				"type": "PatrolBoat",
				"longitud": 2,
				"locations": [],
				"position": "",
				//Almacena la parte de barco que va mostrando
				"part": 0,
				"link": 'images/patrolBoat.png',
				"sunk": 'images/patrolBoatSunk.png',
		}
		],
		//Guardo los ships que va seleccionando el usuario
		ships: [],
		salvos: {},
		//Guarda todos los barcos golpeados
		shipsHitsViewer: [],
		shipsHitsOpponent: [],
		sunksOnViewer: [],
		sunksOnOpponent: [],
		leftOnViewer: 0,
		leftOnOpponent: 0,
		//Almacena los hits dados en los ships del oponente
		hitsOpponent: [],
		gameState: "",
		timerReloadId: "",
		timerWaitId: "",
		scores: {},
		timerReload: 0,
		timerWait: 0,
		message: "",
		fire: 'images/hit.gif',

	},
	methods: {

		startReload: function (tiempo) {
			app.timerReloadId = setInterval(function () {
				console.log("Reloading Page: " + window.location);
				window.location.reload();

			}, tiempo);
		},

		getHits: function (hitsPlayer) {
			var shipsHits = [];
			var tipo = "";

			for (var i = 0; i < hitsPlayer.length; ++i) {
				for (var j = 0; j < hitsPlayer[i].hitsShips.length; ++j) {
					tipo = hitsPlayer[i].hitsShips[j].type;
					shipsHits.push({
						turn: hitsPlayer[i].turn,
						type: tipo,
						//Array que almacena cada golpe
						hits: hitsPlayer[i].hitsShips[j].locations,
					});
				}
			}
			console.log("ShipsHits: " + shipsHits);
			return shipsHits;
		},

		getHitsOpponent: function (hitsPlayer) {
			var hits = [];
			for (var i = 0; i < hitsPlayer.length; ++i) {
				for (var j = 0; j < hitsPlayer[i].hitsShips.length; ++j) {
					for (var k = 0; k < hitsPlayer[i].hitsShips[j].locations.length; ++k) {
						hits.push(hitsPlayer[i].hitsShips[j].locations[k]);
					}
				}
			}

			return hits;

		},

		getTurnoSalvo: function (celda) {
			var index = this.salvoLocations.findIndex(salvo => salvo.location == celda);
			if (index != -1) {
				console.log(this.salvoLocations[index].turno);
				return this.salvoLocations[index].turno;
			} else
				return -1;
		},
		getTurnoShipSalvo: function (celda) {
			var index = this.shipSalvoLocations.findIndex(salvo => salvo.location == celda);
			if (index != -1) {
				console.log(this.shipSalvoLocations[index].turno);
				return this.shipSalvoLocations[index].turno;
			} else
				return -1;
		},
		logout: function () {
			$.post("/api/logout",
					function () {
						console.log("logged out!");
						window.location = "/web/games.html";
					}).done(function () {
					console.log("Ready");
				})
				.fail(function (data) {
					swal("Cannot log in.");
				});
		},


		ubicarShip: function (row, col) {
			if (app.gameState == "PLACE_SHIPS") {

				app.obtenerInfoShip()
				//Valido ubicación valida, no fuera de la grilla
				if (app.valido(row, col)) {
					//Valido si barco ya ha sido seleccionado
					app.reubicar()
					//Ejecuta si ha seleccionado la posición y el barco
					if (app.selectedShip()) {
						if (!app.ubicar(row, col)) {
							app.guardarShip();
						}
					} else {
						swal("Error, need to select boat to locate");
						console.log("You must select the ship to locate.");
					}

				} else {
					swal("Error, you have touched the grid limit");
				}
			} else {
				swal("Error, you have already placed your ships or the Game is over.");
			}
		},

		ubicarSalvo: function (row, col) {

			if (app.gameState != "PLACE_SHIPS") {

				var turno = Math.abs((app.turnViewer + 1) - app.turnOponent);

				if (app.gameState == "SHOOT") {
					//Verificar que posición esta libre
					if (app.locationsSalvo.findIndex(s => s == row + col) == -1 && app.salvoLocations.findIndex(s => s.location == row + col) == -1 && app.locationsSalvo.length < 5) {
						//Probar con GetByElementByID(nombre de las clases )
						app.locationsSalvo.push(row + col);
					} else {
						if (app.locationsSalvo.findIndex(s => s == row + col) != -1) {
							var eliminarLocations = app.locationsSalvo.splice(app.locationsSalvo.findIndex(s => s == row + col), 1);
						}
						//Verificar la cantidad de disparos permitidos por turno
						if (app.locationsSalvo.length >= 5) {
							swal("You cannot select more than 5 shots per turn.");
						}
						
					}
				} else {

					swal("Error, There is no opponent yet, you are ahead of your opponent's turn or the Game is over");

				}

			} else {
				swal("You cannot save the shots. You must first locate the ships.");
			}
		},

		obtenerInfoShip: function () {

			//Obtengo la posición del ship
			var radioButTrat = document.getElementsByName("position");
			//Obtengo el tipo de ship
			app.shipType = document.getElementById("ships").value;
			for (var i = 0; i < radioButTrat.length; i++) {
				if (radioButTrat[i].checked == true) {
					app.position = radioButTrat[i].value;
				}
			}
		},
		selectedShip: function () {
			var selected = false;
			if (document.getElementById("ships").value != "") {
				selected = true;
			}
			return selected;
		},

		reubicar: function () {
			var indice = app.ships.findIndex(s => s.type == document.getElementById("ships").value);
			if (indice != -1) {
				swal("The selected ship was already located previously, you will relocate the ship.");
				var eliminarLocations = app.shipsData[app.shipsData.findIndex(s => s.type == app.shipType)].locations.splice(0);
				var removed = app.newsLocations.splice(app.newsLocations.findIndex(l => l == eliminarLocations[0]), app.shipsData[app.shipsData.findIndex(s => s.type == app.shipType)].longitud);
				app.ships.splice(indice, 1);

			}

		},

		valido: function (row, col) {
			app.shipLength = app.shipsData[app.shipsData.findIndex(s => s.type == app.shipType)].longitud;
			if ((app.position == "h" && col + app.shipLength - 1 > 10) || (app.position == "v" && (row.charCodeAt()) - "@".charCodeAt() + app.shipLength - 1 > 10)) {
				return false;

			} else {
				return true;
			}
		},

		ubicar: function (row, col) {

			var cantPos = 0;
			var existe = false;
			app.newLocation = [];

			if (app.position == "v") {
				for (var i = 0; i < app.rowHeaders.length; ++i) {
					if (row == app.rowHeaders[i]) {
						++cantPos;
						var newCelda = app.rowHeaders[i] + col;
						if (app.newsLocations.indexOf(newCelda) != -1) {
							existe = true;
							swal("The position is already occupied, select new position.");
							break;
						}
						app.newLocation.push(newCelda);
						var indexRow = i;
					}
					if (i > indexRow && cantPos < app.shipLength) {
						++cantPos;
						var newCelda = app.rowHeaders[i] + col;
						if (app.newsLocations.indexOf(newCelda) != -1) {
							existe = true;
							swal("The position is already occupied, select new position.");
							break;
						}
						app.newLocation.push(newCelda);
					}

				}
			} else {
				for (var i = 0; i < app.columnHeaders.length; ++i) {
					if (col == app.columnHeaders[i]) {
						++cantPos;
						var newCelda = row + app.columnHeaders[i];
						if (app.newsLocations.indexOf(newCelda) != -1) {
							existe = true;
							swal("The position is already occupied, select new position.");
							break;
						}
						app.newLocation.push(newCelda);
						var indexCol = i;
					}
					if (i > indexCol && cantPos < app.shipLength) {
						++cantPos;
						var newCelda = row + app.columnHeaders[i];
						if (app.newsLocations.indexOf(newCelda) != -1) {
							existe = true;
							swal("The position is already occupied, select new position.");
							break;
						}
						app.newLocation.push(newCelda);
					}

				}

			}
			return existe;
		},

		guardarShip: function () {
			app.newLocation.forEach(l => app.newsLocations.push(l));
			app.locations = app.newsLocations;
			app.shipsData[app.shipsData.findIndex(s => s.type == app.shipType)].locations = app.newLocation;
			app.shipsData[app.shipsData.findIndex(s => s.type == app.shipType)].position = app.position;
			console.log("Ships Data: " + app.shipsData);
			app.ships.push({
				type: document.getElementById("ships").value,
				locations: app.newLocation,
				position: app.position,
			});
		},

		getShipTypePartUbicar: function (celda) {
			var indice = app.shipsData.findIndex(s => s.locations.indexOf(celda) != -1);
			var tipo = app.shipsData[indice].type;
			var parte = "";
			
			if (app.shipsData[indice].position == "v") {
				parte = tipo + "V" + app.shipsData[indice].locations.indexOf(celda) + " " + "shipVer";
			} else {
				parte = tipo + "H" + app.shipsData[indice].locations.indexOf(celda) + " " + "shipHor";
			}
			//Retornar la clase a dibujar 
			//Una clase por cada parte de barco(celda)

			//return tipo+"V"+parte;
			return parte;
			//return parte;


		},

		getShipTypePartMostrar: function (celda) {

			var indice = app.game.ships.findIndex(s => s.locations.indexOf(celda) != -1);
			var tipo = app.game.ships[indice].type;
			var parte = "";
			
			//Retornar la clase a dibujar 
			//Una clase por cada parte de barco(celda)

			//return tipo+"V"+parte;
			//return tipo + " " + parte;
			if (app.game.ships[indice].position == "v") {
				parte = tipo + "V" + app.game.ships[indice].locations.indexOf(celda) + " " + "shipVer";
			} else {
				parte = tipo + "H" + app.game.ships[indice].locations.indexOf(celda) + " " + "shipHor";
			}
			return parte;

		},


		listoShips: function () {
			$.post({
					url: "/api/games/players/" + app.valor + "/ships",
					data: JSON.stringify(app.ships),
					success: function () {
						console.log("Ships added");
						window.location.reload();
					},
					dataType: "text",
					contentType: "application/json"
				})
				.done(function () {
					//console.log("Ready");
					//window.location.reload();
				}).fail(function (data) {
					("Error, Ships not added, Missing load all ships");
				});
		},


		listoSalvoes: function () {
			if (app.locationsSalvo.length != 0 && app.locationsSalvo.length <= 5) {

				app.salvos = {
					turn: 0,
					locations: app.locationsSalvo,
				};

				$.post({
						url: "/api/games/players/" + app.valor + "/salvoes",
						data: JSON.stringify(app.salvos),
						success: function () {
							//console.log("Added shots");
							window.location.reload();
						},
						dataType: "text",
						contentType: "application/json"
					})
					.done(function () {
						//console.log("Ready");
						window.location.reload();
					}).fail(function (data) {
						swal("Error, shots not added");
						//Que recargue la página porque puede ser que el juego ya haya terminado porque no  actualiza cuando está en shoot
						//reload
						window.location.reload();
					});
			} else {
				swal("Error, you. you did not select any save or You have exceeded the maximum limit of saved (5) per shift.");
			}
		},

		leaderboard: function () {
			window.location = "/web/games.html";
		},

	}
})

getParameter()

function getParameter() {
	const urlParams = new URLSearchParams(window.location.search);
	const myParam = urlParams.get('Gp');
	app.valor = myParam;
	app.url = '/api/game_view/' + app.valor;
}

$.get(app.url)
	.done(function (data) {
		app.game = data;

		app.hitsOnViewer = app.game.hitsOnViewer = !null ? app.game.hitsOnViewer : '';
		app.hitsOnOpponent = app.game.hitsOnOpponent != null ? app.game.hitsOnOpponent : '';

		app.sunksOnViewer = app.game.sunkOnViewer != null ? app.game.sunkOnViewer : '';
		app.sunksOnOpponent = app.game.sunkOnOpponent != null ? app.game.sunkOnOpponent : '';

		if (app.game.ships != null && app.sunksOnViewer != null)
			app.leftOnViewer = app.game.ships.length - app.sunksOnViewer.length;
		if (app.game.shipsOpponent != null && app.sunksOnOpponent != null)
			app.leftOnOpponent = app.game.shipsOpponent - app.sunksOnOpponent.length;

		app.hitsOpponent = app.hitsOnOpponent != null ? app.getHitsOpponent(app.hitsOnOpponent) : '';
		console.log(app.hitsOpponent);

		//Para obtener el player y el viewer
		getPlayers()
		getShipLocations()
		getSalvoLocations()
		getShipSalvo()

		app.shipsHitsViewer = app.hitsOnViewer != null ? app.getHits(app.hitsOnViewer) : '';
		app.shipsHitsOpponent = app.hitsOnOpponent != null ? app.getHits(app.hitsOnOpponent) : '';
		app.gameState = app.game.state;

		if (app.gameState == "WAIT_OPPONENT" || app.gameState == "WAIT_SHIPS_OPPONENT" || app.gameState == "WAIT_SHOOT_OPPONENT") {
			app.timerReload = 10000;
			app.startReload(app.timerReload)
		}
		switch (app.gameState) {
			case 'PLACE_SHIPS':
				app.message = 'Locate your 5 ships';
				break;
			case 'SHOOT':
				app.message = 'Select your shots';
				break;
			case 'WIN':
				app.message = ' GAME OVER - CONGRATULATIONS, YOU YOU WON THE GAME! :)';
				break;
			case 'LOST':
				app.message = " GAME OVER - I'M SORRY, YOU. YOU HAVE LOST THE GAME! :(" ;
				break;
			case 'TIED':
				app.message = 'GAME OVER - OK, YOU HAS PACKED THE GAME! :|';
				break;
			case 'WAIT_SHIPS_OPPONENT':
				app.message = 'Wait for your opponent to locate his ships';
				break;
			case 'WAIT_SHOOT_OPPONENT':
				app.message = 'Wait for your opponent to make his shots';
				break;
			case 'WAIT_OPPONENT':
				app.message = 'Wait for your opponent';
				break;
		}

	})
	.fail(function (data) {
		swal("error");
	});

function getPlayers() {
	for (var i = 0; i < app.game.gamePlayers.length; ++i) {
		if (app.game.gamePlayers[i].id == app.valor) {
			app.viewer = app.game.gamePlayers[i].player;
		} else {
			app.otherPlayer = app.game.gamePlayers[i].player;
		}
	}
}

function getShipLocations() {
	for (var i = 0; i < app.game.ships.length; ++i) {
		for (var j = 0; j < app.game.ships[i].locations.length; ++j) {
			app.shipLocations.push(app.game.ships[i].locations[j]);
			console.log(app.shipLocations);
		}
		app.shipsType.push(app.game.ships[i].type);
	}
}

function getSalvoLocations() {

	app.turnViewer = 0;
	for (var i = 0; i < app.game.salvoes.length; ++i) {
		if (app.game.salvoes[i].player == app.viewer.id) {
			++app.turnViewer;
			for (var j = 0; j < app.game.salvoes[i].locations.length; ++j) {
				app.salvoLocations.push({
					location: app.game.salvoes[i].locations[j],
					turno: app.game.salvoes[i].turn,
				});
				console.log(app.salvoLocations);
			}
		}
	}

}

function getShipSalvo() {
	app.turnOponent = 0;
	for (var i = 0; i < app.game.salvoes.length; ++i) {
		if (app.game.salvoes[i].player == app.otherPlayer.id) {
			++app.turnOponent;
			for (var j = 0; j < app.game.salvoes[i].locations.length; ++j) {
				app.shipSalvoLocations.push({
					location: app.game.salvoes[i].locations[j],
					turno: app.game.salvoes[i].turn,
				});
				console.log(app.shipSalvoLocations);
			}
		}
	}
}
