// creating global parameters and start
// listening to 'port', we are creating an express
// server and then we are binding it with socket.io
var express 	= require('express'),
	app			= express(),
    server  	= require('http').createServer(app),
    io      	= require('socket.io').listen(server),
    port    	= 8989,

    // hash object to save clients data,
    // { socketid: { userId, nickname }, socketid: { ... } }
    chatClients = new Object();

//this is to listen to port.
server.listen(port);

// configure express, since this server is
// also a web server, we need to define the
// paths to the static files
app.use("/styles", express.static(__dirname + '/public/styles'));
app.use("/scripts", express.static(__dirname + '/public/scripts'));
app.use("/images", express.static(__dirname + '/public/images'));

//this requests the index.html file to show on the localhost/8989
app.get('/', function (req, res) {
	res.sendfile(__dirname + '/public/index.html');
});

//this log level as stated only shows those that are connected and disconnected
io.set('log level', 2);

// if the client doesn't support websockets then this  will replace it with xhr-polling, this was adapted from github.
//not entirely necessary, but still a very important aspect to learn.
io.set('transports', [ 'websocket', 'xhr-polling' ]);

//this handles the events during the connection
io.sockets.on('connection', function(socket){
	
	//once the connection is initiated the name of the user is sent through the event.
	socket.on('connect', function(data){
		connect(socket, data);
	});

	// when the client sends a message, he emits
	// this event, then the server forwards the
	// message to other clients within the same connection
	socket.on('chatmessage', function(data){
		chatmessage(socket, data);
	});
	
	// client registeration into a room
	socket.on('register', function(data){
		register(socket, data);
	});

	// client signing out of a room
	socket.on('deregister', function(data){
		deregister(socket, data);
	});
	
//another feature of socket.io when someone disconnects, it handles it.
	socket.on('disconnect', function(){
		disconnect(socket);
	});
});
//this function handles the process to
// create a client for the socket
//then provide an id 
function connect(socket, data){
	data.userId = createid();

	// unlike the previous project, instead of saving the client to an array, here we're using hashing to quickly retrieve the user.
	chatClients[socket.id] = data;

	
	// the client is ready to be updated.
	socket.emit('ready', { userId: data.userId });
	
	// auto register the client to the 'lobby'
	register(socket, { room: 'lobby' });

	// sends a list of all active rooms in the
	// server
	socket.emit('roomslist', { rooms: activeRooms() });
}

// when a client disconnect, deregister him from
// the rooms he registered to
function disconnect(socket){
	// get a list of rooms for the client
	var rooms = io.sockets.manager.roomClients[socket.id];
	
	// deregister from the rooms
	for(var room in rooms){
		if(room && rooms[room]){
			deregister(socket, { room: room.replace('/','') });
		}
	}

	// After a client have deregisetered, we can delete him/her from the hash object.
	delete chatClients[socket.id];
}

// this handles the messages from the client side.
function chatmessage(socket, data){
	// using he "socket.broadcast" will send a message to everyone connected.
	socket.broadcast.to(data.room).emit('chatmessage', 
	{ client: chatClients[socket.id], message: data.message, room: data.room });
}

// assigning a client to a room.
function register(socket, data){
	//show the active rooms
	var rooms = activeRooms();

	//checking if  a room is already added.
	if(rooms.indexOf('/' + data.room) < 0){
		socket.broadcast.emit('addroom', { room: data.room });
	}

	// register the client to the room
	socket.join(data.room);

	// update all other clients about the online
	// presence
	updatePresence(data.room, socket, 'online');

	// send to the client a list of all registered clients
	// in this room
	socket.emit('Clients', { room: data.room, clients: getClientsInRoom(socket.id, data.room) });
}

//as stated in the demo, 
function deregister(socket, data){
	// update the client roster
	updatePresence(data.room, socket, 'offline');
	
	// remove the client from socket.io room
	socket.leave(data.room);

	// if this client was the only one in that room
	// we are updating all clients about that the
	// room is destroyed
	if(!countClientsInRoom(data.room)){

		// with 'io.sockets' we can contact all the
		// clients that connected to the server
		io.sockets.emit('removeroom', { room: data.room });
	}
}

// 'io.sockets.manager.rooms' is an object that holds
// the active room names as a key, returning array of
// room names
function activeRooms(){
	return Object.keys(io.sockets.manager.rooms);
}

// get array of clients in a room
function getClientsInRoom(socketId, room){
	// get array of socket ids in this room
	var socketIds = io.sockets.manager.rooms['/' + room];
	var clients = [];
	
	if(socketIds && socketIds.length > 0){
		socketsCount = socketIds.lenght;
		
		// push every client to the result array
		for(var i = 0, len = socketIds.length; i < len; i++){
			
			// check if the socket is not the requesting
			// socket
			if(socketIds[i] != socketId){
				clients.push(chatClients[socketIds[i]]);
			}
		}
	}
	
	return clients;
}

// get the amount of clients in a room
function countClientsInRoom(room){
	// 'io.sockets.manager.rooms' is an object that holds
	// the active room names as a key and an array of
	// all registerd client socket ids
	if(io.sockets.manager.rooms['/' + room]){
		return io.sockets.manager.rooms['/' + room].length;
	}
	return 0;
}

// updating all other clients when a client goes
// online or offline. 
function updatePresence(room, socket, state){
	// socket.io may add a trailing '/' to the
	// room name so we are clearing it
	room = room.replace('/','');

	
	socket.broadcast.to(room).emit('presence', { client: chatClients[socket.id], state: state, room: room });
}

// an id generator.
function createid(){
	var S4 = function () {
		return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
	};
	return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
}

// show a message in console
console.log('The chat server is running, currently listening to ', port);