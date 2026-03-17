import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import DreamEntry from './pages/DreamEntry';
import DreamLog from './pages/DreamLog';
import DreamDetail from './pages/DreamDetail';
import { SyncManager } from './services/SyncManager';
import { useEffect, useState } from 'react';
import api from './api/axios';

function App() {
  const [isAuthReady, setIsAuthReady] = useState(false);

  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('dream_token');
      if (!token) {
        try {
          // Detect device automatically or send a generic one for now
          const platformInfo = navigator.userAgent.includes('Mobile') ? 'Mobile Web' : 'Desktop Web';
          
          const response = await api.post('/users/guest', {
            deviceInfo: platformInfo
          });
          
          const data = response.data;
          // GuestRegistrationResponse: { userId, guestToken, token, freeTrialAvailable }
          const newToken = data.token || data.guestToken;
          
          if (newToken) {
            localStorage.setItem('dream_token', newToken);
            localStorage.setItem('dream_userId', data.userId);
          }
        } catch (error) {
          console.error("Failed to register guest user:", error);
          // Might want to retry later, but for now we just log
        }
      }
      setIsAuthReady(true);
      // Initialize sync after auth is confirmed
      SyncManager.initBackgroundSync();
    };

    initializeAuth();
  }, []);

  if (!isAuthReady) {
    return <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'var(--background)', color: 'var(--text-muted)' }}>Başlatılıyor...</div>;
  }

  return (
    <Router>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/entry" element={<DreamEntry />} />
        <Route path="/log" element={<DreamLog />} />
        <Route path="/dream/:id" element={<DreamDetail />} />
      </Routes>
    </Router>
  );
}

export default App;
