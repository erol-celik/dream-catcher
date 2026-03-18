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
    <div style={{ marginTop: '32px', width: '100%', display: 'flex', flexDirection: 'column', gap: '16px' }}>
      <button
        className="primary"
        disabled={hasEntryToday}
        onClick={() => navigate('/entry')}
        style={{
          width: '100%',
          padding: '20px',
          fontSize: '1.25rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '12px'
        }}
      >
        {hasEntryToday ? 'Günün Tamamlandı ✨' : 'Bugün Rüya Gördün Mü?'}
      </button>

      {!hasEntryToday && (
        <button
          onClick={async () => {
             const focus = prompt("That's okay! What is your main focus for today?");
             if (focus && focus.trim().length > 0) {
                const tzOffset = (new Date()).getTimezoneOffset() * 60000;
                const localDate = new Date(Date.now() - tzOffset).toISOString().slice(0, 19);
                
                await db.local_dreams.add({
                  clientId: crypto.randomUUID(),
                  text: focus,
                  sentiment: 'NEUTRAL',
                  is_synced: 0,
                  date: localDate,
                  type: 'DUMMY_FOCUS'
                });
                setHasEntryToday(true);
             }
          }}
          style={{
            width: '100%',
            padding: '16px',
            fontSize: '1rem',
            backgroundColor: 'transparent',
            color: 'var(--text-muted)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-md)'
          }}
        >
          Didn't dream / Don't remember
        </button>
      )}
    </div>
  );
};

export default DailyActionCTA;
