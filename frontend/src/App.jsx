import React, { useState, useEffect, useRef } from 'react';
import './App.css';

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [typingUsers, setTypingUsers] = useState({});
  const ws = useRef(null);
  const messagesEndRef = useRef(null);
  const typingTimeout = useRef(null);

  useEffect(() => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const wsUrl = `${protocol}//${host}/chat`;


    // Connect to the WebSocket server
    ws.current = new WebSocket(wsUrl);

    ws.current.onopen = () => {
      console.log('WebSocket connection opened');
    };

    ws.current.onmessage = (event) => {
      const data = JSON.parse(event.data);

      switch (data.type) {
        case 'chat':
          setMessages(prev => [...prev, { user: data.user, text: data.payload, type: 'chat' }]);
          setTypingUsers(prev => {
            if (prev[data.user]) {
              clearTimeout(prev[data.user]);
              const { [data.user]: _, ...rest } = prev;
              return rest;
            }
            return prev;
          });
          break;
        case 'info':
          setMessages(prev => [...prev, { text: data.payload, type: 'info' }]);
          break;
        case 'typing':
          setTypingUsers(prevTypingUsers => {
            if (prevTypingUsers[data.user]) {
              clearTimeout(prevTypingUsers[data.user]);
            }
            const timerId = setTimeout(() => {
              setTypingUsers(currentTypingUsers => {
                const { [data.user]: _, ...rest } = currentTypingUsers;
                return rest;
              });
            }, 3000); // User is considered "not typing" after 3 seconds.
            return { ...prevTypingUsers, [data.user]: timerId };
          });
          break;
        default:
          break;
      }
    };

    ws.current.onclose = () => {
      console.log('WebSocket connection closed');
    };

    // Cleanup on component unmount
    return () => {
      if (ws.current) {
        ws.current.close();
      }
    };
  }, []); // Empty dependency array means this effect runs once on mount

  useEffect(() => {
    // Scroll to the bottom every time messages update
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, typingUsers]);

  const sendJsonMessage = (type, payload = {}) => {
    if (ws.current && ws.current.readyState === WebSocket.OPEN) {
      const message = { type, ...payload };
      ws.current.send(JSON.stringify(message));
    }
  };

  const sendMessage = () => {
    if (input.trim()) {
      sendJsonMessage('chat', { payload: input });
      setMessages(prev => [...prev, { user: 'You', text: input, type: 'chat' }]);
      setInput('');
      if (typingTimeout.current) {
        clearTimeout(typingTimeout.current);
        typingTimeout.current = null;
      }
    }
  };

  const handleTyping = (e) => {
    setInput(e.target.value);
    if (!typingTimeout.current) {
      sendJsonMessage('typing');
      typingTimeout.current = setTimeout(() => {
        typingTimeout.current = null;
      }, 2000); // Can send a typing event every 2 seconds
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  };

  const typingIndicatorText = Object.keys(typingUsers).length > 0
    ? `${Object.keys(typingUsers).join(', ')} is typing...`
    : '\u00A0'; // non-breaking space to maintain layout

  return (
    <div className="App">
      <header className="App-header">
        <h1>Chatter</h1>
      </header>
      <div className="chat-container">
        <div className="messages">
          {messages.map((msg, index) => (
            <div key={index} className={`message ${msg.type === 'info' ? 'info-message' : ''}`}>
              {msg.type === 'chat' && <strong>{msg.user}: </strong>}
              {msg.text}
            </div>
          ))}
          <div ref={messagesEndRef} />
        </div>
        <div className="typing-indicator">
          {typingIndicatorText}
        </div>
        <div className="input-area">
          <input
            type="text"
            value={input}
            onChange={handleTyping}
            onKeyPress={handleKeyPress}
            placeholder="Type a message..."
          />
          <button onClick={sendMessage}>Send</button>
        </div>
      </div>
    </div>
  );
}

export default App;