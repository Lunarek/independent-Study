(function($){

	// create global app parameters...
	var M_MAX_LENGTH = 10,
		lockShakeAnimation = false,
		socket = null,
		clientId = null,
		userName = null,

		// holds the current room we are in
		currentRoom = null,

		// server information
		serverAddress = 'http://localhost',
		serverDisplayName = 'Server',
		serverDisplayColor = '#1c5380',

		// some templates we going to use in the chat,
		// like message row, client and room, this
		// templates will be rendered with jQuery.tmpl
		tmplt = {
			room: [
				'<li data-roomId="${room}">',
					'<span class="icon"></span> ${room}',
				'</li>'
			].join(""),
			client: [
				'<li data-clientId="${clientId}" class="cf">',
					'<div class="fl clientName"><span class="icon"></span> ${userName}</div>',
					'<div class="fr composing"></div>',
				'</li>'
			].join(""),
			message: [
				'<li class="cf">',
					'<div class="fl sender">${sender}: </div><div class="fl text">${text}</div><div class="fr time">${time}</div>',
				'</li>'
			].join("")
		};

	// bind DOM elements like button clicks and keydown
	function bindDOMEvents(){
		
		$('.chat-input input').on('keydown', function(e){
			var key = e.which || e.keyCode;
			if(key == 13) { handleMessage(); }
		});

		$('.chat-submit button').on('click', function(){
			handleMessage();
		});

		$('#userName-popup .input input').on('keydown', function(e){
			var key = e.which || e.keyCode;
			if(key == 13) { handleuserName(); }
		});

		$('#userName-popup .begin').on('click', function(){
			handleuserName();
		});
		
		$('#addroom-popup .input input').on('keydown', function(e){
			var key = e.which || e.keyCode;
			if(key == 13) { createRoom(); }
		});

		$('#addroom-popup .create').on('click', function(){
			createRoom();
		});

		$('.big-button-green.start').on('click', function(){
			$('#userName-popup .input input').val('');
			Avgrund.show('#userName-popup');
			window.setTimeout(function(){
	        	$('#userName-popup .input input').focus();
	        },100);
		});

		$('.chat-rooms .title-button').on('click', function(){
			$('#addroom-popup .input input').val('');
			Avgrund.show('#addroom-popup');
			window.setTimeout(function(){
	        	$('#addroom-popup .input input').focus();
	        },100);
		});

		$('.chat-rooms ul').on('scroll', function(){
			$('.chat-rooms ul li.selected').css('top', $(this).scrollTop());
		});

		$('.chat-messages').on('scroll', function(){
			var self = this;
			window.setTimeout(function(){
				if($(self).scrollTop() + $(self).height() < $(self).find('ul').height()){
					$(self).addClass('scroll');
				} else {
					$(self).removeClass('scroll');
				}
			}, 50);
		});

		$('.chat-rooms ul li').live('click', function(){
			var room = $(this).attr('data-roomId');
			if(room != currentRoom){
				socket.emit('unregister', { room: currentRoom });
				socket.emit('register', { room: room });
			}
		});
	}

	// server side event handler.
	function SocketEvents(){

		// the server runs the connect event.
		socket.on('connect', function(){
			//sends the name to the client. 
			socket.emit('connect', { userName: userName });
		});
		
		// after the server created a client for us, the ready event
		// is fired in the server with our clientId, now we can start 
		socket.on('ready', function(data){
			// hiding the 'connecting...' message
			$('.chat-shadow').animate({ 'opacity': 0 }, 200, function(){
				$(this).hide();
				$('.chat input').focus();
			});
			
			// saving the clientId localy
			clientId = data.clientId;
		});

		// after the initialize, the server sends a list of
		// all the active rooms
		socket.on('roomslist', function(data){
			for(var i = 0, len = data.rooms.length; i < len; i++){
				// in socket.io, their is always one default room
				// without a name (empty string), every socket is automaticaly
				// joined to this room, however, we don't want this room to be
				// displayed in the rooms list
				if(data.rooms[i] != ''){
					addRoom(data.rooms[i], false);
				}
			}
		});

		// when someone sends a message, the sever push it to
		// our client through this event with a relevant data
		socket.on('chatmessage', function(data){
			var userName = data.client.userName;
			var message = data.message;
			
			//display the message in the chat window
			insertMessage(userName, message, true, false, false);
		});
		
		// when we registers to a room, the server sends a list
		// with the clients in this room
		socket.on('roomclients', function(data){
			
			// add the room name to the rooms list
			addRoom(data.room, false);

			// set the current room
			setCurrentRoom(data.room);
			
			// announce a welcome message
			insertMessage(serverDisplayName, 'Welcome to the room: `' + data.room + '`... enjoy!', true, false, true);
			$('.chat-clients ul').empty();
			
			// add the clients to the clients list
			addClient({ userName: userName, clientId: clientId }, false, true);
			for(var i = 0, len = data.clients.length; i < len; i++){
				if(data.clients[i]){
					addClient(data.clients[i], false);
				}
			}

			// hide connecting to room message message
			$('.chat-shadow').animate({ 'opacity': 0 }, 200, function(){
				$(this).hide();
				$('.chat input').focus();
			});
		});
		
		// if someone creates a room the server updates us
		// about it
		socket.on('addroom', function(data){
			addRoom(data.room, true);
		});
		
		// if one of the room is empty from clients, the server,
		// destroys it and updates us
		socket.on('removeroom', function(data){
			removeRoom(data.room, true);
		});
		
		// with this event the server tells us when a client
		// is connected or disconnected to the current room
		socket.on('presence', function(data){
			if(data.state == 'online'){
				addClient(data.client, true);
			} else if(data.state == 'offline'){
				removeClient(data.client, true);
			}
		});
	}

	// add a room to the rooms list, socket.io may add
	// a trailing '/' to the name so we are clearing it
	function addRoom(name, announce){
		// clear the trailing '/'
		name = name.replace('/','');

		// check if the room is not already in the list
		if($('.chat-rooms ul li[data-roomId="' + name + '"]').length == 0){
			$.tmpl(tmplt.room, { room: name }).appendTo('.chat-rooms ul');
			// if announce is true, show a message about this room
			if(announce){
				insertMessage(serverDisplayName, 'The room `' + name + '` created...', true, false, true);
			}
		}
	}

	// remove a room from the rooms list
	function removeRoom(name, announce){
		$('.chat-rooms ul li[data-roomId="' + name + '"]').remove();
		// if announce is true, show a message about this room
		if(announce){
			insertMessage(serverDisplayName, 'The room `' + name + '` destroyed...', true, false, true);
		}
	}

	// add a client to the clients list
	function addClient(client, announce, isMe){
		var $html = $.tmpl(tmplt.client, client);
		
		// if this is our client, mark him with color
		if(isMe){
			$html.addClass('me');
		}

		// if announce is true, show a message about this client
		if(announce){
			insertMessage(serverDisplayName, client.userName + ' has joined the room...', true, false, true);
		}
		$html.appendTo('.chat-clients ul')
	}

	// remove a client from the clients list
	function removeClient(client, announce){
		$('.chat-clients ul li[data-clientId="' + client.clientId + '"]').remove();
		
		// if announce is true, show a message about this room
		if(announce){
			insertMessage(serverDisplayName, client.userName + ' has left the room...', true, false, true);
		}
	}

	// every client can create a new room, when creating one, the client
	// is unregisterd from the current room and then registerd to the
	// room he just created, if he trying to create a room with the same
	// name like another room, then the server will register the user
	// to the existing room
	function createRoom(){
		var room = $('#addroom-popup .input input').val().trim();
		if(room && room.length <= ROOM_MAX_LENGTH && room != currentRoom){
			
			// show room creating message
			$('.chat-shadow').show().find('.content').html('Creating room: ' + room + '...');
			$('.chat-shadow').animate({ 'opacity': 1 }, 200);
			
			// unregister from the current room
			socket.emit('unregister', { room: currentRoom });

			// create and register to the new room
			socket.emit('register', { room: room });
			Avgrund.hide();
		} else {
			shake('#addroom-popup', '#addroom-popup .input input', 'tada', 'yellow');
			$('#addroom-popup .input input').val('');
		}
	}

	// sets the current room when the client
	// makes a subscription
	function setCurrentRoom(room){
		currentRoom = room;
		$('.chat-rooms ul li.selected').removeClass('selected');
		$('.chat-rooms ul li[data-roomId="' + room + '"]').addClass('selected');
	}

	// save the client userName and start the chat by
	// calling the 'connect()' function
	function handleuserName(){
		var nick = $('#userName-popup .input input').val().trim();
		if(nick && nick.length <= NICK_MAX_LENGTH){
			userName = nick;
			Avgrund.hide();
			connect();
		} else {
			shake('#userName-popup', '#userName-popup .input input', 'tada', 'yellow');
			$('#userName-popup .input input').val('');
		}
	}

	// handle the client messages
	function handleMessage(){
		var message = $('.chat-input input').val().trim();
		if(message){

			// send the message to the server with the room name
			socket.emit('chatmessage', { message: message, room: currentRoom });
			
			// display the message in the chat window
			insertMessage(userName, message, true, true);
			$('.chat-input input').val('');
		} else {
			shake('.chat', '.chat input', 'wobble', 'yellow');
		}
	}

	// insert a message to the chat window, this function can be
	// called with some flags
	function insertMessage(sender, message, showTime, isMe, idServer){
		var $html = $.tmpl(tmplt.message, {
			sender: sender,
			text: message,
			time: showTime ? getTime() : ''
		});

		// if isMe is true, mark this message so we can
		// know that this is our message in the chat window
		if(isMe){
			$html.addClass('marker');
		}

		// if idServer is true, mark this message as a server
		// message
		if(idServer){
			$html.find('.sender').css('color', serverDisplayColor);
		}
		$html.appendTo('.chat-messages ul');
		$('.chat-messages').animate({ scrollTop: $('.chat-messages ul').height() }, 100);
	}

	// return a short time format for the messages
	function getTime(){
		var date = new Date();
		return (date.getHours() < 10 ? '0' + date.getHours().toString() : date.getHours()) + ':' +
				(date.getMinutes() < 10 ? '0' + date.getMinutes().toString() : date.getMinutes());
	}

	// just for animation
	function shake(container, input, effect, bgColor){
		if(!lockShakeAnimation){
			lockShakeAnimation = true;
			$(container).addClass(effect);
			$(input).addClass(bgColor);
			window.setTimeout(function(){
				$(container).removeClass(effect);
				$(input).removeClass(bgColor);
				$(input).focus();
				lockShakeAnimation = false;
			}, 1500);
		}
	}
	
	// once a name is given we use the following function ot connect.
	function connect(){
		// show connecting message
		$('.chat-shadow .content').html('Connecting...');
		
		// asign the connection to the socket.
		socket = io.connect(serverAddress);
		
		//socket events to show.
		SocketEvents();
	}

	// on document ready, bind the DOM elements to events
	$(function(){
		bindDOMEvents();
	});

})(jQuery);