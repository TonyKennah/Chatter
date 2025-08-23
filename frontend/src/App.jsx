import React, { useState, useEffect, useRef, useCallback } from 'react';
import './App.css';

function UsernameInput({ onUsernameSubmit }) {
  const [input, setInput] = useState('');

  const handleSubmit = () => {
    if (input.trim()) {
      onUsernameSubmit(input.trim());
    }
  };

  return (
    <div className="username-container">
      <h2>Enter your username</h2>
      <div className="input-area">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSubmit()}
          placeholder="Your name..."
        />
        <button onClick={handleSubmit}>Join</button>
      </div>
    </div>
  );
}

function ChatRoom({ room, onLeave, username }) {
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [typingUsers, setTypingUsers] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const ws = useRef(null);
  const messagesEndRef = useRef(null);
  const typingTimeout = useRef(null);

  useEffect(() => {
    // Don't connect if there's no room object.
    if (!room || !username) return;
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const backendHost = 'localhost:8080';
    const encodedRoomId = encodeURIComponent(room.id);
    const encodedUsername = encodeURIComponent(username);
    const wsUrl = `${protocol}//${backendHost}/chat-ws/${encodedRoomId}?username=${encodedUsername}`;

    ws.current = new WebSocket(wsUrl);
    
    ws.current.onopen = () => {
      console.log(`WebSocket connection opened for room ${room.id} as user ${username}`);
      // Clear messages from previous room
      setMessages([]);
    };

    ws.current.onerror = (error) => {
      console.error('WebSocket Error: The connection to the server could not be established. Please check that the backend is running and accessible.');
      console.error('Specific error event:', error);
    };

    ws.current.onmessage = (event) => {
      const data = JSON.parse(event.data);
      switch (data.type) {
        case 'user-id':
          setCurrentUser(data.id);
          break;
        case 'chat':
        case 'info':
          setMessages((prev) => [...prev, data]);
          break;
        case 'typing':
          setTypingUsers((prev) => {
            const otherTypingUsers = prev.filter(u => u.id !== data.user);
            return [...otherTypingUsers, { id: data.user, ts: Date.now() }];
          });
          break;
        case 'user-left':
          setMessages((prev) => [...prev, { type: 'info', payload: `User ${data.user} has left.` }]);
          break;
        default:
          console.warn('Received unknown message type:', data.type);
      }
    };

    ws.current.onclose = () => {
      console.log(`WebSocket connection closed for room ${room.id}`);
    };

    return () => {
      if (ws.current) ws.current.close();
    };
  }, [room, username]); // Re-establish connection when the room object changes.

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  useEffect(() => {
    // Periodically clean up users who have stopped typing.
    const interval = setInterval(() => {
      setTypingUsers((prev) => prev.filter(u => Date.now() - u.ts < 2000));
    }, 2000);
    return () => clearInterval(interval);
  }, []);

  const handleSendMessage = useCallback(() => {
    if (inputValue.trim() && ws.current?.readyState === WebSocket.OPEN) {
      ws.current.send(JSON.stringify({ type: 'chat', payload: inputValue }));
      setInputValue('');
    }
  }, [inputValue]);

  const handleTyping = useCallback(() => {
    if (ws.current?.readyState === WebSocket.OPEN) {
      ws.current.send(JSON.stringify({ type: 'typing' }));
    }
  }, []);

  const handleInputChange = useCallback((e) => {
    setInputValue(e.target.value);
    if (typingTimeout.current) {
      clearTimeout(typingTimeout.current);
    }
    typingTimeout.current = setTimeout(handleTyping, 300);
  }, [handleTyping]);

  const filteredTypingUsers = typingUsers.filter(u => u.id !== currentUser);

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h2>{room.name}</h2>
        <button onClick={onLeave} className="leave-button">Leave Room</button>
      </div>
      <div className="messages">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`message ${msg.type === 'info' ? 'info-message' : ''} ${msg.user === currentUser ? 'current-user' : ''}`}
          >
            {msg.type === 'info' ? (
              <span>{msg.payload}</span>
            ) : (
              <span><strong>{msg.user === currentUser ? 'You' : msg.user}:</strong> {msg.payload}</span>
            )}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      <div className="typing-indicator">
        {filteredTypingUsers.length > 0 &&
          `${filteredTypingUsers.map(u => u.id).join(', ')} ${filteredTypingUsers.length === 1 ? 'is' : 'are'} typing...`
        }
      </div>
      <div className="input-area">
        <input
          type="text"
          value={inputValue}
          onChange={handleInputChange}
          onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
          placeholder="Type a message..."
        />
        <button onClick={handleSendMessage}>Send</button>
      </div>
    </div>
  );
}

function RoomBrowser({ onSelectRoom, rooms }) {
  return (
    <div className="room-browser">
      <h2>Join a Room</h2>
      <div className="room-list">
        {rooms.map((room) => (
          <button key={room.id} onClick={() => onSelectRoom(room)}>
            {room.name}
          </button>
        ))}
      </div>
    </div>
  );
}

// This component manages which view is active: the room browser or the chat room.
function App() {
  const [rooms, setRooms] = useState([]);
  const [currentRoom, setCurrentRoom] = useState(null);
  const [username, setUsername] = useState('');

  useEffect(() => {
    if (username) { // Only fetch rooms after a username is entered
      fetch('/api/rooms')
        .then(response => {
          if (!response.ok) {
            throw new Error('Failed to fetch rooms. Is the backend server running?');
          }
          return response.json();
        })
        .then(data => setRooms(data))
        .catch(error => console.error(error.message));
    }
  }, [username]);

  const handleUsernameSubmit = (name) => {
    setUsername(name);
  };

  const handleSelectRoom = (room) => {
    setCurrentRoom(room);
  };

  const handleLeaveRoom = () => {
    setCurrentRoom(null);
  };

  return (
    <div className="App">
      <main className="App-main">
        {!username ? (
          <UsernameInput onUsernameSubmit={handleUsernameSubmit} />
        ) : currentRoom ? (
          <ChatRoom room={currentRoom} onLeave={handleLeaveRoom} username={username} />
        ) : (
          <RoomBrowser onSelectRoom={handleSelectRoom} rooms={rooms} />
        )}
      </main>
    </div>
  );
}

export default App;