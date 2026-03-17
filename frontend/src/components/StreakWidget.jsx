import React, { useEffect, useState } from 'react';
import { Flame } from 'lucide-react';

const StreakWidget = () => {
  const [streak, setStreak] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchStreak = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/v1/streaks');
        if (response.ok) {
          const data = await response.json();
          setStreak(data.currentStreak || 0);
        } else {
          // Fallback or handle error
          console.error("Failed to fetch streak");
          setStreak(0); // Default to 0 on failure
        }
      } catch (err) {
        console.error("Error fetching streak:", err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchStreak();
  }, []);

  if (isLoading) {
    return <div style={{ minHeight: '160px', display: 'flex', alignItems: 'center' }}>Yükleniyor...</div>;
  }

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '32px',
      backgroundColor: 'var(--surface)',
      borderRadius: 'var(--radius-lg)',
      width: '100%',
      maxWidth: '300px',
      boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.3)'
    }}>
      <Flame size={64} color="#F59E0B" style={{ marginBottom: '16px' }} />
      <h2 style={{ fontSize: '2.5rem', fontWeight: 800, margin: 0 }}>{streak}</h2>
      <p style={{ color: 'var(--text-muted)', fontSize: '1rem', marginTop: '8px' }}>Gündür Buradasın</p>
    </div>
  );
};

export default StreakWidget;
