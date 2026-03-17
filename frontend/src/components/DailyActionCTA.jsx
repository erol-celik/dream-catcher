import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { db } from '../db/db';

const DailyActionCTA = () => {
  const [hasEntryToday, setHasEntryToday] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const checkTodayEntry = async () => {
      const today = new Date().toISOString().split('T')[0];
      
      // Check local DB first for any entry today
      const todayEntries = await db.local_dreams
        .filter(dream => dream.date.startsWith(today))
        .toArray();
      
      if (todayEntries.length > 0) {
        setHasEntryToday(true);
        return;
      }

      // Check backend (optional based on requirement, but user mentioned GET /api/v1/activities)
      try {
        const response = await fetch('http://localhost:8080/api/v1/activities?date=' + today);
        if (response.ok) {
           const data = await response.json();
           // Example structure check
           if (data.hasEntry) {
              setHasEntryToday(true);
           }
        }
      } catch (err) {
        console.error('Failed to check backend for today entry:', err);
      }
    };

    checkTodayEntry();
  }, []);

  return (
    <button
      className="primary"
      disabled={hasEntryToday}
      onClick={() => navigate('/entry')}
      style={{
        width: '100%',
        padding: '20px',
        fontSize: '1.25rem',
        marginTop: '32px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '12px'
      }}
    >
      {hasEntryToday ? 'Günün Tamamlandı ✨' : 'Bugün Rüya Gördün Mü?'}
    </button>
  );
};

export default DailyActionCTA;
