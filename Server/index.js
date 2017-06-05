
var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];

server.listen(8081, function() {

	console.log("Server is now running...");

});

io.on('connection', function(socket) {

	console.log("Player connected...");
	socket.emit('socketID', { id: socket.id });

	socket.emit('getPlayers', players);
	socket.broadcast.emit('newPlayer', { id: socket.id });
	players.push(new player(socket.id, 0, 0, 0, 0)); //add to newly joined client

	socket.on('playerMoved', function(data) {
        data.id = socket.id;
        socket.broadcast.emit('playerMoved', data);

        for(var i = 0; i<players.length; i++) {
            if(players[i].id==data.id) {
                players[i].vx = data.vx;
                players[i].vy = data.vy;
                players[i].x = data.x;
                players[i].y = data.y;
            }
        }
	});

	socket.on('playerShot', function(data) {
         data.id = socket.id;
         socket.broadcast.emit('playerShot', data);
	});

//	socket.on('ping', function() {
//        socket.emit('pong');
//    });

	socket.on('disconnect', function() {

		console.log("Player disconnected...");
		socket.broadcast.emit('playerDisconnected', {id: socket.id});

		for(var i = 0; i<players.length; i++) { //remove if disconnected
		    if(players[i].id==socket.id) {
		        players.splice(i, 1);
		    }
		}

	});


});

process.on('uncaughtException', function (error) {
   console.log(error.stack);
});

function player(id, vx, vy, x, y) {
    this.id = id;
    this.vx = vx;
    this.vy = vy;
    this.x = x;
    this.y = y;
}

